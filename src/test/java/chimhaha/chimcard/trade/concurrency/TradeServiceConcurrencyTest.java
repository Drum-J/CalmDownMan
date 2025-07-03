package chimhaha.chimcard.trade.concurrency;

import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradeRequest;
import chimhaha.chimcard.entity.TradeStatus;
import chimhaha.chimcard.trade.dto.TradeStatusRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ActiveProfiles("h2")
public class TradeServiceConcurrencyTest extends TradeConcurrencyBase {

    @Test
    @DisplayName("사용자 B와 교환 성공 / C,D는 교환 거절 - 정상 흐름")
    void success() throws Exception {
        //given
        TradeStatusRequestDto dto = new TradeStatusRequestDto(requestB.getId());

        //when
        tradeService.tradeComplete(tradePost.getId(), owner.getId(), dto);

        //then
        // 비동기 스레드의 로직이 5초 이내로 끝나는지 확인
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                //Optional.get() 사용 시 isPresent() 체크 해야하지만 테스트라 무시
                tradeRequestRepository.findById(requestC.getId()).get().getTradeStatus().equals(TradeStatus.REJECTED) &&
                tradeRequestRepository.findById(requestD.getId()).get().getTradeStatus().equals(TradeStatus.REJECTED)
        );

        // 교환 결과 조회 .get() 무시
        TradePost completedPost = tradePostRepository.findById(tradePost.getId()).get();
        TradeRequest completedRequestB = tradeRequestRepository.findById(requestB.getId()).get();
        TradeRequest rejectedRequestC = tradeRequestRepository.findById(requestC.getId()).get();
        TradeRequest rejectedRequestD = tradeRequestRepository.findById(requestD.getId()).get();

        // 1. 교환 상태 검증
        assertAll(
                () -> assertEquals(TradeStatus.COMPLETED, completedPost.getTradeStatus()),
                () -> assertEquals(TradeStatus.COMPLETED, completedRequestB.getTradeStatus()),
                () -> assertEquals(TradeStatus.REJECTED, rejectedRequestC.getTradeStatus()),
                () -> assertEquals(TradeStatus.REJECTED, rejectedRequestD.getTradeStatus())
        );

        // 2. 카드 변경 검증
        // 2-1. owner 소유 카드 변경
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(owner, userBCard).isPresent()),
                () -> assertFalse(accountCardRepository.findByAccountAndCard(owner, ownerCard).isPresent())
        );

        // 2-2. userB 소유 카드 변경
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userB,ownerCard).isPresent()),
                () -> assertFalse(accountCardRepository.findByAccountAndCard(userB, userBCard).isPresent())
        );

        // 2-3. C,D 카드 유지
        assertAll(
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userC, userCCard).isPresent()),
                () -> assertTrue(accountCardRepository.findByAccountAndCard(userD, userDCard).isPresent())
        );
    }

    @Test
    @DisplayName("동시성 문제 1: B와 교환 성공 시도 중 B가 신청 취소")
    void completeAndCancel() throws Exception {
        //given
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        TradeStatusRequestDto dto = new TradeStatusRequestDto(requestB.getId());

        //when
        // 멀티 스레드 1: 교환 수락
        executorService.submit(() -> {
            try {
                latch.await(); // countDownLatch 가 0이 될 때까지 대기
                tradeService.tradeComplete(tradePost.getId(), owner.getId(), dto);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        // 멀티 스레드 2: 교환 신청 취소
        executorService.submit(() -> {
            try {
                latch.await(); // countDownLatch 가 0이 될 때까지 대기
                tradeService.requestCancel(requestB.getId(), userB.getId());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        latch.countDown(); // 1 -> 0 으로 감소, await() 끝! 멀티 스레드 동시 시작
        executorService.shutdown(); // 새로운 작업을 받지 않는다. 처리 중이거나, 큐에 이미 대기중인 작업은 처리한다. 이후에 풀의 스레드를 종료한다.
        executorService.awaitTermination(10, TimeUnit.SECONDS); // 처리/대기중인 작업들을 모두 완료할 때 까지 10초 기다린다.

        //then
        TradeRequest tradeRequest = tradeRequestRepository.findById(requestB.getId()).get();

        // 멀티 스레드를 통해 실행된 로직의 결과는 확정지을 수 없다. 완료 되었거나 취소되어야 한다.
        assertAll(
                () -> assertTrue(
                        tradePost.getTradeStatus().equals(TradeStatus.COMPLETED) ||
                                tradePost.getTradeStatus().equals(TradeStatus.WAITING)
                ),
                () -> assertTrue(
                        tradeRequest.getTradeStatus().equals(TradeStatus.COMPLETED) ||
                                tradeRequest.getTradeStatus().equals(TradeStatus.CANCEL)
                )
        );

        // 완료된 경우
        if (tradeRequest.getTradeStatus().equals(TradeStatus.COMPLETED)) {
            assertAll(
                    // owner 카드 변경
                    () -> assertTrue(accountCardRepository.findByAccountAndCard(owner, userBCard).isPresent()),
                    () -> assertFalse(accountCardRepository.findByAccountAndCard(owner, ownerCard).isPresent()),
                    // userB 카드 변경
                    () -> assertTrue(accountCardRepository.findByAccountAndCard(userB, ownerCard).isPresent()),
                    () -> assertFalse(accountCardRepository.findByAccountAndCard(userB, userBCard).isPresent())
            );
        } else { // 취소된 경우 TradeStatus.CANCEL
            assertAll(
                    // owner 의 교환글은 여전히 대기 중이기 때문에 ownerCard 는 존재하지 않음
                    () -> assertFalse(accountCardRepository.findByAccountAndCard(owner, ownerCard).isPresent()),
                    // userB의 카드만 본인에게 돌아감
                    () -> assertTrue(accountCardRepository.findByAccountAndCard(userB, userBCard).isPresent())
            );
        }
    }

    @Test
    @DisplayName("동시성 문제 2: A-B 교환 성공 / C,D의 교환 거절 중(비동기) C의 교환 신청 취소")
    void rejectAndCancel() throws Exception {
        //given
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        // A와 B의 교환은 동기로 처리, 교환 성공 됨
        TradeStatusRequestDto dto = new TradeStatusRequestDto(requestB.getId());

        //when
        // 멀티 스레드 1: 교환 수락, C,D의 카드 롤백은 비동기로 처리
        executorService.submit(() -> {
            try {
                latch.await(); // countDownLatch 가 0이 될 때까지 대기
                tradeService.tradeComplete(tradePost.getId(), owner.getId(), dto);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        //멀티 스레드 2: C가 본인의 교환 신청을 취소
        executorService.submit(() -> {
            try {
                latch.await();
                tradeService.requestCancel(requestC.getId(), userC.getId());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        //then
        TradeRequest tradeRequest = tradeRequestRepository.findById(requestC.getId()).get();
        AccountCard accountCard = accountCardRepository.findByAccountAndCard(userC, userCCard).get();

        assertAll(
                () -> assertTrue(
                        tradeRequest.getTradeStatus().equals(TradeStatus.REJECTED) ||
                                tradeRequest.getTradeStatus().equals(TradeStatus.CANCEL)
                ),
                () -> assertEquals(1, accountCard.getCount())
        );
    }
}
