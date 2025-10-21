package com.unihub.backend.controller;

import com.unihub.backend.dto.LoginResponse;
import com.unihub.backend.service.GoogleAuthService;
import com.unihub.backend.dto.GoogleLoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthGoogleController {

    private final GoogleAuthService googleAuthService;

    public AuthGoogleController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginGoogle(@RequestBody GoogleLoginRequest body) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(body.getIdToken()));
    }
}