package com.monadacademy.backend.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

/**
 * Authenticates requests that contain a valid bearer JWT.
 *
 * @author Monad Academy Agent
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenService jwtTokenService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		var header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			var currentUser = jwtTokenService.parse(header.substring(7));
			if (currentUser != null) {
				var authority = new SimpleGrantedAuthority("ROLE_" + currentUser.role().name());
				var authentication = new UsernamePasswordAuthenticationToken(currentUser, null, java.util.List.of(authority));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		filterChain.doFilter(request, response);
	}
}
