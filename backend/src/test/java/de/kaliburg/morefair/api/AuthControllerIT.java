package de.kaliburg.morefair.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.icegreen.greenmail.spring.GreenMailBean;
import de.kaliburg.morefair.MoreFairApplication;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountRepository;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.utils.ITUtils;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes =
    MoreFairApplication.class)
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.properties")
@DBRider
@Slf4j
@WebAppConfiguration
@ContextConfiguration
public class AuthControllerIT {

  @Autowired
  private GreenMailBean greenMailBean;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private SecurityUtils securityUtils;

  @BeforeEach
  public void beforeEach() throws Exception {
    greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
  }

  @Test
  public void registerLogin_default_authenticated() throws Exception {
    String email = "test1@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    String token = registerUser(email, password, ip, xsrfToken);
    confirmRegistrationToken(UUID.fromString(token).toString(), ip);

    String session = login(email, password, ip, xsrfToken);
    AccountEntity account1 = accountRepository.findByUsername(email).orElseThrow();
    assertTrue(passwordEncoder.matches(password, account1.getPassword()));
  }

  @Test
  public void registerLoginChangePasswordLogin_default_authenticated() throws Exception {
    String email = "registerLoginChangePassword_default_authenticated@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    String registrationToken = registerUser(email, password, ip, xsrfToken);
    confirmRegistrationToken(UUID.fromString(registrationToken).toString(), ip);

    String session = login(email, password, ip, xsrfToken);
    String newPassword = SecurityUtils.generatePassword();
    mockMvc.perform(post("/api/auth/password/change")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("oldPassword", password)
            .param("newPassword", newPassword)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken))
            .cookie(new Cookie("SESSION", session)))
        .andExpect(status().isOk())
        .andExpect(content().string("Password changed"));
  }

  @Test
  public void registerForgotPasswordResetLogin_default_authenticated() throws Exception {
    String email = "registerForgotPasswordResetLogin_default_authenticated@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    String registrationToken = registerUser(email, password, ip, xsrfToken);
    confirmRegistrationToken(UUID.fromString(registrationToken).toString(), ip);

    String session = login(email, password, ip, xsrfToken);

    mockMvc.perform(post("/api/auth/password/forgot")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("username", email)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Please look into your inbox for the reset token"))
        .andReturn().getResponse().getContentAsString();

    assertTrue(greenMailBean.getGreenMail().waitForIncomingEmail(2));
    MimeMessage message = ITUtils.getLastGreenMailMessage(greenMailBean);
    String resetToken = message.getContent().toString().split("\n")[1].split("\r")[0];

    String newPassword = SecurityUtils.generatePassword();
    mockMvc.perform(post("/api/auth/password/reset")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("resetToken", resetToken)
            .param("newPassword", newPassword)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isOk())
        .andExpect(content().string("Password changed"))
        .andReturn().getResponse().getContentAsString();


  }


  @Test
  public void register_tooShortPassword_badRequest() throws Exception {
    String email = "registerTooShortPassword@mail.de";
    String password = SecurityUtils.generatePassword().substring(0, 4);
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Password must be at least 8 characters long"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  public void register_tooLongPassword_badRequest() throws Exception {
    String email = "registerTooShortPassword@mail.de";
    String password = SecurityUtils.generatePassword();
    String xsrfToken = getXsrfToken();
    password = password + password + password + password + password + password + password;
    String ip = ITUtils.randomIp();

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Password must be at most 64 characters long"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  public void register_tooLongEmailPassword_badRequest() throws Exception {
    String email = "2wRooaIYpZDfq53aGZqL50Ev4JOKeMxzQTGci03acsam40MK53XFNkPYpIJIofOrFbFpTofgRbgZLWEPZt9BDOgXgBEbLJknnmv0VQIquxY1THTipig1cBfqkPVGduaqZ9C4RPHey5QDHztVdhKql1YpWD62FYBiU9memAjE4nrAGITmm6fcJy23xa9cevCumDi8nEb1OwCXviWuLCSdznwllrhpO6qDPcf6OME8WYzaBJOhSs6rj1GZKqBcd4l@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Email must be at most 254 characters long"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void register_invalidEmail_badRequest() throws Exception {
    String email = "jbneßfq7ß09234";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid email address"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void register_multipleRequestsWithSameIp_statusForbidden() throws Exception {
    String email1 = "test2@mail.de";
    String email2 = "test3@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    registerUser(email1, password, ip, xsrfToken);

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email2)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().string("Too many requests"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void register_multipleRequestsWithSameEmail_badRequest() throws Exception {
    String email = "registerMultipleRequestsWithSameEmail@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();

    String token = registerUser(email, password, ip, xsrfToken);
    confirmRegistrationToken(token, ip);

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ITUtils.randomIp());
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Email address already in use"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  public void registerGuestLogin_default_authenticated() throws Exception {
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    String uuid = registerGuest(ip, xsrfToken);
    String session = login(uuid, uuid, ip, xsrfToken);

    AccountEntity account1 = accountRepository.findByUsername(uuid).orElseThrow();
    assertTrue(passwordEncoder.matches(uuid, account1.getPassword()));
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void registerGuest_multipleRequestsWithSameIp_statusForbidden() throws Exception {
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    final String uuid = registerGuest(ip, xsrfToken);

    mockMvc
        .perform(post("/api/auth/register/guest")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string("Too many requests"))
        .andReturn().getResponse().getContentAsString();

    List<AccountEntity> allAccounts = accountRepository.findAll();
    allAccounts.sort(Comparator.comparing(AccountEntity::getId));

    assertEquals(2, allAccounts.size());
    assertEquals(allAccounts.get(1).getUsername(), uuid);
  }

  @Test
  public void registerLoginAndLogout_default_unauthorized() throws Exception {
    String email = "registerLoginAndRevoke_default_unauthorized@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String xsrfToken = getXsrfToken();
    String registrationToken = registerUser(email, password, ip, xsrfToken);
    confirmRegistrationToken(registrationToken, ip);
    String session = login(email, password, ip, xsrfToken);

    logout(xsrfToken, session);
  }

  @Test
  public void registerGuestUpgradeLogin_default_authenticated() throws Exception {
    String ip = ITUtils.randomIp();
    final String email = "test4@mail.de";
    final String password = SecurityUtils.generatePassword();
    String xsrfToken = getXsrfToken();
    final String uuid = registerGuest(ip, xsrfToken);

    String session0 = login(uuid, uuid, ip, xsrfToken);

    AccountEntity account0 = accountRepository.findByUsername(uuid)
        .orElseThrow();
    assertTrue(passwordEncoder.matches(uuid, account0.getPassword()));

    // Upgrade
    ip = ITUtils.randomIp();
    String registrationToken = upgradeGuestToUser(email, password, uuid, ip, xsrfToken, session0);
    confirmRegistrationToken(UUID.fromString(registrationToken).toString(), ip);

    String session1 = login(email, password, ip, xsrfToken);
    assertNotEquals(session0, session1);

    AccountEntity account1 = accountRepository.findByUsername(email)
        .orElseThrow();
    assertTrue(passwordEncoder.matches(password, account1.getPassword()));
  }

  private String getXsrfToken() throws Exception {
    MockHttpServletResponse response = mockMvc.perform(get("/api/auth/"))
        .andExpect(status().isOk()).andReturn().getResponse();

    return Objects.requireNonNull(response.getCookie("XSRF-TOKEN")).getValue();
  }

  private String registerGuest(String ip, String xsrfToken) throws Exception {
    return mockMvc
        .perform(post("/api/auth/register/guest")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
  }

  private String registerUser(String email, String password, String ip, String xsrfToken)
      throws Exception {
    int emailCount = greenMailBean.getGreenMail().getReceivedMessages().length;
    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Please look into your inbox for a confirmation link"))
        .andReturn().getResponse().getContentAsString();

    assertTrue(greenMailBean.getGreenMail().waitForIncomingEmail(emailCount + 1));
    MimeMessage message = ITUtils.getLastGreenMailMessage(greenMailBean);

    return message.getContent().toString().split("token=")[1].split("\"")[0];
  }

  private String upgradeGuestToUser(String email, String password, String uuid, String ip,
      String xsrfToken, String session) throws Exception {
    int emailCount = greenMailBean.getGreenMail().getReceivedMessages().length;
    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .param("uuid", uuid)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken))
            .cookie(new Cookie("SESSION", session)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Please look into your inbox for a confirmation link"))
        .andReturn().getResponse().getContentAsString();

    assertTrue(greenMailBean.getGreenMail().waitForIncomingEmail(1000, emailCount + 1));
    MimeMessage message = ITUtils.getLastGreenMailMessage(greenMailBean);

    return message.getContent().toString().split("token=")[1].split("\"")[0];
  }

  private void confirmRegistrationToken(String token, String ip) throws Exception {
    mockMvc.perform(get("/api/auth/register/confirm")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .param("token", token))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string("Registration successful; Please log into your account"));
  }

  private String login(String email, String password, String ip, String xsrfToken)
      throws Exception {
    MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password)
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken)))
        .andExpect(status().isOk())
        .andReturn().getResponse();

    return Objects.requireNonNull(response.getCookie("SESSION")).getValue();
  }

  private void logout(String xsrfToken, String session) throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .header("X-XSRF-TOKEN", xsrfToken)
            .cookie(new Cookie("XSRF-TOKEN", xsrfToken))
            .cookie(new Cookie("SESSION", session)))
        .andExpect(status().isFound())
        .andReturn().getResponse();
  }
}
