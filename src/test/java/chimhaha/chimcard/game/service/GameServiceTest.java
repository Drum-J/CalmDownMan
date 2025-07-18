package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static chimhaha.chimcard.entity.CardLocation.FIELD;
import static chimhaha.chimcard.entity.CardLocation.GRAVE;
import static chimhaha.chimcard.entity.CardLocation.HAND;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks GameService gameService;

    @Mock GameRoomRepository gameRoomRepository;
    @Mock GameCardRepository gameCardRepository;
    @Mock SimpMessagingTemplate simpMessagingTemplate; //message send 추가로 인해 Mock 추가

    private Account player1;
    private Account player2;
    private GameRoom gameRoom;
    private Card card1;
    private Card card2;
    private GameCard gameCardInHand;

    @BeforeEach
    void setUp() {
        player1 = Account.builder().id(1L).nickname("Player1").build();
        player2 = Account.builder().id(2L).nickname("Player2").build();
        gameRoom = GameRoom.builder()
                .id(1L)
                .player1(player1)
                .player2(player2)
                .currentTurnPlayerId(player1.getId())
                .build();
        card1 = Card.builder().id(101L).title("Card1").attackType(AttackType.SCISSORS).power(9).grade(Grade.R).build();
        card2 = Card.builder().id(102L).title("Card2").attackType(AttackType.PAPER).power(6).grade(Grade.N).build();
        gameCardInHand = GameCard.builder()
                .id(201L)
                .playerId(player1.getId())
                .card(card1)
                .location(HAND)
                .build();
    }

    @Test
    @DisplayName("player1 카드 제출 성공")
    void cardSubmit_Success() {
        // given
        given(gameRoomRepository.findWithPlayersById(anyLong())).willReturn(Optional.of(gameRoom));
        given(gameCardRepository.findById(anyLong())).willReturn(Optional.of(gameCardInHand));
        given(gameCardRepository.findWithCardByGameRoomAndLocation(anyLong(), any(CardLocation.class)))
                .willReturn(new ArrayList<>()); // 필드는 비어있음

        // when
        // player1의 카드 제출
        gameService.cardSubmit(gameRoom.getId(), player1.getId(), gameCardInHand.getId());

        // then
        assertEquals(FIELD, gameCardInHand.getLocation());
        assertEquals(1, gameCardInHand.getFieldPosition()); // Player1은 1번 위치에 배치
        assertEquals(player2.getId(), gameRoom.getCurrentTurnPlayerId()); // 턴이 넘어감
    }

    @Test
    @DisplayName("자신의 턴이 아닐 때 카드 제출 시 예외 발생")
    void cardSubmit_Fail_NotMyTurn() {
        // given
        gameRoom.changeTurn(); // 턴을 player2에게 넘김
        given(gameRoomRepository.findWithPlayersById(anyLong())).willReturn(Optional.of(gameRoom));

        // when & then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                        gameService.cardSubmit(gameRoom.getId(), player1.getId(), gameCardInHand.getId());
                    });
                    assertEquals("현재 턴이 아닙니다.", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("필드 카드 전진 테스트")
    void cardSubmit() {
        // given
        // 기존 필드에 깔린 카드
        GameCard oldCard = GameCard.builder()
                .id(202L)
                .playerId(player1.getId())
                .card(card2)
                .location(FIELD)
                .fieldPosition(1)
                .build();

        given(gameRoomRepository.findWithPlayersById(anyLong())).willReturn(Optional.of(gameRoom));
        given(gameCardRepository.findById(anyLong())).willReturn(Optional.of(gameCardInHand));
        given(gameCardRepository.findWithCardByGameRoomAndLocation(anyLong(), any(CardLocation.class)))
                .willReturn(List.of(oldCard));

        // when
        // 손에 있던 카드 제출
        gameService.cardSubmit(gameRoom.getId(), player1.getId(), gameCardInHand.getId());

        // then
        assertEquals(2, oldCard.getFieldPosition()); // 기존 카드는 2번 위치로 전진
        assertEquals(1, gameCardInHand.getFieldPosition()); // 새 카드는 1번 위치에 배치
    }

    @Test
    @DisplayName("두 카드가 만나서 battle 메서드 발생")
    void cardSubmitAndBattle() throws Exception {
        //given
        // p2 턴으로 변경
        gameRoom.changeTurn();

        // 현재 필드 상황 [p1Card1] [p1Card2] [p1Card3] [] [p2Card1] [p2Card2]
        GameCard p1Card1 = GameCard.builder().id(201L).playerId(player1.getId()).card(card1).location(FIELD).fieldPosition(1).build();
        GameCard p1Card2 = GameCard.builder().id(202L).playerId(player1.getId()).card(card2).location(FIELD).fieldPosition(2).build();
        GameCard p1Card3 = GameCard.builder().id(203L).playerId(player1.getId()).card(card1).location(FIELD).fieldPosition(3).build();

        GameCard p2Card1 = GameCard.builder().id(204L).playerId(player2.getId()).card(card2).location(FIELD).fieldPosition(5).build();
        GameCard p2Card2 = GameCard.builder().id(205L).playerId(player2.getId()).card(card1).location(FIELD).fieldPosition(6).build();
        GameCard p2Card3 = GameCard.builder().id(206L).playerId(player2.getId()).card(card2).location(HAND).build(); // 제출할 카드

        //repository 조회 결과
        given(gameRoomRepository.findWithPlayersById(anyLong())).willReturn(Optional.of(gameRoom));
        given(gameCardRepository.findById(206L)).willReturn(Optional.of(p2Card3));
        given(gameCardRepository.findWithCardByGameRoomAndLocation(anyLong(), any(CardLocation.class))).willReturn(List.of(p1Card1, p1Card2, p1Card3, p2Card1, p2Card2));

        //when
        gameService.cardSubmit(gameRoom.getId(), player2.getId(), p2Card3.getId()); // p2 카드 제출

        //then
        assertAll(
                () -> {
                    assertEquals(5, p2Card2.getFieldPosition());
                    assertEquals(6, p2Card3.getFieldPosition());
                    assertEquals(FIELD, p2Card3.getLocation());
                },
                () -> {
                    assertNull(p2Card1.getFieldPosition()); // 졌기 때문에 필드에서 제거되고,
                    assertEquals(GRAVE, p2Card1.getLocation()); // 무덤으로 이동
                    assertEquals(FIELD, p1Card3.getLocation());
                }
                // gameService.battle() 은 private 이기 때문에 verify() 할 수 없음
        );
    }

    @Test
    @DisplayName("필드 배틀 테스트 - P1 승리")
    void fieldBattle() {
        // given
        gameCardInHand.handToField(1); // p1의 카드 필드 제출
        GameCard p2Card = GameCard.builder().id(202L).playerId(player2.getId()).card(card2).fieldPosition(6).build();
        given(gameRoomRepository.findWithPlayersById(anyLong())).willReturn(Optional.of(gameRoom));
        given(gameCardRepository.findWithCardByGameRoomAndLocation(anyLong(), any(CardLocation.class)))
                .willReturn(List.of(gameCardInHand, p2Card));

        // when
        gameService.fieldBattle(gameRoom.getId(), player1.getId());

        // then
        assertEquals(FIELD, gameCardInHand.getLocation()); // 승리한 카드는 필드에 남음
        assertEquals(GRAVE, p2Card.getLocation()); // 패배한 카드는 무덤으로
    }
}
