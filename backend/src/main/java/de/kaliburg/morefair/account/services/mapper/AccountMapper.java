package de.kaliburg.morefair.account.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.model.dto.AccountDetailsDto;
import de.kaliburg.morefair.account.model.dto.UserListResponse;
import de.kaliburg.morefair.account.model.dto.UserListResponse.User;
import de.kaliburg.morefair.account.services.AccountSettingsService;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link AccountEntity AccountEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class AccountMapper {

  private final LadderService ladderService;
  private final RankerService rankerService;
  private final AccountSettingsService accountSettingsService;
  private final AccountSettingsMapper accountSettingsMapper;
  private final AchievementsService achievementsService;
  private final FairConfig fairConfig;

  /**
   * Mapping the {@link AccountEntity} to a {@link AccountDetailsDto}.
   *
   * @param account the {@link AccountEntity}
   * @return the {@link AccountDetailsDto}
   */
  public AccountDetailsDto mapToAccountDetailsDto(AccountEntity account) {
    int ladderNumber = rankerService.findHighestActiveRankerOfAccount(account)
        .map(r -> ladderService.findLadderById(r.getLadderId()).orElseThrow())
        .map(LadderEntity::getNumber)
        .orElse(1);

    var accountSettings = accountSettingsService.findOrCreateByAccount(account.getId());

    return AccountDetailsDto.builder()
        .uuid(account.getUuid())
        .accountId(account.getId())
        .username(account.getDisplayName())
        .email(account.getUsername())
        .accessRole(account.getAccessRole())
        .highestCurrentLadder(ladderNumber)
        .settings(accountSettingsMapper.mapToAccountSettingsDto(accountSettings))
        .build();
  }

  /**
   * Maps a list of {@link AccountEntity} objects to a {@link UserListResponse} object.
   *
   * @param accounts the list of {@link AccountEntity} objects to be mapped
   * @return a {@link UserListResponse} object containing a list of {@link UserListResponse.User}
   * objects
   */
  public UserListResponse mapToUserList(List<AccountEntity> accounts) {
    List<UserListResponse.User> users = accounts.stream()
        .map(this::mapToUser)
        .toList();

    return UserListResponse.builder()
        .users(users)
        .build();
  }


  /**
   * Maps an {@link AccountEntity} object to a {@link User} object.
   *
   * @param account the {@link AccountEntity} to be mapped
   * @return the mapped {@link User} object
   */
  public User mapToUser(AccountEntity account) {
    AchievementsEntity achievements = achievementsService.findOrCreateByAccountInCurrentSeason(
        account.getId());

    return UserListResponse.User.builder()
        .accountId(account.getId())
        .displayName(account.getDisplayName())
        .tag(fairConfig.getAssholeTag(achievements.getAssholeLevel()))
        .accessRole(account.getAccessRole())
        .build();
  }
}
