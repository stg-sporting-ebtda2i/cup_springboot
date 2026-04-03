package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.dtos.insights.AdminStatsPageDTO;
import com.stgsporting.piehmecup.dtos.insights.AttemptedAllQuizUserDTO;
import com.stgsporting.piehmecup.dtos.insights.BestSellerDTO;
import com.stgsporting.piehmecup.dtos.insights.ChartPointDTO;
import com.stgsporting.piehmecup.dtos.insights.ChoiceDistributionDTO;
import com.stgsporting.piehmecup.dtos.insights.ChoiceDistributionOptionDTO;
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
import com.stgsporting.piehmecup.dtos.users.UserResponseDTO;
import com.stgsporting.piehmecup.entities.Option;
import com.stgsporting.piehmecup.entities.Question;
import com.stgsporting.piehmecup.entities.Quiz;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.enums.QuestionType;
import com.stgsporting.piehmecup.repositories.InsightsRepository;
import com.stgsporting.piehmecup.repositories.UserRepository;
import net.minidev.json.JSONArray;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
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
        List<BestSellerDTO> bestSellerPlayers = insightsRepository.findBestSeller(levelId).stream()
                .limit(DEFAULT_LIMIT)
                .toList();

        QuizStatsSnapshot quizStats = getQuizStatsSnapshot(schoolYear);

        return new AdminStatsPageDTO(
                new StatsSummaryDTO(
                        Math.toIntExact(userRepository.countBySchoolYear(schoolYear)),
                        quizStats.summary().getQuizzesCount(),
                        quizStats.summary().getQuestionsCount(),
                        insightsRepository.countApprovedAttendancesBySchoolYear(schoolYear).intValue()
                ),
                topOverallUsers,
                topEarnedCoinsUsers,
                topValueUsers,
                topAttendanceUsers,
                quizStats.hardestQuizzes(),
                quizStats.easiestQuizzes(),
                quizStats.hardestQuestions(),
                quizStats.hardestQuestionsByQuiz(),
                bestSellerPlayers,
                quizStats.mcqDistribution(),
                quizStats.hardestQuizzes().stream()
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
        try {
            return quizService.getQuizDifficultyStats(schoolYear);
        } catch (RuntimeException exception) {
            return getQuizDifficulty(getDetailedQuizzes(schoolYear));
        }
    }

    public List<HardestQuestionDTO> getHardestQuestions(SchoolYear schoolYear, int limit) {
        try {
            return quizService.getHardestQuestionsStats(schoolYear, limit);
        } catch (RuntimeException exception) {
            return getHardestQuestions(getDetailedQuizzes(schoolYear), limit);
        }
    }

    public List<HardestQuestionDTO> getHardestQuestionsForQuiz(SchoolYear schoolYear, String slug, int limit) {
        try {
            return quizService.getHardestQuestionsForQuizStats(schoolYear, slug, limit);
        } catch (RuntimeException exception) {
            return getHardestQuestionsForQuiz(getDetailedQuizzes(schoolYear), slug, limit);
        }
    }

    public ChoiceDistributionDTO getQuestionDistribution(SchoolYear schoolYear, Long questionId) {
        try {
            return quizService.getQuestionDistributionStats(schoolYear, questionId);
        } catch (RuntimeException exception) {
            return getQuestionDistribution(getDetailedQuizzes(schoolYear), questionId);
        }
    }

    public PaginationDTO<AttemptedAllQuizUserDTO> getUsersAttemptedAllPublishedQuizzes(SchoolYear schoolYear, Integer page) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        long publishedQuizCount;
        List<EntityQuizAttemptsDTO> attempts;
        try {
            publishedQuizCount = quizService.getPublishedQuizCount(schoolYear);
            attempts = quizService.getAttemptCounts(schoolYear);
        } catch (RuntimeException exception) {
            return new PaginationDTO<>(new PageImpl<>(List.of(), PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE), 0));
        }
        if (publishedQuizCount == 0) {
            return new PaginationDTO<>(new PageImpl<>(List.of(), PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE), 0));
        }

        List<EntityQuizAttemptsDTO> filteredAttempts = attempts.stream()
                .filter(item -> Objects.equals(item.getAttemptedQuizzesCount(), publishedQuizCount))
                .toList();

        if (filteredAttempts.isEmpty()) {
            return new PaginationDTO<>(new PageImpl<>(List.of(), PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE), 0));
        }

        Map<Long, Long> attemptCountsByEntityId = filteredAttempts.stream()
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

        int start = Math.min(pageNumber * ATTEMPTED_ALL_PAGE_SIZE, rows.size());
        int end = Math.min(start + ATTEMPTED_ALL_PAGE_SIZE, rows.size());

        return new PaginationDTO<>(new PageImpl<>(
                rows.subList(start, end),
                PageRequest.of(pageNumber, ATTEMPTED_ALL_PAGE_SIZE),
                rows.size()
        ));
    }

    private QuizStatsSnapshot getQuizStatsSnapshot(SchoolYear schoolYear) {
        try {
            QuizStatsSummaryDTO summary = quizService.getQuizStatsSummary(schoolYear);
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

            return new QuizStatsSnapshot(summary, hardestQuizzes, easiestQuizzes, hardestQuestions, hardestQuestionsByQuiz, mcqDistribution);
        } catch (RuntimeException exception) {
            List<Quiz> quizzes = getDetailedQuizzes(schoolYear);
            List<QuizDifficultyDTO> quizDifficulty = getQuizDifficulty(quizzes);
            List<QuizDifficultyDTO> hardestQuizzes = quizDifficulty.stream().limit(QUIZ_DIFFICULTY_COUNT).toList();
            List<QuizDifficultyDTO> easiestQuizzes = quizDifficulty.stream()
                    .sorted(Comparator.comparing(QuizDifficultyDTO::getAccuracy).reversed()
                            .thenComparing(QuizDifficultyDTO::getSubmissionsCount, Comparator.reverseOrder())
                            .thenComparing(QuizDifficultyDTO::getQuizId))
                    .limit(QUIZ_DIFFICULTY_COUNT)
                    .toList();
            List<HardestQuestionDTO> hardestQuestions = getHardestQuestions(quizzes, DEFAULT_LIMIT);
            List<HardestQuestionsByQuizDTO> hardestQuestionsByQuiz = quizzes.stream()
                    .map(quiz -> new HardestQuestionsByQuizDTO(
                            quiz.getId(),
                            quiz.getSlug(),
                            quiz.getName(),
                            getHardestQuestionsForQuiz(quizzes, quiz.getSlug(), PER_QUIZ_QUESTIONS_COUNT)
                    ))
                    .filter(entry -> !entry.getQuestions().isEmpty())
                    .toList();
            ChoiceDistributionDTO mcqDistribution = hardestQuestions.isEmpty()
                    ? null
                    : getQuestionDistribution(quizzes, hardestQuestions.get(0).getQuestionId());

            return new QuizStatsSnapshot(
                    new QuizStatsSummaryDTO(
                            quizzes.size(),
                            quizzes.stream().mapToInt(quiz -> quiz.getQuestions() == null ? 0 : quiz.getQuestions().size()).sum()
                    ),
                    hardestQuizzes,
                    easiestQuizzes,
                    hardestQuestions,
                    hardestQuestionsByQuiz,
                    mcqDistribution
            );
        }
    }

    private List<Quiz> getDetailedQuizzes(SchoolYear schoolYear) {
        return quizService.getQuizzes(schoolYear, null).stream()
                .map(quiz -> quizService.getQuizBySlug(quiz.getSlug(), schoolYear, true, true))
                .toList();
    }

    private List<QuizDifficultyDTO> getQuizDifficulty(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::toQuizDifficulty)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(QuizDifficultyDTO::getAccuracy)
                        .thenComparing(QuizDifficultyDTO::getSubmissionsCount, Comparator.reverseOrder())
                        .thenComparing(QuizDifficultyDTO::getQuizId))
                .toList();
    }

    private QuizDifficultyDTO toQuizDifficulty(Quiz quiz) {
        List<UserResponseDTO> responses = quiz.getResponses();
        if (responses == null || responses.size() < 3) {
            return null;
        }

        long totalAnswers = 0;
        long correctAnswers = 0;
        for (UserResponseDTO response : responses) {
            if (response.getAnswers() == null) {
                continue;
            }

            totalAnswers += response.getAnswers().size();
            correctAnswers += response.getAnswers().values().stream()
                    .filter(UserResponseDTO.Answer::getIsCorrect)
                    .count();
        }

        if (totalAnswers == 0) {
            return null;
        }

        return new QuizDifficultyDTO(
                quiz.getId(),
                quiz.getSlug(),
                quiz.getName(),
                (long) responses.size(),
                totalAnswers,
                correctAnswers,
                correctAnswers * 1.0 / totalAnswers
        );
    }

    private List<HardestQuestionDTO> getHardestQuestions(List<Quiz> quizzes, int limit) {
        Map<Long, HardestQuestionDTO> aggregated = new LinkedHashMap<>();

        for (Quiz quiz : quizzes) {
            if (quiz.getQuestions() == null || quiz.getResponses() == null) {
                continue;
            }

            for (Question question : quiz.getQuestions()) {
                long attempts = 0;
                long correctAnswers = 0;

                for (UserResponseDTO response : quiz.getResponses()) {
                    if (response.getAnswers() == null) {
                        continue;
                    }

                    UserResponseDTO.Answer answer = response.getAnswers().get(String.valueOf(question.getId()));
                    if (answer == null) {
                        continue;
                    }

                    attempts++;
                    if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                        correctAnswers++;
                    }
                }

                if (attempts < 3) {
                    continue;
                }

                aggregated.put(question.getId(), new HardestQuestionDTO(
                        quiz.getId(),
                        quiz.getSlug(),
                        quiz.getName(),
                        question.getId(),
                        question.getTitle(),
                        question.getType() == null ? null : question.getType().name(),
                        attempts,
                        correctAnswers,
                        correctAnswers * 1.0 / attempts
                ));
            }
        }

        return aggregated.values().stream()
                .sorted(Comparator.comparing(HardestQuestionDTO::getAccuracy)
                        .thenComparing(HardestQuestionDTO::getAttempts, Comparator.reverseOrder())
                        .thenComparing(HardestQuestionDTO::getQuestionId))
                .limit(normalizeLimit(limit))
                .toList();
    }

    private List<HardestQuestionDTO> getHardestQuestionsForQuiz(List<Quiz> quizzes, String slug, int limit) {
        Quiz quiz = quizzes.stream()
                .filter(candidate -> Objects.equals(candidate.getSlug(), slug))
                .findFirst()
                .orElse(null);

        if (quiz == null || quiz.getQuestions() == null || quiz.getResponses() == null) {
            return List.of();
        }

        return getHardestQuestions(List.of(quiz), limit);
    }

    private ChoiceDistributionDTO getQuestionDistribution(List<Quiz> quizzes, Long questionId) {
        for (Quiz quiz : quizzes) {
            if (quiz.getQuestions() == null || quiz.getResponses() == null) {
                continue;
            }

            for (Question question : quiz.getQuestions()) {
                if (!Objects.equals(question.getId(), questionId) || !isMcq(question)) {
                    continue;
                }

                Map<Long, Long> counts = new LinkedHashMap<>();
                for (Option option : question.getOptions()) {
                    counts.put(option.getOrder(), 0L);
                }

                long totalResponses = 0;
                for (UserResponseDTO response : quiz.getResponses()) {
                    if (response.getAnswers() == null) {
                        continue;
                    }

                    UserResponseDTO.Answer answer = response.getAnswers().get(String.valueOf(questionId));
                    if (answer == null) {
                        continue;
                    }

                    List<Long> selections = normalizeToLongList(answer.getAnswer());
                    if (selections.isEmpty()) {
                        continue;
                    }

                    totalResponses++;
                    for (Long selection : selections) {
                        counts.computeIfPresent(selection, (key, current) -> current + 1);
                    }
                }

                List<Long> correctAnswers = normalizeToLongList(question.getAnswers());
                final long responsesCount = totalResponses;
                List<ChoiceDistributionOptionDTO> options = question.getOptions().stream()
                        .map(option -> new ChoiceDistributionOptionDTO(
                                option.getId(),
                                option.getName(),
                                option.getOrder(),
                                counts.getOrDefault(option.getOrder(), 0L),
                                responsesCount == 0 ? 0.0 : counts.getOrDefault(option.getOrder(), 0L) * 1.0 / responsesCount,
                                correctAnswers.contains(option.getOrder())
                        ))
                        .toList();

                return new ChoiceDistributionDTO(
                        quiz.getId(),
                        quiz.getSlug(),
                        quiz.getName(),
                        question.getId(),
                        question.getTitle(),
                        totalResponses,
                        options
                );
            }
        }

        return null;
    }

    private boolean isMcq(Question question) {
        return question.getType() == QuestionType.Choice || question.getType() == QuestionType.MultipleCorrectChoices;
    }

    private List<Long> normalizeToLongList(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }

        if (rawValue instanceof JSONArray jsonArray) {
            List<Long> values = new ArrayList<>();
            for (Object value : jsonArray) {
                parseLong(value).ifPresent(values::add);
            }
            return values;
        }

        if (rawValue instanceof Collection<?> collection) {
            List<Long> values = new ArrayList<>();
            for (Object value : collection) {
                parseLong(value).ifPresent(values::add);
            }
            return values;
        }

        return parseLong(rawValue).map(List::of).orElse(List.of());
    }

    private java.util.Optional<Long> parseLong(Object value) {
        if (value == null) {
            return java.util.Optional.empty();
        }

        if (value instanceof Number number) {
            return java.util.Optional.of(number.longValue());
        }

        if (value instanceof String stringValue) {
            try {
                return java.util.Optional.of(Long.parseLong(stringValue));
            } catch (NumberFormatException ignored) {
                return java.util.Optional.empty();
            }
        }

        return java.util.Optional.empty();
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

    private record QuizStatsSnapshot(
            QuizStatsSummaryDTO summary,
            List<QuizDifficultyDTO> hardestQuizzes,
            List<QuizDifficultyDTO> easiestQuizzes,
            List<HardestQuestionDTO> hardestQuestions,
            List<HardestQuestionsByQuizDTO> hardestQuestionsByQuiz,
            ChoiceDistributionDTO mcqDistribution
    ) {
    }
}
