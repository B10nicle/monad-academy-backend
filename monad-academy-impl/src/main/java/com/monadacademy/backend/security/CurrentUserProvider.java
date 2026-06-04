package com.monadacademy.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.monadacademy.backend.entity.UserRole;

import lombok.extern.slf4j.Slf4j;

/**
 * Reads the authenticated user from Spring Security context.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Component
public class CurrentUserProvider {

	public CurrentUser getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			log.debug("No JWT principal found in security context");
			return null;
		}
		var currentUser = new CurrentUser(Long.valueOf(jwt.getSubject()), UserRole.valueOf(jwt.getClaimAsString("role")));
		log.debug("Resolved current user id={} role={}", currentUser.id(), currentUser.role());
		return currentUser;
	}
}
