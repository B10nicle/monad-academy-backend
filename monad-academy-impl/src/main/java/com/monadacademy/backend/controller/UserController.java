package com.monadacademy.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.UserResponse;
import com.monadacademy.backend.service.user.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes user endpoints for authenticated account data.
 *
 * @author Monad Academy Agent
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	UserResponse me() {
		return userService.getCurrentUser();
	}
}
