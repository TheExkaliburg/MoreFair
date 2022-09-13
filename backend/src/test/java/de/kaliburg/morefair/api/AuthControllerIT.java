package de.kaliburg.morefair.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import de.kaliburg.morefair.MoreFairApplication;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountRepository;
import de.kaliburg.morefair.utils.ITUtils;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MoreFairApplication.class)
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.properties")
@DBRider
@Slf4j
public class AuthControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AccountRepository accountRepository;

  @Test
  public void registerGuest_default_registered() throws Exception {
    String result = mockMvc.perform(post("/api/auth/register/guest").with(request -> {
          request.setRemoteAddr(ITUtils.randomIp());
          return request;
        }))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();

    accountRepository.findByUsername(UUID.fromString(result).toString())
        .orElseThrow();
  }

  @Test
  @DataSet(cleanBefore = true, value = "yml/datasets/data_initial.yml")
  public void registerGuest_multipleRequestsWithSameIp_statusForbidden() throws Exception {
    String ipAddress = ITUtils.randomIp();

    final String result = mockMvc
        .perform(post("/api/auth/register/guest").with(request -> {
          request.setRemoteAddr(ipAddress);
          return request;
        }))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();

    mockMvc
        .perform(post("/api/auth/register/guest").with(request -> {
          request.setRemoteAddr(ipAddress);
          return request;
        }))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();

    List<AccountEntity> allAccounts = accountRepository.findAll();
    allAccounts.sort(Comparator.comparing(AccountEntity::getId));
    assertEquals(2, allAccounts.size());
    assertEquals(allAccounts.get(1).getUsername(), result);
  }

}
