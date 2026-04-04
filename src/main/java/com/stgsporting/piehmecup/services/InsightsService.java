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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class InsightsService {
    private static final Logger log = LoggerFactory.getLogger(InsightsService.class);
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int QUIZ_DIFFICULTY_COUNT = 5;
    private static final int PER_QUIZ_QUESTIONS_COUNT = 3;

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
        long startedAt = System.currentTimeMillis();
        try {
            List<UserMetricRowDTO> topOverallUsers = getTopOverallUsers(schoolYear, 0, DEFAULT_LIMIT).getData();
            List<UserMetricRowDTO> topEarnedCoinsUsers = getTopEarnedCoinsUsers(schoolYear, 0, DEFAULT_LIMIT).getData();
            List<UserMetricRowDTO> topValueUsers = getTopValueUsers(schoolYear, 0, DEFAULT_LIMIT).getData();
            List<UserMetricRowDTO> topAttendanceUsers = getTopAttendanceUsers(schoolYear, 0, DEFAULT_LIMIT).getData();
            List<BestSellerDTO> bestSellerPlayers = insightsRepository.findBestSeller(levelId).stream()
                    .limit(DEFAULT_LIMIT)
                    .toList();
            StatsSummaryDTO summary = getStatsSummary(schoolYear);
            List<QuizDifficultyDTO> quizDifficulty = getQuizDifficulty(schoolYear);
            List<QuizDifficultyDTO> hardestQuizzes = quizDifficulty.stream().limit(QUIZ_DIFFICULTY_COUNT).toList();
            List<QuizDifficultyDTO> easiestQuizzes = quizDifficulty.stream()
                    .sorted(Comparator.comparing(QuizDifficultyDTO::getAccuracy).reversed()
                            .thenComparing(QuizDifficultyDTO::getSubmissionsCount, Comparator.reverseOrder())
                            .thenComparing(QuizDifficultyDTO::getQuizId))
                    .limit(QUIZ_DIFFICULTY_COUNT)
                    .toList();
            List<HardestQuestionDTO> hardestQuestions = getHardestQuestions(schoolYear, 0, DEFAULT_LIMIT).getData();
            List<HardestQuestionsByQuizDTO> hardestQuestionsByQuiz = getHardestQuestionsByQuiz(schoolYear, PER_QUIZ_QUESTIONS_COUNT);
            ChoiceDistributionDTO mcqDistribution = hardestQuestions.isEmpty()
                    ? null
                    : getQuestionDistribution(schoolYear, hardestQuestions.get(0).getQuestionId());

            return new AdminStatsPageDTO(
                    summary,
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
        } finally {
            log.info(
                    "Insights getStatsPage completed in {} ms for schoolYear={} levelId={}",
                    System.currentTimeMillis() - startedAt,
                    schoolYear.getSlug(),
                    levelId
            );
        }
    }

    public StatsSummaryDTO getStatsSummary(SchoolYear schoolYear) {
        QuizStatsSummaryDTO quizSummary = runQuizStatsCall(
                "getQuizStatsSummary",
                schoolYear,
                () -> quizService.getQuizStatsSummary(schoolYear),
                new QuizStatsSummaryDTO(0, 0)
        );

        return new StatsSummaryDTO(
                Math.toIntExact(userRepository.countBySchoolYear(schoolYear)),
                quizSummary.getQuizzesCount(),
                quizSummary.getQuestionsCount(),
                insightsRepository.countApprovedAttendancesBySchoolYear(schoolYear).intValue()
        );
    }

    public PaginationDTO<UserMetricRowDTO> getTopOverallUsers(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        List<UserMetricRowDTO> rows = mapUserCoins(
                insightsRepository.findTopUsersByOverallScore(schoolYear, pageRequest),
                row -> getOverallScore(row.getUser()),
                UserCoinsDTO::getCoins,
                pageRequest.getOffset() + 1
        );
        return toPagination(rows, pageRequest, userRepository.countBySchoolYear(schoolYear));
    }

    public PaginationDTO<UserMetricRowDTO> getTopEarnedCoinsUsers(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        List<UserMetricRowDTO> rows = mapUserCoins(
                userRepository.findTopUsersBySchoolYearAndCoins(schoolYear, pageRequest),
                row -> row.getCoins().doubleValue(),
                UserCoinsDTO::getCoins,
                pageRequest.getOffset() + 1
        );
        return toPagination(rows, pageRequest, userRepository.countBySchoolYear(schoolYear));
    }

    public PaginationDTO<UserMetricRowDTO> getTopValueUsers(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        List<UserSpendValueDTO> users = insightsRepository.findTopUsersByValue(schoolYear, pageRequest);
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = pageRequest.getOffset() + 1;
        for (UserSpendValueDTO row : users) {
            rows.add(toUserMetricRow(rank++, row.getUser(), row.getMetricValue(), 0));
        }
        return toPagination(rows, pageRequest, insightsRepository.countTopUsersByValue(schoolYear));
    }

    public PaginationDTO<UserMetricRowDTO> getTopAttendanceUsers(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        List<UserLongMetricDTO> users = insightsRepository.findTopUsersByApprovedAttendances(schoolYear, pageRequest);
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = pageRequest.getOffset() + 1;
        for (UserLongMetricDTO row : users) {
            rows.add(toUserMetricRow(rank++, row.getUser(), row.getMetricValue().doubleValue(), 0));
        }
        return toPagination(rows, pageRequest, insightsRepository.countTopUsersByApprovedAttendances(schoolYear));
    }

    public List<QuizDifficultyDTO> getQuizDifficulty(SchoolYear schoolYear) {
        return runQuizStatsCall("getQuizDifficultyStats", schoolYear, () -> quizService.getQuizDifficultyStats(schoolYear), List.of());
    }

    public PaginationDTO<HardestQuestionDTO> getHardestQuestions(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        return runQuizStatsCall(
                "getHardestQuestionsStats",
                schoolYear,
                () -> quizService.getHardestQuestionsStats(schoolYear, pageRequest.getPageNumber(), pageRequest.getPageSize()),
                emptyPagination(pageRequest)
        );
    }

    public List<HardestQuestionsByQuizDTO> getHardestQuestionsByQuiz(SchoolYear schoolYear, int limit) {
        return runQuizStatsCall(
                "getHardestQuestionsByQuizStats",
                schoolYear,
                () -> quizService.getHardestQuestionsByQuizStats(schoolYear, limit),
                List.of()
        );
    }

    public PaginationDTO<HardestQuestionDTO> getHardestQuestionsForQuiz(SchoolYear schoolYear, String slug, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        return runQuizStatsCall(
                "getHardestQuestionsForQuizStats",
                schoolYear,
                () -> quizService.getHardestQuestionsForQuizStats(schoolYear, slug, pageRequest.getPageNumber(), pageRequest.getPageSize()),
                emptyPagination(pageRequest)
        );
    }

    public ChoiceDistributionDTO getQuestionDistribution(SchoolYear schoolYear, Long questionId) {
        return runQuizStatsCall(
                "getQuestionDistributionStats",
                schoolYear,
                () -> quizService.getQuestionDistributionStats(schoolYear, questionId),
                null
        );
    }

    public PaginationDTO<BestSellerDTO> getBestSellerPage(Long levelId, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        List<BestSellerDTO> allPlayers = insightsRepository.findBestSeller(levelId);
        List<BestSellerDTO> pageItems = slicePage(allPlayers, pageRequest);
        return toPagination(pageItems, pageRequest, insightsRepository.countBestSeller(levelId));
    }

    public PaginationDTO<AttemptedAllQuizUserDTO> getUsersAttemptedAllPublishedQuizzes(SchoolYear schoolYear, Integer page, Integer size) {
        PageRequest pageRequest = buildPageRequest(page, size);
        long publishedQuizCount;
        List<EntityQuizAttemptsDTO> attempts;
        try {
            publishedQuizCount = quizService.getPublishedQuizCount(schoolYear);
            attempts = quizService.getAttemptCounts(schoolYear);
        } catch (RuntimeException exception) {
            return emptyPagination(pageRequest);
        }
        if (publishedQuizCount == 0) {
            return emptyPagination(pageRequest);
        }

        List<EntityQuizAttemptsDTO> filteredAttempts = attempts.stream()
                .filter(item -> Objects.equals(item.getAttemptedQuizzesCount(), publishedQuizCount))
                .toList();

        if (filteredAttempts.isEmpty()) {
            return emptyPagination(pageRequest);
        }

        Map<Long, Long> attemptCountsByEntityId = filteredAttempts.stream()
                .collect(java.util.stream.Collectors.toMap(EntityQuizAttemptsDTO::getEntityId, EntityQuizAttemptsDTO::getAttemptedQuizzesCount));

        List<User> users = userRepository.findBySchoolYearAndQuizIdIn(schoolYear, new ArrayList<>(attemptCountsByEntityId.keySet()));
        Map<Long, User> usersByQuizId = users.stream()
                .filter(user -> user.getQuizId() != null)
                .collect(java.util.stream.Collectors.toMap(User::getQuizId, Function.identity()));

        List<UserCoinsDTO> filteredUsersWithCoins = users.isEmpty()
                ? Collections.emptyList()
                : insightsRepository.findUserCoinsByIds(users.stream().map(User::getId).toList());
        Map<Long, Long> totalCoinsByUserId = filteredUsersWithCoins.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row.getUser().getId(),
                        UserCoinsDTO::getCoins,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<AttemptedAllQuizUserDTO> rows = filteredAttempts.stream()
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

        return toPagination(slicePage(rows, pageRequest), pageRequest, rows.size());
    }

    private List<UserMetricRowDTO> mapUserCoins(
            List<UserCoinsDTO> users,
            Function<UserCoinsDTO, Double> metric,
            Function<UserCoinsDTO, Long> totalCoinsEarned,
            long startingRank
    ) {
        List<UserMetricRowDTO> rows = new ArrayList<>();
        long rank = startingRank;
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

    private PageRequest buildPageRequest(Integer page, Integer size) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null || size <= 0 ? DEFAULT_LIMIT : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(pageNumber, pageSize);
    }

    private <T> PaginationDTO<T> toPagination(List<T> rows, PageRequest pageRequest, long total) {
        return new PaginationDTO<>(new PageImpl<>(rows, pageRequest, total));
    }

    private <T> PaginationDTO<T> emptyPagination(PageRequest pageRequest) {
        return toPagination(List.of(), pageRequest, 0);
    }

    private <T> List<T> slicePage(List<T> rows, PageRequest pageRequest) {
        int start = (int) Math.min(pageRequest.getOffset(), rows.size());
        int end = Math.min(start + pageRequest.getPageSize(), rows.size());
        return rows.subList(start, end);
    }

    private <T> T runQuizStatsCall(String label, SchoolYear schoolYear, Supplier<T> supplier, T fallback) {
        long startedAt = System.currentTimeMillis();
        try {
            T result = supplier.get();
            log.info(
                    "Insights {} completed in {} ms for schoolYear={}",
                    label,
                    System.currentTimeMillis() - startedAt,
                    schoolYear.getSlug()
            );
            return result;
        } catch (RuntimeException exception) {
            log.warn(
                    "Insights {} failed in {} ms for schoolYear={}. Returning lightweight fallback instead of hydrating full quizzes. Cause={}",
                    label,
                    System.currentTimeMillis() - startedAt,
                    schoolYear.getSlug(),
                    exception.getMessage()
            );
            return fallback;
        }
    }
}
