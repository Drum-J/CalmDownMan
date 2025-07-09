package chimhaha.chimcard.game.dto;

import java.util.List;

public record GameInfoDto(String otherPlayer, List<MyGameCardDto> myCards) {
}
