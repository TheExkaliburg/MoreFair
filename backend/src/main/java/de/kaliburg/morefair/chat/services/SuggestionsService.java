package de.kaliburg.morefair.chat.services;

import de.kaliburg.morefair.chat.model.dto.SuggestionDto;
import java.util.List;

public interface SuggestionsService {

  List<SuggestionDto> getAllSuggestions();
}
