package com.monadacademy.backend.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.AuthResponse;
import com.monadacademy.backend.dto.LoginRequest;
import com.monadacademy.backend.dto.MessageResponse;
import com.monadacademy.backend.dto.RegisterRequest;
import com.monadacademy.backend.dto.ResendVerificationRequest;
import com.monadacademy.backend.dto.VerifyEmailRequest;
import com.monadacademy.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	MessageResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/verify-email")
	MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		return authService.verifyEmail(request);
	}

	@PostMapping("/resend-verification")
	MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
		return authService.resendVerification(request);
	}

	@PostMapping("/login")
	AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
