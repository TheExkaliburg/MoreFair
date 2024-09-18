package de.kaliburg.morefair.moderation.events.services;

import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;
import java.util.List;

public interface NameChangeService {

  NameChangeEntity updateDisplayName(Long accountId, String displayName);

  List<NameChangeEntity> listAllNameChangesTo(String displayName);

  List<NameChangeEntity> listAllNameChangesOf(Long accountId);
}
