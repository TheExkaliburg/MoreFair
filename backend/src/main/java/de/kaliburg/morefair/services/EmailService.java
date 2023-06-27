package de.kaliburg.morefair.services;

public interface EmailService {

  void sendEmail(String to, String subject, String text);

  void sendRegistrationMail(String to, String token);

  void sendPasswordResetMail(String username, String confirmToken);

  void sendChangeEmailMail(String newMail, String confirmToken);
}
