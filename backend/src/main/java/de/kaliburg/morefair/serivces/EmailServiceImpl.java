package de.kaliburg.morefair.serivces;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender emailSender;

  @Override
  public void sendEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("noreply@kaliburg.de");
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    try {
      emailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send email from \"noreply@kaliburg.de\" with the subject \"{}\"",
          subject, e);
    }
  }

  @Override
  public void sendRegistrationMail(String to, String token) {
    String fullToken = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/auth/register/confirm").queryParam("token", token).toUriString()).toString();
    sendEmail(to, "Registration at FairGame",
        "Hello, thanks for registering at FairGame. Please click on the following link to "
            + "activate your account:\n"
            + fullToken);
  }
}
