package com.stgsporting.piehmecup.controllers;

import com.stgsporting.piehmecup.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderboardController {
    @Autowired
    private UserService userService;

    @GetMapping("/leaderboard")
    public ResponseEntity<Object> getLeaderboard() {
        return ResponseEntity.ok().body(userService.getLeaderboard());
    }

}
