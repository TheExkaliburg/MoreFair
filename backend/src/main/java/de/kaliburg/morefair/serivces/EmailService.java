package de.kaliburg.morefair.serivces;

public interface EmailService {

  void sendEmail(String to, String subject, String text);

  void sendRegistrationMail(String to, String token);

  void sendPasswordResetMail(String username, String confirmToken);
}
