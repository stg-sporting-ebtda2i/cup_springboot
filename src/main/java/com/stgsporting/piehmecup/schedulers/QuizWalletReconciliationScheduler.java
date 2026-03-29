package com.stgsporting.piehmecup.schedulers;

import com.stgsporting.piehmecup.entities.Quiz;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.enums.TransactionType;
import com.stgsporting.piehmecup.repositories.TransactionRepository;
import com.stgsporting.piehmecup.services.QuizService;
import com.stgsporting.piehmecup.services.UserService;
import com.stgsporting.piehmecup.services.WalletService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class QuizWalletReconciliationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QuizWalletReconciliationScheduler.class);

    private final QuizService quizService;
    private final UserService userService;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    public QuizWalletReconciliationScheduler(
            QuizService quizService,
            UserService userService,
            WalletService walletService,
            TransactionRepository transactionRepository
    ) {
        this.quizService = quizService;
        this.userService = userService;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(fixedRate = 300000)
    public void reconcile() {
        String since = LocalDateTime.now().minusHours(24)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        JSONArray responses = quizService.getRecentQuizResponses(since);
        if (responses.isEmpty()) {
            return;
        }

        List<String> allDescriptions = new ArrayList<>();
        for (Object responseObj : responses) {
            if (!(responseObj instanceof JSONObject response)) {
                continue;
            }
            allDescriptions.add("Quiz: " + response.getAsNumber("quiz_id").longValue());
        }

        Set<String> creditedDescriptions = transactionRepository.findExistingDescriptions(
                TransactionType.CREDIT, allDescriptions
        );

        for (Object responseObj : responses) {
            if (!(responseObj instanceof JSONObject response)) {
                continue;
            }

            Long quizId = response.getAsNumber("quiz_id").longValue();
            if (creditedDescriptions.contains("Quiz: " + quizId)) {
                continue;
            }

            try {
                processResponse(response);
            } catch (Exception e) {
                logger.error("Failed to reconcile quiz response: {}", response, e);
            }
        }
    }

    private void processResponse(JSONObject response) {
        Long entityId = response.getAsNumber("entity_id").longValue();
        Long quizId = response.getAsNumber("quiz_id").longValue();
        int points = response.getAsNumber("points").intValue();

        Optional<User> userOpt = userService.getUserByQuizId(entityId);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        if (points > 0) {
            walletService.credit(user, points, "Quiz: " + quizId);
            logger.info("Reconciled {} points for user {} (entity {}) for quiz {}",
                    points, user.getId(), entityId, quizId);
        }

        if (hasBonusEligibility(response)) {
            JSONObject data = (JSONObject) ((JSONObject) response.get("quiz")).get("data");
            Long bonus = (Long) data.get("bonus");
            walletService.credit(user, bonus.intValue(), "Bonus for Quiz: " + quizId);
            logger.info("Reconciled bonus {} for user {} (entity {}) for quiz {}",
                    bonus, user.getId(), entityId, quizId);
        }
    }

    private boolean hasBonusEligibility(JSONObject response) {
        Object quizObj = response.get("quiz");
        if (!(quizObj instanceof JSONObject quizJson)) {
            return false;
        }

        Object dataObj = quizJson.get("data");
        if (!(dataObj instanceof JSONObject data)) {
            return false;
        }

        if (!data.containsKey("bonus") || !data.containsKey("bonusBefore")) {
            return false;
        }

        Long bonus = (Long) data.get("bonus");
        String bonusBefore = data.getAsString("bonusBefore");
        String createdAt = response.getAsString("created_at");

        if (bonus == null || bonus <= 0 || bonusBefore == null || createdAt == null) {
            return false;
        }

        return Quiz.dateFromString(bonusBefore).after(Quiz.dateFromString(createdAt));
    }
}
