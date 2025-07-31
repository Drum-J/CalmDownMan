package chimhaha.chimcard.game.event;

public record TimeoutEvent(Long gameRoomId, Long playerId, Long gameWinnerId) {
}
