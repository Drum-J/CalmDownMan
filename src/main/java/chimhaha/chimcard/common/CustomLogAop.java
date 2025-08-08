package chimhaha.chimcard.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CustomLogAop {

    // 각 스레드별로 호출 깊이를 저장할 ThreadLocal
    private final ThreadLocal<Integer> callDepth = new ThreadLocal<>();

    @Around("execution(* chimhaha.chimcard..controller..*(..)) || " +
            "execution(* chimhaha.chimcard..service..*(..)) || " +
            "execution(* chimhaha.chimcard..repository..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();

        // 현재 깊이를 가져오고, 없으면 0으로 시작
        Integer depth = callDepth.get();
        if (depth == null) {
            depth = 0;
        }

        // 들여쓰기 생성
        String indent = "  ".repeat(depth);

        // 다음 호출을 위해 깊이를 1 증가
        callDepth.set(depth + 1);

        try {
            log.info("{}➡️ {}() 호출", indent, methodName);

            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed(); // 실제 메서드 실행
            long end = System.currentTimeMillis();

            log.info("{}⬅️ {}() 완료 ({}ms)", indent, methodName, end - start);

            return result;
        } finally {
            // 메서드 실행이 끝나면 (정상/예외 무관) 깊이를 원래대로 복원
            if (depth == 0) {
                // 최상위 호출이 끝나면 ThreadLocal 정리 (메모리 누수 방지)
                callDepth.remove();
            } else {
                callDepth.set(depth);
            }
        }
    }
}
