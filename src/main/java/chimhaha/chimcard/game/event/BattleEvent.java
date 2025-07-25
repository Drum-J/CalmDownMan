package chimhaha.chimcard.game.event;

import chimhaha.chimcard.game.dto.FieldCardDto;

import java.util.Map;

public record BattleEvent(Long gameRoomId,
                          Long player1Id, Long player2Id,
                          Long currentTurnPlayerId,
                          Long winnerId,
                          Map<Integer, FieldCardDto> player1FieldCards,
                          Map<Integer, FieldCardDto> player2FieldCards,
                          String card1Image, String card2Image,
                          Long gameWinnerId
) {
}
