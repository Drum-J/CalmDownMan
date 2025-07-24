package chimhaha.chimcard.game.dto;

import java.util.List;
import java.util.Map;

public record GameInfoDto(String otherPlayer, List<MyGameCardDto> myCards, Long currentTurnPlayerId,
                          Long player1Id, Long player2Id,
                          Map<Integer, FieldCardDto> fieldCards
) {
}
