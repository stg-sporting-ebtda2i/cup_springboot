package com.stgsporting.piehmecup.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flag")
public class CtfController {

    @GetMapping
    public ResponseEntity<Object> ctfFlag() {
        return ResponseEntity.ok("Oops you got me: U0VSVkVSIEhBQ0tFUg== TEST CHANGES");
    }
}
