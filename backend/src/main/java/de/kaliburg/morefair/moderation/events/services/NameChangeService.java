package de.kaliburg.morefair.moderation.events.services;

import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;

public interface NameChangeService {

  NameChangeEntity updateDisplayName(Long accountId, String displayName);
}
