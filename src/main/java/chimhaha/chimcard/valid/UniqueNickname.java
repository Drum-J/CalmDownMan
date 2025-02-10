package chimhaha.chimcard.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueNicknameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueNickname {

    String message() default "이미 사용 중인 닉네임입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
