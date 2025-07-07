package chimhaha.chimcard.user;

import chimhaha.chimcard.config.P6SpyConfig;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static chimhaha.chimcard.common.MessageConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@DataJpaTest
@Transactional
@Import({P6SpyConfig.class})
public class PointConcurrencyTest {

    @Autowired private AccountRepository accountRepository;

    @Test
    @DisplayName("포인트 증감 기본")
    void point() throws Exception {
        //given
        Account account = accountRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
        Integer point = account.getPoint();

        //when
        Integer plus = 100;
        account.increasePoint(plus);

        //then
        assertEquals(point + plus, account.getPoint());
    }

    @Test
    @DisplayName("Account.point 데이터 변경 시 동시성 문제")
    void concurrencyPoint() throws Exception {
        //given
        Integer point = 100;
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        //멀티 스레드 결과 담을 리스트
        List<Future<?>> futures = new ArrayList<>();

        //when
        for (int i = 0; i < threadCount; i++) {
            Future<?> submit = executorService.submit(() -> {
                try {
                    latch.await();
                    Account account = accountRepository.findById(1L)
                            .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));

                    account.increasePoint(point);
                    accountRepository.saveAndFlush(account); // 명시적 flush
                    // -> test 에서의 transaction은 rollback이 일어나야 하지만 멀티 스레드에서 진행된 Transaction은 rollback 되지 않음
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            });
            futures.add(submit);
        }

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        //then
        // 멀티 스레드 실행 중 예외가 발생 (실행 중 예외 - ExecutionException)
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            for (Future<?> future : futures) {
                future.get();
            }
        });

        // 발생한 ExecutionException이 낙관적 락 실패 예외(OptimisticLockingFailureException)인지 확인
        assertInstanceOf(OptimisticLockingFailureException.class, exception.getCause());
    }
}
