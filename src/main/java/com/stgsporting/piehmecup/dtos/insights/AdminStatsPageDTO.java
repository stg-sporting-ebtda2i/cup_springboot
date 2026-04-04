package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsPageDTO {
    private StatsSummaryDTO summary;
    private List<UserMetricRowDTO> topOverallUsers;
    private List<UserMetricRowDTO> topEarnedCoinsUsers;
    private List<UserMetricRowDTO> topValueUsers;
    private List<UserMetricRowDTO> topAttendanceUsers;
    private List<QuizDifficultyDTO> hardestQuizzes;
    private List<QuizDifficultyDTO> easiestQuizzes;
    private List<HardestQuestionDTO> hardestQuestions;
    private List<HardestQuestionsByQuizDTO> hardestQuestionsByQuiz;
    private List<BestSellerDTO> bestSellerPlayers;
    private QuestionDistributionDTO mcqDistribution;
    private List<ChartPointDTO> quizDifficultyChart;
    private List<ChartPointDTO> bestSellerChart;
    private List<ChartPointDTO> attendanceChart;
    private List<ChartPointDTO> leaderboardComparisonChart;
}
