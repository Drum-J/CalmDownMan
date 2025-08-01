package chimhaha.chimcard.game.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record GameRecordDto(String player1, String player2, String gameResult, LocalDateTime gameDate) {

    @QueryProjection
    public GameRecordDto{
    }
}
