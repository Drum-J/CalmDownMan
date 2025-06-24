package chimhaha.chimcard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    WAITING("대기"), COMPLETED("완료"), REJECTED("거절"), CANCEL("취소");

    private final String status;

    public static TradeStatus getEnum(String status) {
        for (TradeStatus tradeStatus : values()) {
            if (status != null && status.equals(tradeStatus.status)) {
                return tradeStatus;
            }
        }
        return null;
    }
}
