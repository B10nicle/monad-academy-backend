package com.monadacademy.backend.service;

public interface EmailSender {

	void sendVerificationEmail(String email, String verificationLink);
}
