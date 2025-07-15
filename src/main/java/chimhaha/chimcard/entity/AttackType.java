package chimhaha.chimcard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttackType {
    ROCK("바위"), SCISSORS("가위"), PAPER("보"), ALL("ALL");

    private final String type;

    public static AttackType getEnum(String type) {
        for (AttackType attackType : values()) {
            if (type.equals(attackType.type)) {
                return attackType;
            }
        }

        return null;
    }

    // 승: 1 무승부: 0 패: -1
    public int compare(AttackType other) {
        if (this == other) return 0; // 같은 타입이면 무승부
        if (this == ALL) return 1; // ALL은 ALL 이외에는 모두 승리
        if (other == ALL) return -1; // 상대방이 ALL이면 패배

        return switch (this) {
            case ROCK -> (other == SCISSORS) ? 1 : -1;
            case SCISSORS -> (other == PAPER) ? 1 : -1;
            case PAPER -> (other == ROCK) ? 1 : -1;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}
