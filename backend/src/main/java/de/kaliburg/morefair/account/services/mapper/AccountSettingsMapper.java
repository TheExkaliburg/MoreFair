package de.kaliburg.morefair.account.services.mapper;

import de.kaliburg.morefair.account.model.AccountSettingsEntity;
import de.kaliburg.morefair.account.model.dto.AccountSettingsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link AccountSettingsEntity AccountSettingsEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class AccountSettingsMapper {

  /**
   * Mapping the {@link AccountSettingsEntity} to a {@link AccountSettingsDto}.
   *
   * @param accountSettings the {@link AccountSettingsEntity}
   * @return the {@link AccountSettingsDto}
   */
  public AccountSettingsDto mapToAccountSettingsDto(AccountSettingsEntity accountSettings) {
    return AccountSettingsDto.builder()
        .vinegarSplit(accountSettings.getVinegarSplit())
        .build();
  }

}
