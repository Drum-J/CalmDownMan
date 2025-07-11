package chimhaha.chimcard.game.pure;

import chimhaha.chimcard.entity.AttackType;
import chimhaha.chimcard.entity.Grade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.internal.matchers.CompareTo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static chimhaha.chimcard.entity.AttackType.*;
import static chimhaha.chimcard.entity.Grade.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameRoom에 추가하게 될 메서드 테스트
 */
@Slf4j
public class FieldCardTest {

    private final List<PureGameCard> fieldCards = new ArrayList<>(Collections.nCopies(6, null));

    private final PureAccount player1 = new PureAccount(1L);
    private final PureAccount player2 = new PureAccount(2L);

    private final List<PureGameCard> cards1 = new ArrayList<>(List.of(
            new PureGameCard(player1, new PureCard(1L, SCISSORS, SR, 13)), // 패
            new PureGameCard(player1, new PureCard(2L, ROCK, SR, 10)), // 승
            new PureGameCard(player1, new PureCard(3L, SCISSORS, R, 7)), // power 패
            new PureGameCard(player1, new PureCard(4L, PAPER, R, 9)), // 패
            new PureGameCard(player1, new PureCard(5L, SCISSORS, N, 6)), // 승
            new PureGameCard(player1, new PureCard(6L, ROCK, R, 7)), // Grade 승
            new PureGameCard(player1, new PureCard(7L, PAPER, C, 2)) // 패
    ));
    private final List<PureGameCard> cards2 = new ArrayList<>(List.of(
            new PureGameCard(player2, new PureCard(8L, ROCK, SR, 11)), // 승
            new PureGameCard(player2, new PureCard(9L, SCISSORS, R, 9)), // 패
            new PureGameCard(player2, new PureCard(10L, SCISSORS, C, 8)), // power 승
            new PureGameCard(player2, new PureCard(11L, ROCK, R, 11)), // 승
            new PureGameCard(player2, new PureCard(12L, PAPER, C, 1)), // 패
            new PureGameCard(player2, new PureCard(13L, ROCK, N, 7)), // Grade 패
            new PureGameCard(player2, new PureCard(14L, SCISSORS, N, 6)) // 승
    ));

    private Long currentTurnPlayerId; // 어떤 플레이어의 턴인가
    private Integer currentPlayer1FieldIndex; // 플레이어1의 가장 앞에 있는 카드 위치
    private Integer currentPlayer2FieldIndex; // 플레이어2의 가장 앞에 있는 카드 위치

    @BeforeEach
    void setUp() {
        currentPlayer1FieldIndex = -1;
        currentPlayer2FieldIndex = 6;

        currentTurnPlayerId = whoIsFirst();
        log.info("who's turn: {}", currentTurnPlayerId);
    }

    @AfterEach
    void end() {
        log.info("currentTopPlayer1Card: {}, currentTopPlayer2Card: {}", currentPlayer1FieldIndex, currentPlayer2FieldIndex);
        log.info("fieldCards: {}", fieldCards);
    }

    @Test
    @DisplayName("각각 2턴씩 카드 제출")
    void turn2() throws Exception {
        for (int i = 0; i < 2; i++) {
            addCardToField(cards1.get(i), player1.id()); //player1 카드 제출
            addCardToField(cards2.get(i), player2.id()); //player2 카드 제출
        }
    }

    @Test
    @DisplayName("각각 3턴씩 카드제출")
    void turn3() throws Exception {
        for (int i = 0; i < 3; i++) {
            addCardToField(cards1.get(i), player1.id()); //player1 카드 제출
            addCardToField(cards2.get(i), player2.id()); //player2 카드 제출
        }
    }

