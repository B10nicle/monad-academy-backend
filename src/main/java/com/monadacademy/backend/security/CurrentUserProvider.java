package com.monadacademy.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the authenticated user from Spring Security context.
 *
 * @author Monad Academy Agent
 */
@Component
public class CurrentUserProvider {

	public CurrentUser getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
			return null;
		}
		return currentUser;
	}
}
