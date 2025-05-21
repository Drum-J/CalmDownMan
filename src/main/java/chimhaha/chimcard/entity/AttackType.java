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
}
