package chimhaha.chimcard.game.event;

public record SurrenderEvent(Long gameRoomId,
                             Long player1Id, Long player2Id,
                             Long gameWinnerId) {
}
