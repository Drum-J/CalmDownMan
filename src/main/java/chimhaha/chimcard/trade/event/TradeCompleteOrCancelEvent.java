package chimhaha.chimcard.trade.event;

import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;

import java.util.List;

public record TradeCompleteOrCancelEvent(List<TradeRequest> requests, TradeStatus status) {
}
