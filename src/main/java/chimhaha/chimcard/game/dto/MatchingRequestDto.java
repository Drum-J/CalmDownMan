package chimhaha.chimcard.game.dto;

import java.util.List;

public record MatchingRequestDto(Long playerId, List<Long> cardIds) {

}