    @Test
    @DisplayName("필드가 가득 찼을때는 더 이상 제출할 수 없음.")
    void turn4() throws Exception {
        for (int i = 0; i < 3; i++) {
            addCardToField(cards1.get(i), player1.id()); //player1 카드 제출
            addCardToField(cards2.get(i), player2.id()); //player2 카드 제출
        }

        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> addCardToField(cards1.get(3), player1.id()));
                    assertEquals("더 이상 카드를 제출할 수 없습니다. playerId: " + player1.id(), exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> addCardToField(cards2.get(3), player2.id()));
                    assertEquals("더 이상 카드를 제출할 수 없습니다. playerId: " + player2.id(), exception.getMessage());
                }
        );
    }

    /**
     * 카드 제출 메서드
     * 각 플레이어의 fieldIndex와 현재 filedCards의 위치에 null 값을 판단해서 필드를 차지 할 수 있게 한다.
     */
    public void addCardToField(PureGameCard card, Long player) {
        if (player.equals(player1.id())) {
            // currentPlater1FieldIndex가 5라면 0~5까지 모든 필드를 player1이 차지해서 승리
            if (currentPlayer1FieldIndex < 5 && fieldCards.get(currentPlayer1FieldIndex + 1) != null) {
                throw new IllegalArgumentException("더 이상 카드를 제출할 수 없습니다. playerId: " + player);
            }
            for (int i = currentPlayer1FieldIndex; i >= 0; i--) {
                if (fieldCards.get(i) != null && fieldCards.get(i).account.id().equals(player)) {
                    fieldCards.set(i + 1, fieldCards.get(i)); // 기존 카드를 오른쪽으로 이동 시키고
                    fieldCards.set(i, null); // 기존 카드가 있던 곳은 비워둠
                }
            }
            fieldCards.set(0, card); // 마지막에 추가하는 카드는 항상 0번에
            currentPlayer1FieldIndex += 1; // fieldIndex 변경
        } else if (player.equals(player2.id())) { // player2는 오른쪽부터 왼쪽으로 카드를 제출
            // currentPlater2FieldIndex가 0이라면 0~5까지 모든 필드를 player2가 차지해서 승리
            if (currentPlayer2FieldIndex > 0 && fieldCards.get(currentPlayer2FieldIndex - 1) != null) {
                throw new IllegalArgumentException("더 이상 카드를 제출할 수 없습니다. playerId: " + player);
            }
            for (int i = currentPlayer2FieldIndex; i < 6; i++) {
                if (fieldCards.get(i) != null && fieldCards.get(i).account().id().equals(player)) {
                    fieldCards.set(i - 1, fieldCards.get(i)); // 기존 카드를 왼쪽으로 이동 시키고
                    fieldCards.set(i, null); // 기존 카드가 있던 곳은 비워둠
                }
            }
            fieldCards.set(5, card); // 마지막에 추가하는 카드는 항상 5번에
            currentPlayer2FieldIndex -= 1; // fieldIndex 변경
        }

        currentTurnPlayerId = currentTurnPlayerId.equals(player1.id()) ? player2.id() : player1.id();
        log.info("턴 변경: {}", currentTurnPlayerId);

        if (fieldCards.get(currentPlayer1FieldIndex + 1) != null) {
            cardBattle();
        }
    }

    // 최초 선 플레이어 설정
    private Long whoIsFirst() {
        return ThreadLocalRandom.current().nextBoolean() ? player1.id() : player2.id();
    }

    // 카드 승부
    private void cardBattle() {
        PureGameCard card1 = fieldCards.get(currentPlayer1FieldIndex);
        PureGameCard card2 = fieldCards.get(currentPlayer2FieldIndex);
        log.info("카드 승부 호출!!! player1's Card: {}, player2's Card: {}", card1, card2);

        PureCard player1 = card1.card();
        PureCard player2 = card2.card();

        int result = player1.match(player2);
        switch (result) {
            case 1 -> log.info("승리!");
            case 0 -> log.info("무승부");
            case -1 -> log.info("패배...");
        }
    }

    /**
     * 내부 클래스
     */
    record PureAccount(Long id) {
        @Override
        public String toString() {
            return id.toString();
        }
    }

    record PureCard(Long id, AttackType attackType, Grade grade, int power) {

        public int match(PureCard other) {
            log.info("승부 조건 확인!");
            int result = 0;

            int matchType = matchType(this, other);
            log.info("matchType: {}", matchType);

            if (matchType == 0) { // type 무승부일 경우 power 확인
                int matchPower = matchPower(this, other);
                log.info("matchPower: {}", matchPower);

                if (matchPower == 0) { //power 무승부일 경우 grade 확인
                    int matchGrade = matchGrade(this, other);
                    log.info("matchGrade: {}", matchGrade);

                    if (matchGrade == 0) { // 최종적으로 무승부일 경우
                        return result;
                    } else {
                        result = matchGrade;
                    }
                } else { // 승패 결정
                    result = matchPower;
                }

            } else { // 승패 결정
                result = matchType;
            }

            return result;
        }

        // 승: 1 무승부: 0 패: -1
        private int matchType(PureCard card, PureCard other) {
            switch (card.attackType) {
                case ROCK -> {
                    if (other.attackType == SCISSORS) return 1; // 승
                    if (other.attackType == ROCK) return 0; // 무승부
                    if (other.attackType == PAPER || other.attackType == ALL) return -1; // 패
                }
                case SCISSORS -> {
                    if (other.attackType == PAPER) return 1; // 승
                    if (other.attackType == SCISSORS) return 0; // 무승부
                    if (other.attackType == ROCK || other.attackType == ALL) return -1; // 패
                }
                case PAPER -> {
                    if (other.attackType == ROCK) return 1; // 승
                    if (other.attackType == PAPER) return 0; // 무승부
                    if (other.attackType == SCISSORS || other.attackType == ALL) return -1; // 패
                }
                case ALL -> { //ALL은 ALL 이외에는 모두 승리
                    if (other.attackType == ALL) return 0; // 무승부
                    return 1; // 승
                }
            }

            return -2;
        }

        private int matchPower(PureCard pureCard, PureCard other) {
            return Integer.compare(pureCard.power, other.power);
        }

        private int matchGrade(PureCard pureCard, PureCard other) {
            return Integer.compare(pureCard.grade.getValue(), other.grade.getValue()) * -1; // Grade.value 는 등급이 높을수록 숫자가 낮기 때문에 -1 곱해줌
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    record PureGameCard(PureAccount account, PureCard card) {
        @Override
        public String toString() {
            return "{" +
                    "account=" + account +
                    ", card=" + card +
                    '}';
        }
    }
}
