package chimhaha.chimcard.game.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.event.MatchingSuccessEvent;
import chimhaha.chimcard.game.event.PlayerMatchingJoinEvent;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GameMatchingServiceTest {

    @InjectMocks GameMatchingService gameMatchingService; // 테스트 대상

    // 테스트에 필요한 가짜 객체
    @Mock GameRoomRepository gameRoomRepository;
    @Mock GameCardRepository gameCardRepository;
    @Mock AccountRepository accountRepository;
    @Mock CardRepository cardRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    // 이벤트 캡쳐
    @Captor ArgumentCaptor<GameRoom> gameRoomCaptor;
    @Captor ArgumentCaptor<MatchingSuccessEvent> matchingSuccessEventCaptor;

    @Test
    @DisplayName("매칭 성공")
    void matchSuccess() throws Exception {
        //given
        Account player1 = createAccount(1L, "user1", "테스트유저1");
        Account player2 = createAccount(2L, "user2", "테스트유저2");

        List<Long> cardIds1 = List.of(1L, 2L);
        List<Long> cardIds2 = List.of(3L, 4L);

        MatchingRequestDto dto1 = new MatchingRequestDto(1L, cardIds1);
        MatchingRequestDto dto2 = new MatchingRequestDto(2L, cardIds2);

        given(accountRepository.findById(dto1.playerId())).willReturn(Optional.of(player1));
        given(accountRepository.findById(dto2.playerId())).willReturn(Optional.of(player2));
        given(cardRepository.findAllById(dto1.cardIds())).willReturn(getCardList(1));
        given(cardRepository.findAllById(dto2.cardIds())).willReturn(getCardList(2));

        //when
        // 플레이어의 매칭 요청
        gameMatchingService.joinMatching(dto1);
        gameMatchingService.joinMatching(dto2);

        //then
        // 이벤트 직접 호출
        gameMatchingService.successMatching();

        // successMatching 에서 해당 메서드가 얼마나 호출 되었는지 확인
        verify(accountRepository, times(2)).findById(anyLong());
        verify(gameRoomRepository, times(1)).save(any(GameRoom.class));
        verify(cardRepository, times(2)).findAllById(anyList());
        verify(gameCardRepository, times(2)).saveAll(anyList());
        // 매칭 성공 이벤트 발행 확인.
        verify(eventPublisher, times(1)).publishEvent(matchingSuccessEventCaptor.capture());
        MatchingSuccessEvent event = matchingSuccessEventCaptor.getValue();

        assertEquals(player1.getId(), event.player1Id());
        assertEquals(player2.getId(), event.player2Id());
    }

    @Test
    @DisplayName("실패 - 대기열에 대기 중인 플레이어의 매칭 요청")
    void joinDuplicate() throws Exception {
        //given
        MatchingRequestDto firstRequest = new MatchingRequestDto(1L, List.of(1L, 2L));
        gameMatchingService.joinMatching(firstRequest); // 최초 대기열 진입 - 정상 처리

        //when
        MatchingRequestDto secondRequest = new MatchingRequestDto(1L, List.of(1L, 2L));

        //then
        // 대기열 재진입 - 예외 발생
        assertAll(
                () -> {
                    IllegalArgumentException exception
                            = assertThrows(IllegalArgumentException.class, () -> gameMatchingService.joinMatching(secondRequest));
                    assertEquals("이미 매칭 대기열에 등록 되어있습니다.", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("매칭 취소")
    void matchCancel() throws Exception {
        //given
        MatchingRequestDto firstRequest = new MatchingRequestDto(1L, List.of(1L, 2L));
        gameMatchingService.joinMatching(firstRequest);

        //when
        gameMatchingService.cancelMatching(1L);

        //then
        // 매칭 취소 후 successMatching 호출
        gameMatchingService.successMatching();// 대기열 부족으로 아무일도 일어나지 않음.

        assertAll(
                () -> {
                    verify(accountRepository, never()).findById(anyLong());
                    verify(gameRoomRepository, never()).save(any(GameRoom.class));
                    verify(cardRepository, never()).findAllById(anyList());
                    verify(gameCardRepository, never()).saveAll(anyList());
                    // 매칭 대기열 등록 이벤트는 발생하지만
                    verify(eventPublisher, times(1)).publishEvent(any(PlayerMatchingJoinEvent.class));
                    // 매칭 성공 이벤트는 발생하지 않는다.
                    verify(eventPublisher, never()).publishEvent(any(MatchingSuccessEvent.class));
                }
        );
    }

    @Test
    @DisplayName("동시성 문제 - 매칭 성공과 취소가 동시에 발생할 경우")
    void matchSuccessAndCancelConcurrency() throws Exception {
        //given
        Account player1 = createAccount(1L, "user1", "테스트유저1");
        Account player2 = createAccount(2L, "user2", "테스트유저2");
        MatchingRequestDto dto1 = new MatchingRequestDto(1L, new ArrayList<>(List.of(1L ,2L)));
        MatchingRequestDto dto2 = new MatchingRequestDto(2L, new ArrayList<>(List.of(3L, 4L)));

        // cancelMatching 이 먼저 실행되면 이게 실행되지 않는다.
        lenient().when(accountRepository.findById(1L)).thenReturn(Optional.of(player1));
        lenient().when(accountRepository.findById(2L)).thenReturn(Optional.of(player2));
        lenient().when(cardRepository.findAllById(dto1.cardIds())).thenReturn(getCardList(1));
        lenient().when(cardRepository.findAllById(dto2.cardIds())).thenReturn(getCardList(2));

        // 대기열 등록
        gameMatchingService.joinMatching(dto1);
        gameMatchingService.joinMatching(dto2);

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // 1번 유저 매칭 취소
        executorService.submit(() -> {
            try {
                latch.await();
                gameMatchingService.cancelMatching(1L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        // 매칭 성공
        executorService.submit(() -> {
            try {
                latch.await();
                gameMatchingService.successMatching();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        //then
        // 매칭 대기열 등록 이벤트는 2번 발생
        verify(eventPublisher, times(2)).publishEvent(any(PlayerMatchingJoinEvent.class));
        // gameRoomRepository.save()가 적어도 1번 실행 (매칭 취소: 0, 매칭 성공: 1)
        verify(gameRoomRepository, atMost(1)).save(gameRoomCaptor.capture());

        if (gameRoomCaptor.getAllValues().isEmpty()) { // 매칭 취소가 먼저 된 경우
            log.info("매칭 취소가 먼저 진행됨.");
            assertAll(
                    () -> {
                        verify(accountRepository, never()).findById(anyLong());
                        verify(cardRepository, never()).findAllById(anyList());
                        verify(eventPublisher, never()).publishEvent(any(MatchingSuccessEvent.class));
                    }
            );
        } else { // 매칭 성사가 먼저 된 경우
            log.info("매칭 성사가 먼저 진행됨.");
            assertAll(
                    () -> {
                        verify(accountRepository, times(2)).findById(anyLong());
                        verify(cardRepository, times(2)).findAllById(anyList());
                        verify(gameCardRepository, times(2)).saveAll(anyList());
                        verify(eventPublisher, times(1)).publishEvent(any(MatchingSuccessEvent.class));
                    }
            );
        }
    }

    private Account createAccount(Long id, String username, String nickname) {
        return Account.builder()
                .id(id)
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
}
