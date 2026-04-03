package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.dtos.insights.AdminStatsPageDTO;
import com.stgsporting.piehmecup.dtos.insights.AttemptedAllQuizUserDTO;
import com.stgsporting.piehmecup.dtos.insights.BestSellerDTO;
import com.stgsporting.piehmecup.dtos.insights.ChartPointDTO;
import com.stgsporting.piehmecup.dtos.insights.ChoiceDistributionDTO;
import com.stgsporting.piehmecup.dtos.insights.EntityQuizAttemptsDTO;
import com.stgsporting.piehmecup.dtos.insights.HardestQuestionDTO;
import com.stgsporting.piehmecup.dtos.insights.HardestQuestionsByQuizDTO;
import com.stgsporting.piehmecup.dtos.insights.QuizDifficultyDTO;
import com.stgsporting.piehmecup.dtos.insights.QuizStatsSummaryDTO;
import com.stgsporting.piehmecup.dtos.insights.StatsSummaryDTO;
import com.stgsporting.piehmecup.dtos.insights.UserLongMetricDTO;
import com.stgsporting.piehmecup.dtos.insights.UserMetricRowDTO;
import com.stgsporting.piehmecup.dtos.insights.UserSpendValueDTO;
import com.stgsporting.piehmecup.dtos.PaginationDTO;
import com.stgsporting.piehmecup.dtos.users.UserCoinsDTO;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.repositories.InsightsRepository;
import com.stgsporting.piehmecup.repositories.UserRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class InsightsService {
    private static final int DEFAULT_LIMIT = 10;
    private static final int QUIZ_DIFFICULTY_COUNT = 5;
    private static final int PER_QUIZ_QUESTIONS_COUNT = 3;
    private static final int ATTEMPTED_ALL_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final InsightsRepository insightsRepository;
    private final QuizService quizService;
    private final FileService fileService;

    public InsightsService(
            UserRepository userRepository,
            InsightsRepository insightsRepository,
            QuizService quizService,
            FileService fileService
    ) {
        this.userRepository = userRepository;
        this.insightsRepository = insightsRepository;
        this.quizService = quizService;
        this.fileService = fileService;
    }

    public List<BestSellerDTO> findBestSeller(Long levelId) {
        return insightsRepository.findBestSeller(levelId);
    }

    public AdminStatsPageDTO getStatsPage(SchoolYear schoolYear, Long levelId) {
        List<UserMetricRowDTO> topOverallUsers = getTopOverallUsers(schoolYear, DEFAULT_LIMIT);
        List<UserMetricRowDTO> topEarnedCoinsUsers = getTopEarnedCoinsUsers(schoolYear, DEFAULT_LIMIT);
        List<UserMetricRowDTO> topValueUsers = getTopValueUsers(schoolYear, DEFAULT_LIMIT);
        List<UserMetricRowDTO> topAttendanceUsers = getTopAttendanceUsers(schoolYear, DEFAULT_LIMIT);

        QuizStatsSummaryDTO quizSummary = quizService.getQuizStatsSummary(schoolYear);
        List<QuizDifficultyDTO> quizDifficulty = quizService.getQuizDifficultyStats(schoolYear);
        List<QuizDifficultyDTO> hardestQuizzes = quizDifficulty.stream().limit(QUIZ_DIFFICULTY_COUNT).toList();
        List<QuizDifficultyDTO> easiestQuizzes = quizDifficulty.stream()
                .sorted(Comparator.comparing(QuizDifficultyDTO::getAccuracy).reversed()
                        .thenComparing(QuizDifficultyDTO::getSubmissionsCount, Comparator.reverseOrder())
                        .thenComparing(QuizDifficultyDTO::getQuizId))
                .limit(QUIZ_DIFFICULTY_COUNT)
                .toList();

        List<HardestQuestionDTO> hardestQuestions = quizService.getHardestQuestionsStats(schoolYear, DEFAULT_LIMIT);
        List<HardestQuestionsByQuizDTO> hardestQuestionsByQuiz = quizService.getHardestQuestionsByQuizStats(
                schoolYear,
                PER_QUIZ_QUESTIONS_COUNT
        );

        ChoiceDistributionDTO mcqDistribution = hardestQuestions.isEmpty()
                ? null
                : quizService.getQuestionDistributionStats(schoolYear, hardestQuestions.get(0).getQuestionId());

        List<BestSellerDTO> bestSellerPlayers = insightsRepository.findBestSeller(levelId).stream()
                .limit(DEFAULT_LIMIT)
                .toList();

        return new AdminStatsPageDTO(
                new StatsSummaryDTO(
                        Math.toIntExact(userRepository.countBySchoolYear(schoolYear)),
                        quizSummary.getQuizzesCount(),
                        quizSummary.getQuestionsCount(),
                        insightsRepository.countApprovedAttendancesBySchoolYear(schoolYear).intValue()
                ),
                topOverallUsers,
                topEarnedCoinsUsers,
                topValueUsers,
                topAttendanceUsers,
                hardestQuizzes,
                easiestQuizzes,
                hardestQuestions,
                hardestQuestionsByQuiz,
                bestSellerPlayers,
                mcqDistribution,
                hardestQuizzes.stream()
                        .map(quiz -> new ChartPointDTO(quiz.getQuizName(), percentageValue(quiz.getAccuracy()), null))
                        .toList(),
                bestSellerPlayers.stream()
                        .map(player -> new ChartPointDTO(player.getName(), player.getCount().doubleValue(), null))
                        .toList(),
                topAttendanceUsers.stream()
                        .map(user -> new ChartPointDTO(user.getUsername(), safeMetric(user.getMetricValue()), null))
                        .toList(),
                buildLeaderboardComparisonChart(topOverallUsers, topEarnedCoinsUsers, topValueUsers)
        );
    }

    public List<UserMetricRowDTO> getTopOverallUsers(SchoolYear schoolYear, int limit) {
        return mapUserCoins(
                insightsRepository.findTopUsersByOverallScore(schoolYear, PageRequest.of(0, normalizeLimit(limit))),
                row -> getOverallScore(row.getUser()),
                UserCoinsDTO::getCoins
        );
    }

    public List<UserMetricRowDTO> getTopEarnedCoinsUsers(SchoolYear schoolYear, int limit) {
        return mapUserCoins(
                userRepository.findTopUsersBySchoolYearAndCoins(schoolYear, PageRequest.of(0, normalizeLimit(limit))),
                row -> row.getCoins().doubleValue(),
                UserCoinsDTO::getCoins
        );
    }

    public List<UserMetricRowDTO> getTopValueUsers(SchoolYear schoolYear, int limit) {
        List<UserSpendValueDTO> users = insightsRepository.findTopUsersByValue(schoolYear, PageRequest.of(0, normalizeLimit(limit)));
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = 1;
        for (UserSpendValueDTO row : users) {
            rows.add(toUserMetricRow(rank++, row.getUser(), row.getMetricValue(), 0));
        }
        return rows;
    }

    public List<UserMetricRowDTO> getTopAttendanceUsers(SchoolYear schoolYear, int limit) {
        List<UserLongMetricDTO> users = insightsRepository.findTopUsersByApprovedAttendances(schoolYear, PageRequest.of(0, normalizeLimit(limit)));
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = 1;
        for (UserLongMetricDTO row : users) {
            rows.add(toUserMetricRow(rank++, row.getUser(), row.getMetricValue().doubleValue(), 0));
        }
        return rows;
    }

    public List<QuizDifficultyDTO> getQuizDifficulty(SchoolYear schoolYear) {
        return quizService.getQuizDifficultyStats(schoolYear);
    }

    public List<HardestQuestionDTO> getHardestQuestions(SchoolYear schoolYear, int limit) {
        return quizService.getHardestQuestionsStats(schoolYear, limit);
    }

    public List<HardestQuestionDTO> getHardestQuestionsForQuiz(SchoolYear schoolYear, String slug, int limit) {
        return quizService.getHardestQuestionsForQuizStats(schoolYear, slug, limit);
    }

    public ChoiceDistributionDTO getQuestionDistribution(SchoolYear schoolYear, Long questionId) {
        return quizService.getQuestionDistributionStats(schoolYear, questionId);
    }

    public PaginationDTO<AttemptedAllQuizUserDTO> getUsersAttemptedAllPublishedQuizzes(SchoolYear schoolYear, Integer page) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        long publishedQuizCount = quizService.getPublishedQuizCount(schoolYear);
        if (publishedQuizCount == 0) {
            return new PaginationDTO<>(new PageImpl<>(List.of(), PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE), 0));
        }

        List<EntityQuizAttemptsDTO> attempts = quizService.getAttemptCounts(schoolYear).stream()
                .filter(item -> Objects.equals(item.getAttemptedQuizzesCount(), publishedQuizCount))
                .toList();

        if (attempts.isEmpty()) {
            return new PaginationDTO<>(new PageImpl<>(List.of(), PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE), 0));
        }

        Map<Long, Long> attemptCountsByEntityId = attempts.stream()
                .collect(java.util.stream.Collectors.toMap(EntityQuizAttemptsDTO::getEntityId, EntityQuizAttemptsDTO::getAttemptedQuizzesCount));

        List<User> users = userRepository.findBySchoolYearAndQuizIdIn(schoolYear, new ArrayList<>(attemptCountsByEntityId.keySet()));
        Map<Long, User> usersByQuizId = users.stream()
                .filter(user -> user.getQuizId() != null)
                .collect(java.util.stream.Collectors.toMap(User::getQuizId, Function.identity()));

        List<UserCoinsDTO> overallUsersWithCoins = insightsRepository.findTopUsersByOverallScore(
                schoolYear,
                PageRequest.of(0, (int) Math.max(userRepository.countBySchoolYear(schoolYear), 1))
        );
        Map<Long, Long> totalCoinsByUserId = overallUsersWithCoins.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row.getUser().getId(),
                        UserCoinsDTO::getCoins,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<AttemptedAllQuizUserDTO> rows = attempts.stream()
                .map(attempt -> {
                    User user = usersByQuizId.get(attempt.getEntityId());
                    if (user == null) {
                        return null;
                    }

                    return new AttemptedAllQuizUserDTO(
                            user.getId(),
                            user.getUsername(),
                            fileService.generateSignedUrl(user.getImgLink()),
                            user.getImgLink(),
                            getOverallScore(user),
                            totalCoinsByUserId.getOrDefault(user.getId(), 0L).intValue(),
                            attempt.getAttemptedQuizzesCount(),
                            publishedQuizCount
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AttemptedAllQuizUserDTO::getOverallScore, Comparator.reverseOrder())
                        .thenComparing(AttemptedAllQuizUserDTO::getTotalCoinsEarned, Comparator.reverseOrder())
                        .thenComparing(AttemptedAllQuizUserDTO::getUsername))
                .toList();

        int start = Math.min(pageNumber * ATTEMPTED_ALL_PAGE_SIZE, rows.size());
        int end = Math.min(start + ATTEMPTED_ALL_PAGE_SIZE, rows.size());

        return new PaginationDTO<>(new PageImpl<>(
                rows.subList(start, end),
                PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE),
                rows.size()
        ));
    }

    private List<UserMetricRowDTO> mapUserCoins(List<UserCoinsDTO> users, Function<UserCoinsDTO, Double> metric, Function<UserCoinsDTO, Long> totalCoinsEarned) {
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = 1;
        for (UserCoinsDTO row : users) {
            rows.add(toUserMetricRow(rank++, row.getUser(), metric.apply(row), totalCoinsEarned.apply(row).intValue()));
        }
        return rows;
    }

    private UserMetricRowDTO toUserMetricRow(long rank, User user, Double metricValue, Integer totalCoinsEarned) {
        return new UserMetricRowDTO(
                rank,
                user.getId(),
                user.getUsername(),
                fileService.generateSignedUrl(user.getImgLink()),
                user.getImgLink(),
                user.getLineupRating(),
                user.getTotalChemistry(),
                getOverallScore(user),
                user.getCoins(),
                totalCoinsEarned,
                metricValue
        );
    }

    private Double getOverallScore(User user) {
        return user.getLineupRating() + (user.getTotalChemistry() == null ? 0 : user.getTotalChemistry());
    }

    private List<ChartPointDTO> buildLeaderboardComparisonChart(
            List<UserMetricRowDTO> topOverallUsers,
            List<UserMetricRowDTO> topEarnedCoinsUsers,
            List<UserMetricRowDTO> topValueUsers
    ) {
        Map<String, ChartPointDTO> chart = new LinkedHashMap<>();

        topOverallUsers.stream().limit(5).forEach(user -> chart.put(
                user.getUsername(),
                new ChartPointDTO(user.getUsername(), safeMetric(user.getMetricValue()), null)
        ));

        topEarnedCoinsUsers.stream().limit(5).forEach(user -> chart.merge(
                user.getUsername(),
                new ChartPointDTO(user.getUsername(), safeMetric(user.getMetricValue()), null),
                (existing, incoming) -> {
                    existing.setSecondaryValue(incoming.getValue());
                    return existing;
                }
        ));

        topValueUsers.stream().limit(5).forEach(user -> {
            ChartPointDTO existing = chart.get(user.getUsername());
            if (existing == null) {
                chart.put(user.getUsername(), new ChartPointDTO(user.getUsername(), null, safeMetric(user.getMetricValue())));
            } else if (existing.getSecondaryValue() == null) {
                existing.setSecondaryValue(safeMetric(user.getMetricValue()));
            }
        });

        return new ArrayList<>(chart.values());
    }

    private Double safeMetric(Double value) {
        if (value == null) {
            return 0.0;
        }

        return Math.round(value * 100.0) / 100.0;
    }

    private Double percentageValue(Double ratio) {
        return Math.round(ratio * 10000.0) / 100.0;
    }

    private int normalizeLimit(int limit) {
        return limit <= 10 ? 10 : 20;
    }
}
