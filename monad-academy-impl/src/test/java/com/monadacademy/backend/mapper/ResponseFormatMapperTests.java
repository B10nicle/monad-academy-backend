package com.monadacademy.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Verifies frontend response value formatting.
 *
 * @author Monad Academy Agent
 */
class ResponseFormatMapperTests {

	private final ResponseFormatMapper mapper = new ResponseFormatMapper();

	@Test
	void testFormatInstantShouldUseFrontendPatternWithoutFractionalSeconds() {
		var instant = Instant.parse("2026-05-26T17:58:46.743830Z");

		assertThat(mapper.formatInstant(instant)).isEqualTo("17:58:46 2026-05-26");
	}

	@Test
	void testRoundDurationShouldKeepTwoSignificantDigits() {
		assertThat(mapper.roundDuration(3734L)).isEqualTo(37L);
		assertThat(mapper.roundDuration(3764L)).isEqualTo(38L);
		assertThat(mapper.roundDuration(987L)).isEqualTo(99L);
		assertThat(mapper.roundDuration(42L)).isEqualTo(42L);
		assertThat(mapper.roundDuration(null)).isNull();
	}
}
