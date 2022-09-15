package de.kaliburg.morefair.api;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.icegreen.greenmail.spring.GreenMailBean;
import de.kaliburg.morefair.MoreFairApplication;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountRepository;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.utils.ITUtils;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
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

  @Test
  public void registerLoginRefresh_default_authenticated() throws Exception {
    String email = "test1@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();
    String token = registerUser(email, password, ip);
    confirmRegistrationToken(UUID.fromString(token).toString(), ip);

    HashMap<String, String> jwtTokens1 = login(email, password, ip);
    DecodedJWT accessToken1 = securityUtils.verifyToken(jwtTokens1.get("accessToken"));

    AccountEntity account1 = accountRepository.findByUsername(accessToken1.getSubject())
        .orElseThrow();
    assertTrue(passwordEncoder.matches(password, account1.getPassword()));

    HashMap<String, String> jwtTokens2 = refreshTokens(jwtTokens1.get("refreshToken"), ip);
    DecodedJWT accessToken2 = securityUtils.verifyToken(jwtTokens2.get("accessToken"));

    AccountEntity account2 = accountRepository.findByUsername(accessToken2.getSubject())
        .orElseThrow();
    assertTrue(passwordEncoder.matches(password, account2.getPassword()));
    assertEquals(accessToken1.getSubject(), accessToken2.getSubject());
    assertEquals(accessToken1.getClaim("roles").toString(),
        accessToken2.getClaim("roles").toString());
    assertEquals(accessToken1.getHeader(), accessToken2.getHeader());
    assertEquals(accessToken1.getAlgorithm(), accessToken2.getAlgorithm());
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void register_multipleRequestsWithSameIp_statusForbidden() throws Exception {
    String email1 = "test2@mail.de";
    String email2 = "test3@mail.de";
    String password = SecurityUtils.generatePassword();
    String ip = ITUtils.randomIp();

    registerUser(email1, password, ip);

    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email2)
            .param("password", password))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().string("Too many requests"))
        .andReturn().getResponse().getContentAsString();
  }

  @Test
  public void registerGuestLoginRefresh_default_authenticated() throws Exception {
    String ip = ITUtils.randomIp();
    String result = registerGuest(ip);

    AccountEntity account = accountRepository.findByUsername(UUID.fromString(result).toString())
        .orElseThrow();
    assertNotNull(account);
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void registerGuest_multipleRequestsWithSameIp_statusForbidden() throws Exception {
    String ip = ITUtils.randomIp();

    final String result = registerGuest(ip);

    mockMvc
        .perform(post("/api/auth/register/guest").with(request -> {
          request.setRemoteAddr(ip);
          return request;
        }))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string("Too many requests"))
        .andReturn().getResponse().getContentAsString();

    List<AccountEntity> allAccounts = accountRepository.findAll();
    allAccounts.sort(Comparator.comparing(AccountEntity::getId));

    assertEquals(2, allAccounts.size());
    assertEquals(allAccounts.get(1).getUsername(), result);
  }

  private String registerGuest(String ip) throws Exception {
    return mockMvc
        .perform(post("/api/auth/register/guest")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            }))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
  }

  private String registerUser(String email, String password, String ip) throws Exception {
    mockMvc
        .perform(post("/api/auth/register")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password))
        .andExpect(status().isCreated())
        .andExpect(content().string("Please look into your inbox for a confirmation link"))
        .andReturn().getResponse().getContentAsString();

    assertTrue(greenMailBean.getGreenMail().waitForIncomingEmail(1000, 1));
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

  private HashMap<String, String> login(String email, String password, String ip) throws Exception {
    String content = mockMvc.perform(post("/api/auth/login")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", email)
            .param("password", password))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", aMapWithSize(2)))
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        .andReturn().getResponse().getContentAsString();

    Type type = new TypeToken<HashMap<String, String>>() {
    }.getType();
    HashMap<String, String> result = new Gson().fromJson(content, type);
    result.values().forEach(t -> securityUtils.verifyToken(t));
    return result;
  }

  private HashMap<String, String> refreshTokens(String token, String ip) throws Exception {
    String content = mockMvc.perform(get("/api/auth/refresh")
            .with(request -> {
              request.setRemoteAddr(ip);
              return request;
            })
            .header("authorization", "Bearer " + token))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$", aMapWithSize(2)))
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        .andReturn().getResponse().getContentAsString();

    Type type = new TypeToken<HashMap<String, String>>() {
    }.getType();
    HashMap<String, String> result = new Gson().fromJson(content, type);
    result.values().forEach(t -> securityUtils.verifyToken(t));
    return result;
  }
}
