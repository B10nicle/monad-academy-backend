package com.monadacademy.backend.service.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Verifies SMTP verification email delivery.
 *
 * @author Monad Academy Agent
 */
@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTests {

	@Mock
	JavaMailSender mailSender;

	@Test
	void testSendVerificationEmailWhenCalledShouldSendMimeMessage() throws Exception {
		var sender = new SmtpEmailSender(mailSender);
		var mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
		var messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		sender.sendVerificationEmail("test@example.com", "https://monad.academy/verify?token=123");

		verify(mailSender).send(messageCaptor.capture());
		var sentMessage = messageCaptor.getValue();
		assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo("test@example.com");
		assertThat(sentMessage.getSubject()).isEqualTo("Verify your Monad Academy account");
		assertThat(messageBody(sentMessage)).contains("https://monad.academy/verify?token=123");
	}

	@Test
	void testSendVerificationEmailWhenMailSenderFailsShouldThrowException() {
		var sender = new SmtpEmailSender(mailSender);
		var mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

		var exception = catchThrowableOfType(
				() -> sender.sendVerificationEmail("test@example.com", "https://monad.academy/verify?token=123"),
				IllegalStateException.class);

		assertThat(exception).hasMessage("Email delivery failed");
	}

	private String messageBody(MimeMessage message) throws Exception {
		return contentBody(message.getContent());
	}

	private String contentBody(Object content) throws Exception {
		if (content instanceof MimeMultipart multipart) {
			var body = new StringBuilder();
			for (int index = 0; index < multipart.getCount(); index++) {
				body.append(contentBody(multipart.getBodyPart(index).getContent()));
			}
			return body.toString();
		}
		return content.toString();
	}
}
