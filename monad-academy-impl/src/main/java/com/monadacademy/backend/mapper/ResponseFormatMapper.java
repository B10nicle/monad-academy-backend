package com.monadacademy.backend.mapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Formats backend values for frontend API responses.
 *
 * @author Monad Academy Agent
 */
@Component
public class ResponseFormatMapper {

	private static final DateTimeFormatter RESPONSE_DATE_TIME_FORMATTER =
			DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd").withZone(ZoneOffset.UTC);

	@Named("formatInstant")
	public String formatInstant(Instant instant) {
		if (instant == null) {
			return null;
		}
		return RESPONSE_DATE_TIME_FORMATTER.format(instant);
	}

	@Named("roundDuration")
	public Long roundDuration(Long durationMs) {
		if (durationMs == null || durationMs < 100) {
			return durationMs;
		}

		var digits = (int) Math.floor(Math.log10(durationMs)) + 1;
		var divisor = (long) Math.pow(10, digits - 2);
		return Math.round((double) durationMs / divisor);
	}
}
