/*
package chimhaha.chimcard.game.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GameMatchingServiceTest {

    @InjectMocks GameMatchingService gameMatchingService; // 테스트 대상

    // 테스트에 필요한 가짜 객체
    @Mock GameRoomRepository gameRoomRepository;
    @Mock GameCardRepository gameCardRepository;
    @Mock AccountRepository accountRepository;
    @Mock CardRepository cardRepository;

    @Test
    @DisplayName("매칭 성공")
    void matchSuccess() throws Exception {
        //given
        Account player1 = createAccount("user1", "테스트유저1");
        Account player2 = createAccount("user2", "테스트유저2");

        List<Long> cardIds1 = List.of(1L, 2L);
        List<Long> cardIds2 = List.of(3L, 4L);

        MatchingRequestDto dto1 = new MatchingRequestDto(1L, cardIds1);
        MatchingRequestDto dto2 = new MatchingRequestDto(2L, cardIds2);

        given(accountRepository.findById(dto1.playerId())).willReturn(Optional.of(player1));
        given(accountRepository.findById(dto2.playerId())).willReturn(Optional.of(player2));
        given(cardRepository.findAllById(dto1.cardIds())).willReturn(getCardList(1));
        given(cardRepository.findAllById(dto2.cardIds())).willReturn(getCardList(2));

        //when
        // 1. 플레이어의 매칭 요청
        CompletableFuture<GameRoom> future1 = gameMatchingService.joinMatching(dto1);
        CompletableFuture<GameRoom> future2 = gameMatchingService.joinMatching(dto2);

        // 2. 스케쥴러 직접 호출
        gameMatchingService.successMatching();

        //then
        // 1-1. 매칭 요청의 결과 확인, 스케줄에 맞게 돌진 않지만 스케쥴이 1초마다 돌기 때문에 2초로 지정
        GameRoom gameRoom1 = future1.get(2, TimeUnit.SECONDS);
        GameRoom gameRoom2 = future2.get(2, TimeUnit.SECONDS);

        // 1-2. 같은 GameRoom 인지 확인
        assertAll(
                () -> assertNotNull(gameRoom1),
                () -> assertEquals(gameRoom1, gameRoom2)
        );

        // 2. successMatching 에서 해당 메서드가 얼마나 호출 되었는지 확인
        verify(accountRepository, times(2)).findById(anyLong());
        verify(gameRoomRepository, times(1)).save(any(GameRoom.class));
        verify(cardRepository, times(2)).findAllById(anyList());
        verify(gameCardRepository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("실패 - 대기열에 대기 중인 플레이어의 매칭 요청")
    void joinDuplicate() throws Exception {
        //given
        MatchingRequestDto firstRequest = new MatchingRequestDto(1L, List.of(1L, 2L));
        gameMatchingService.joinMatching(firstRequest); // 최초 대기열 진입 - 정상 처리

        //when
        MatchingRequestDto secondRequest = new MatchingRequestDto(1L, List.of(1L, 2L));
        CompletableFuture<GameRoom> failedFuture = gameMatchingService.joinMatching(secondRequest); // 대기열 재진입 - 예외 발생

        //then
        assertAll(
                () -> assertTrue(failedFuture.isCompletedExceptionally()),
                () -> {
                    ExecutionException exception = assertThrows(ExecutionException.class, () -> failedFuture.get(2, TimeUnit.SECONDS));
                    IllegalArgumentException illegalArgumentException = assertInstanceOf(IllegalArgumentException.class, exception.getCause());
                    assertEquals("이미 매칭 대기열에 등록 되어있습니다.",illegalArgumentException.getMessage());
                }
        );
    }

    @Test
    @DisplayName("매칭 취소")
    void matchCancel() throws Exception {
        //given
        MatchingRequestDto firstRequest = new MatchingRequestDto(1L, List.of(1L, 2L));
        CompletableFuture<GameRoom> future = gameMatchingService.joinMatching(firstRequest);

        //when
        gameMatchingService.cancelMatching(1L);

        //then
        assertAll(
                () -> {
                    CancellationException cancel = assertThrows(CancellationException.class, () -> future.get(2, TimeUnit.SECONDS));
                    assertEquals("매칭 취소가 완료되었습니다.", cancel.getMessage());
                }
        );

        // 매칭 취소 후 successMatching 호출
        gameMatchingService.successMatching(); // 대기열 부족으로 아무일도 일어나지 않음.
        assertAll(
                () -> {
                    verify(accountRepository, never()).findById(anyLong());
                    verify(gameRoomRepository, never()).save(any(GameRoom.class));
                    verify(cardRepository, never()).findAllById(anyList());
                    verify(gameCardRepository, never()).saveAll(anyList());
                }
        );
    }

    @Test
    @DisplayName("동시성 문제 - 매칭 성공과 취소가 동시에 발생할 경우")
    void matchSuccessAndCancelConcurrency() throws Exception {
        //given
        Account player1 = createAccount("user1", "테스트유저1");
        Account player2 = createAccount("user2", "테스트유저2");
        MatchingRequestDto dto1 = new MatchingRequestDto(1L, new ArrayList<>(List.of(1L ,2L)));
        MatchingRequestDto dto2 = new MatchingRequestDto(2L, new ArrayList<>(List.of(3L, 4L)));

        // cancelMatching 이 먼저 실행되면 이게 실행되지 않는다.
        lenient().when(accountRepository.findById(1L)).thenReturn(Optional.of(player1));
        lenient().when(accountRepository.findById(2L)).thenReturn(Optional.of(player2));
        lenient().when(cardRepository.findAllById(dto1.cardIds())).thenReturn(getCardList(1));
        lenient().when(cardRepository.findAllById(dto2.cardIds())).thenReturn(getCardList(2));

        // 대기열 등록
        CompletableFuture<GameRoom> future1 = gameMatchingService.joinMatching(dto1);
        CompletableFuture<GameRoom> future2 = gameMatchingService.joinMatching(dto2);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // 매칭 성공
        executorService.submit(() -> {
            try {
                latch.await();
                gameMatchingService.successMatching();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        // 1번 유저 매칭 취소
        executorService.submit(() -> {
            try {
                latch.await();
                gameMatchingService.cancelMatching(1L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        //then
        if (future1.isCompletedExceptionally()) { // 매칭 취소가 먼저 된 경우
            assertAll(
                    () -> {
                        verify(gameRoomRepository, never()).save(any(GameRoom.class));
                        verify(accountRepository, never()).findById(anyLong());
                        verify(cardRepository, never()).findAllById(anyList());
                    }
            );
        } else { // 매칭 성사가 먼저 된 경우
            assertTrue(future2.isDone() && !future2.isCompletedExceptionally());
        }
    }
    private Account createAccount(String username, String nickname) {
        return Account.builder()
                .username(username)
                .password("1234")
                .nickname(nickname)
                .point(1000)
                .role(AccountRole.USER)
                .build();
    }

    private List<Card> getCardList(int order) {
        List<Card> cards = new ArrayList<>();
        switch (order) {
            case 1 -> {
                cards.add(createCard(1L, "card1"));
                cards.add(createCard(2L, "card2"));
            }
            case 2 -> {
                cards.add(createCard(3L, "card3"));
                cards.add(createCard(4L, "card4"));
            }
        }

        return cards;
    }

    private Card createCard(Long id,String title) {
        return Card.builder()
                .id(id)
                .title(title)
                .attackType(AttackType.PAPER)
                .grade(Grade.R)
                .power(10)
                .build();
    }
}*/
