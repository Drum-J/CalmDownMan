package chimhaha.chimcard.game.dto;

public record CardSubmitResponseDto(String message, Long gameRoomId, Long nextTurnPlayer ,Long winnerId) {
}
