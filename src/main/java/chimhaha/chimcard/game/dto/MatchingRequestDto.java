package chimhaha.chimcard.game.dto;

import chimhaha.chimcard.entity.GameRoom;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record MatchingRequestDto(Long playerId, List<Long> cardIds, CompletableFuture<GameRoom> future) {
    public MatchingRequestDto(Long playerId, List<Long> cardIds) {
        this(playerId, cardIds, new CompletableFuture<>());
    }
}
