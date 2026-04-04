package com.stgsporting.piehmecup.controllers;

import com.stgsporting.piehmecup.entities.Admin;
import com.stgsporting.piehmecup.services.AdminService;
import com.stgsporting.piehmecup.services.InsightsService;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ostaz/insights")
public class AdminInsightsController {
    private final InsightsService insightsService;
    private final AdminService adminService;

    public AdminInsightsController(InsightsService insightsService, AdminService adminService) {
        this.insightsService = insightsService;
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> stats(@RequestParam(required = false) Long levelId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getStatsPage(admin.getSchoolYear(), levelId));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<Object> summary() {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getStatsSummary(admin.getSchoolYear()));
    }

    @GetMapping("/stats/users/overall")
    public ResponseEntity<Object> topOverallUsers(@RequestParam(defaultValue = "10") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopOverallUsers(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/users/coins")
    public ResponseEntity<Object> topEarnedCoinsUsers(@RequestParam(defaultValue = "10") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopEarnedCoinsUsers(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/users/value")
    public ResponseEntity<Object> topValueUsers(@RequestParam(defaultValue = "10") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopValueUsers(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/attendance")
    public ResponseEntity<Object> topAttendanceUsers(@RequestParam(defaultValue = "10") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopAttendanceUsers(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/users/attempted-all")
    public ResponseEntity<Object> usersAttemptedAllQuizzes(@RequestParam @Nullable Integer page) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getUsersAttemptedAllPublishedQuizzes(admin.getSchoolYear(), page));
    }

    @GetMapping("/stats/players/best-sellers")
    public ResponseEntity<Object> bestSellerPlayers(@RequestParam(required = false) Long levelId) {
        return ResponseEntity.ok(insightsService.findBestSeller(levelId));
    }

    @GetMapping("/stats/quizzes/difficulty")
    public ResponseEntity<Object> quizDifficulty() {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getQuizDifficulty(admin.getSchoolYear()));
    }

    @GetMapping("/stats/questions/hardest")
    public ResponseEntity<Object> hardestQuestions(@RequestParam(defaultValue = "10") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestions(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/questions/by-quiz")
    public ResponseEntity<Object> hardestQuestionsByQuiz(@RequestParam(defaultValue = "3") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestionsByQuiz(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/quizzes/{slug}/questions/hardest")
    public ResponseEntity<Object> hardestQuestionsForQuiz(
            @PathVariable String slug,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestionsForQuiz(admin.getSchoolYear(), slug, limit));
    }

    @GetMapping("/stats/questions/{questionId}/distribution")
    public ResponseEntity<Object> questionDistribution(@PathVariable Long questionId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getQuestionDistribution(admin.getSchoolYear(), questionId));
    }
}
