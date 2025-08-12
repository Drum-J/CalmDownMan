package chimhaha.chimcard.game.dto;

import com.querydsl.core.annotations.QueryProjection;

public record RankResponseDto(String username, String nickname, String imageUrl,
                              int rankScore, double rating) {
    @QueryProjection
    public RankResponseDto{

    }
}
