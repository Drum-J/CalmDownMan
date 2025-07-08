package chimhaha.chimcard.game.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
}