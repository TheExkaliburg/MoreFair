package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.model.dto.UserListResponse;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.account.services.mapper.AccountMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The UserController class is responsible for handling user-related API requests.
 */
@Controller
@Slf4j
@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {

  public static final String TOPIC_EVENTS_DESTINATION = "/user/events";
  private final AccountService accountService;
  private final AccountMapper accountMapper;

  /**
   * Retrieves a list of users.
   *
   * <p>This method returns a UserListResponse object containing a list of User objects.
   *
   * @return a UserListResponse object containing a list of User objects
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public UserListResponse getUsers() {
    List<AccountEntity> allActive = accountService.findAllActive();

    return accountMapper.mapToUserList(allActive);
  }

  /**
   * Retrieves the user with the specified ID.
   *
   * @param id the ID of the user to retrieve
   * @return the retrieved user as a {@link UserListResponse.User} object
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public UserListResponse.User getUser(@PathVariable Long id) {
    AccountEntity accountEntity = accountService.findById(id).orElseThrow();

    return accountMapper.mapToUser(accountEntity);
  }
}
