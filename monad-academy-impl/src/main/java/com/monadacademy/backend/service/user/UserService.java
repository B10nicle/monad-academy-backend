package com.monadacademy.backend.service.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.dto.UserResponse;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads data for the currently authenticated user.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final CurrentUserProvider currentUserProvider;

	@Transactional(readOnly = true)
	public UserResponse getCurrentUser() {
		var currentUser = currentUserProvider.getCurrentUser();
		if (currentUser == null) {
			throw new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
		}
		var user = userRepository.findById(currentUser.id())
				.orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
		log.debug("Loaded current user id={} role={} status={}", user.getId(), user.getRole(), user.getStatus());
		return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getStatus());
	}
}
