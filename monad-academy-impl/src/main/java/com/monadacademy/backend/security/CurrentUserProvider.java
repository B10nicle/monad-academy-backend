package com.monadacademy.backend.security;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.monadacademy.backend.entity.UserRole;

/**
 * Reads the authenticated user from Spring Security context.
 *
 * @author Monad Academy Agent
 */
@Component
public class CurrentUserProvider {

	public CurrentUser getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			return null;
		}
		return new CurrentUser(UUID.fromString(jwt.getSubject()), UserRole.valueOf(jwt.getClaimAsString("role")));
	}
}
