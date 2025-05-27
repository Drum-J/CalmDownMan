package chimhaha.chimcard.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CustomLogAop {

    @Around("execution(* chimhaha.chimcard..controller..*(..)) || " +
            "execution(* chimhaha.chimcard..service..*(..)) || " +
            "execution(* chimhaha.chimcard..repository..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String fullClassName = joinPoint.getSignature().getDeclaringTypeName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();
        log.info("➡️ {}.{}() 호출", simpleClassName, methodName);

        Object result = joinPoint.proceed(); // 실제 메서드 실행

        long end = System.currentTimeMillis();
        log.info("⬅️ {}.{}() 완료 ({}ms)", simpleClassName, methodName, end - start);

        return result;
    }
}
