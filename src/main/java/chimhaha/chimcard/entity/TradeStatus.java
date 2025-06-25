package chimhaha.chimcard.entity;

public enum TradeStatus {
    WAITING, COMPLETED, REJECTED, CANCEL;

    public static TradeStatus getEnum(String status) {
        try {
            return status != null ? TradeStatus.valueOf(status) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
