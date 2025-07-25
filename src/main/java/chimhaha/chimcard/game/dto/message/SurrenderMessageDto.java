package chimhaha.chimcard.game.dto.message;

public record SurrenderMessageDto(String message, Long gameWinnerId) {
    public SurrenderMessageDto(Long gameWinnerId) {
        this("SURRENDER", gameWinnerId);
    }
}
