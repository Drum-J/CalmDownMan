package chimhaha.chimcard.trade.dto;

import java.util.List;

public record TradePostCardRequestDto(String title, String content, List<Long> cardIds) {
}
