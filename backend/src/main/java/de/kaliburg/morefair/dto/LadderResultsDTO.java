package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.game.ladder.LadderEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LadderResultsDTO {

  private Map<Integer, List<RankerPrivateDTO>> allLadders = new HashMap<>();

  public LadderResultsDTO(Map<Integer, LadderEntity> ladders) {
    ladders.forEach((integer, ladder) -> {
      List<RankerPrivateDTO> allRankers = new ArrayList<>();
      ladder.getRankers().forEach(ranker -> {
        RankerPrivateDTO dto = ranker.convertToPrivateDto();
        allRankers.add(dto);
      });

      allLadders.put(integer, allRankers);
    });
  }
}
