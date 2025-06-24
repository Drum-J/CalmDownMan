package chimhaha.chimcard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Grade {
    SSR(0) , SR(1) , R(2) , N(3) , C(4) , V(5);

    private final int value;

    public static Grade getEnum(int value) {
        for (Grade grade : values()) {
            if (value == grade.value) {
                return grade;
            }
        }
        return null;
    }
}
