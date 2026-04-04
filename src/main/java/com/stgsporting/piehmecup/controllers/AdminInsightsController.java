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
    public ResponseEntity<Object> topOverallUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopOverallUsers(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/users/coins")
    public ResponseEntity<Object> topEarnedCoinsUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopEarnedCoinsUsers(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/users/value")
    public ResponseEntity<Object> topValueUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopValueUsers(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/attendance")
    public ResponseEntity<Object> topAttendanceUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getTopAttendanceUsers(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/users/attempted-all")
    public ResponseEntity<Object> usersAttemptedAllQuizzes(
            @RequestParam @Nullable Integer page,
            @RequestParam @Nullable Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getUsersAttemptedAllPublishedQuizzes(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/players/best-sellers")
    public ResponseEntity<Object> bestSellerPlayers(
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(insightsService.getBestSellerPage(levelId, page, size));
    }

    @GetMapping("/stats/quizzes/difficulty")
    public ResponseEntity<Object> quizDifficulty() {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getQuizDifficulty(admin.getSchoolYear()));
    }

    @GetMapping("/stats/questions/hardest")
    public ResponseEntity<Object> hardestQuestions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestions(admin.getSchoolYear(), page, size));
    }

    @GetMapping("/stats/questions/by-quiz")
    public ResponseEntity<Object> hardestQuestionsByQuiz(@RequestParam(defaultValue = "3") Integer limit) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestionsByQuiz(admin.getSchoolYear(), limit));
    }

    @GetMapping("/stats/quizzes/{slug}/questions/hardest")
    public ResponseEntity<Object> hardestQuestionsForQuiz(
            @PathVariable String slug,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getHardestQuestionsForQuiz(admin.getSchoolYear(), slug, page, size));
    }

    @GetMapping("/stats/questions/{questionId}/distribution")
    public ResponseEntity<Object> questionDistribution(@PathVariable Long questionId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        return ResponseEntity.ok(insightsService.getQuestionDistribution(admin.getSchoolYear(), questionId));
    }
}
