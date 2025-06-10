package chimhaha.chimcard.trade.dto;

import java.util.List;

public record TradePostCreateDto(String title, String content, List<Long> cardIds) {
}
