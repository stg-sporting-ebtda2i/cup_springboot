package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.authentication.Authenticatable;
import com.stgsporting.piehmecup.entities.Quiz;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.exceptions.ChangePasswordException;
import com.stgsporting.piehmecup.exceptions.NotFoundException;
import com.stgsporting.piehmecup.exceptions.UserNotFoundException;
import com.stgsporting.piehmecup.helpers.Response;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {
    private final UserService userService;
    private final EntityService entityService;
    private final HttpService httpService;
    private final WalletService walletService;

    QuizService(UserService userService, EntityService entityService, HttpService httpService, WalletService walletService) {
        this.userService = userService;
        this.entityService = entityService;
        this.httpService = httpService;
        this.walletService = walletService;
    }

    public void correctResponse(Long responseId) {
        String url = "/responses/" + responseId + "/correct";

        Response response = httpService.patch(url, new JSONObject());

        if (!response.isSuccessful()) {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                JSONObject jsonObject = response.getJsonBody();
                throw new IllegalArgumentException(jsonObject.getAsString("message"));
            }

            throw new IllegalArgumentException("Could not correct response");
        }

        JSONObject jsonObject = response.getJsonBody();
        Long entityId = jsonObject.getAsNumber("entity_id").longValue();
        long quizId = jsonObject.getAsNumber("quiz_id").longValue();
        long questionId = jsonObject.getAsNumber("question_id").longValue();
        int coins = jsonObject.getAsNumber("points").intValue();
        User user = userService.getUserByQuizId(entityId)
                .orElseThrow(UserNotFoundException::new);

        walletService.credit(user, coins, "Corrected Question: " + questionId + " in Quiz: " + quizId);
    }

    public void updateResponse(Long responseId, JSONObject body) {
        String url = "/responses/" + responseId;

        Response response = httpService.patch(url, body);

        if (!response.isSuccessful()) {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST || response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                JSONObject jsonObject = response.getJsonBody();
                throw new IllegalArgumentException(jsonObject.getAsString("message"));
            }

            throw new IllegalArgumentException("Could not update response");
        }

        JSONObject jsonObject = response.getJsonBody();
        Long entityId = jsonObject.getAsNumber("entity_id").longValue();
        long quizId = jsonObject.getAsNumber("quiz_id").longValue();
        long questionId = jsonObject.getAsNumber("question_id").longValue();
        int deltaPoints = jsonObject.getAsNumber("delta_points").intValue();
        User user = userService.getUserByQuizId(entityId)
                .orElseThrow(UserNotFoundException::new);

        if (deltaPoints > 0) {
            walletService.credit(user, deltaPoints, "Edited Response for Question: " + questionId + " in Quiz: " + quizId);
        }

        if (deltaPoints < 0) {
            walletService.forceDebit(user, Math.abs(deltaPoints), "Edited Response for Question: " + questionId + " in Quiz: " + quizId);
        }
    }

    public void deleteResponse(Long responseId) {
        String url = "/responses/" + responseId;

        Response response = httpService.delete(url);

        if (!response.isSuccessful()) {
            throw new IllegalArgumentException("Could not delete response");
        }

        JSONObject res = response.getJsonBody();
        int pointsToRemove = res.getAsNumber("points").intValue();
        Quiz quiz = Quiz.fromJson((JSONObject) res.get("quiz"));
        String responseAt = res.getAsString("response_at");
        if (quiz.shouldAddBonus(responseAt)) {
            pointsToRemove += quiz.getBonus().intValue();
        }

        User user = userService.getUserByQuizId(res.getAsNumber("entity_id").longValue())
                .orElseThrow(UserNotFoundException::new);

        walletService.forceDebit(user, pointsToRemove, "Deleted Response in Quiz: " + quiz.getId());
    }

    public JSONObject updateQuiz(Long quizId, JSONObject quiz) {
        Response response = httpService.patch("/quizzes/" + quizId, quiz);

        if (!response.isSuccessful()) {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST || response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                JSONObject jsonObject = response.getJsonBody();
                throw new IllegalArgumentException(jsonObject.getAsString("message"));
            }

            throw new IllegalArgumentException("Could not update quiz");
        }

        JSONObject jsonObject = response.getJsonBody();
        reconcileQuizRescoreWallets(jsonObject);

        return jsonObject;
    }

    private void reconcileQuizRescoreWallets(JSONObject jsonObject) {
        Object rescoreSummaryObject = jsonObject.get("rescore_summary");
        if (!(rescoreSummaryObject instanceof JSONObject rescoreSummary)) {
            return;
        }

        Object itemsObject = rescoreSummary.get("items");
        if (!(itemsObject instanceof JSONArray items)) {
            return;
        }

        for (Object itemObject : items) {
            if (!(itemObject instanceof JSONObject item)) {
                continue;
            }

            int deltaPoints = item.getAsNumber("delta_points").intValue();
            if (deltaPoints == 0) {
                continue;
            }

            Long entityId = item.getAsNumber("entity_id").longValue();
            long quizId = item.getAsNumber("quiz_id").longValue();
            long questionId = item.getAsNumber("question_id").longValue();
            User user = userService.getUserByQuizId(entityId)
                    .orElseThrow(UserNotFoundException::new);

            String reason = "Re-scored Question: " + questionId + " in Quiz: " + quizId;

            if (deltaPoints > 0) {
                walletService.credit(user, deltaPoints, reason);
            }

            if (deltaPoints < 0) {
                walletService.forceDebit(user, Math.abs(deltaPoints), reason);
            }
        }
    }

    public List<Quiz> getQuizzes(SchoolYear schoolYear, Long quizId) {
        String url = quizId == null
                ? "/groups/" + schoolYear.getSlug()
                : "/quizzes?entity=" + quizId + "&published";

        Response response = httpService.get(url);

        List<Quiz> quizzes = new ArrayList<>();
        if (response.isSuccessful()) {
            JSONObject jsonObject = response.getJsonBody();
            if(quizId == null) {
                jsonObject = (JSONObject) jsonObject.get("group");
            }
            JSONArray quizzesArray = (JSONArray) jsonObject.get("quizzes");

            for (Object quizObject : quizzesArray) {
                JSONObject quizJson = (JSONObject) quizObject;

                quizzes.add(Quiz.fromJson(quizJson));
            }
        }

        return quizzes;
    }

    public List<Quiz> getQuizzesForUser() {
        User user = (User) userService.getAuthenticatable();
        SchoolYear schoolYear = user.getSchoolYear();

        return getQuizzes(schoolYear, user.getQuizId());
    }

    public Quiz getQuizBySlug(String slug) {
        Authenticatable user = userService.getAuthenticatable();
        SchoolYear schoolYear = user.getSchoolYear();

        return getQuizBySlug(slug, schoolYear, false, false);
    }

    public Quiz getQuizBySlug(String slug, SchoolYear schoolYear, Boolean withAnswers, Boolean withResponses) {
        String url = "/quizzes/" + schoolYear.getSlug() + "/" + slug;

        if (withAnswers || withResponses) {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");

            if (withAnswers) {
                urlBuilder.append("withAnswers");
            }

            if (withResponses) {
                if (withAnswers) urlBuilder.append("&");
                urlBuilder.append("withResponses");
            }
            url = urlBuilder.toString();
        }

        Response response = httpService.get(url);

        if (response.isSuccessful()) {
            JSONObject jsonObject = response.getJsonBody();
            JSONObject data = (JSONObject) jsonObject.get("quiz");

            return Quiz.fromJson(data);
        }

        throw new NotFoundException("Quiz not found");
    }

    public Long submitQuiz(User user, Quiz quiz, JSONObject answers) {
        if(user.getQuizId() == null || user.getQuizId() == 0) {
            user.setQuizId(
                    entityService.createEntity(user.getUsername(), user.getSchoolYear())
            );
            userService.save(user);
        }

        StringBuilder url = new StringBuilder("/quizzes/")
                .append(user.getSchoolYear().getSlug())
                .append("/")
                .append(quiz.getSlug())
                .append("/")
                .append(user.getQuizId())
                .append("/submit");

        Response response = httpService.post(url.toString(), answers);

        if (response.isSuccessful()) {
            JSONObject jsonObject = response.getJsonBody();

            Long points = (Long) jsonObject.get("points");

            walletService.credit(user, points.intValue(), "Quiz: " + quiz.getId());

            if(quiz.shouldAddBonus()) {
                walletService.credit(user, quiz.getBonus().intValue(), "Bonus for Quiz: " + quiz.getId());
            }

            return points;
        }

        int statusCode = response.getStatusCode().value();
        if (statusCode == 404) {
            throw new NotFoundException("Quiz not found");
        }

        if (statusCode == 400) {
            throw new ChangePasswordException(
                    response.getJsonBody().getAsString("message")
            );
        }

        return 0L;
    }
}
