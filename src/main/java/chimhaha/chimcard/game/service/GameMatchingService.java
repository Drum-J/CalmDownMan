package chimhaha.chimcard.game.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.GameCard;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static chimhaha.chimcard.common.MessageConstants.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameMatchingService {

    private final GameRoomRepository gameRoomRepository;
    private final GameCardRepository gameCardRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    private final Map<Long, MatchingRequestDto> matchingMap = new ConcurrentHashMap<>();
    private final Queue<MatchingRequestDto> matchingQueue = new LinkedBlockingQueue<>();
    private final Lock lock = new ReentrantLock();

    @Async
    public CompletableFuture<GameRoom> joinMatching(MatchingRequestDto dto) {
        lock.lock();
        log.info("lock joinMatching: [{}]", Thread.currentThread().getName());
        try {
            log.info("async joinMatching: [{}]", Thread.currentThread().getName());
            if(matchingMap.containsKey(dto.playerId())) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("이미 매칭 대기열에 등록 되어있습니다."));
            }

            matchingMap.put(dto.playerId(), dto);
            matchingQueue.add(dto);

            return dto.future();
        } finally {
            lock.unlock();
            log.info("unlock joinMatching: [{}]", Thread.currentThread().getName());
        }
    }

    public void cancelMatching(Long playerId) {
        lock.lock();
        log.info("lock cancelMatching: [{}]", Thread.currentThread().getName());
        try {
            MatchingRequestDto cancelRequest = matchingMap.remove(playerId);
            log.info("cancelRequest: {}", cancelRequest);
            if(cancelRequest != null) {
                matchingQueue.remove(cancelRequest);
                cancelRequest.future().completeExceptionally(
                        new CancellationException("매칭 취소가 완료되었습니다.")
                );

                log.info("플레이어: {} 매칭 취소 완료", cancelRequest.playerId());
            }
        } finally {
            lock.unlock();
            log.info("unlock cancelMatching: [{}]", Thread.currentThread().getName());
        }
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void successMatching() {
        MatchingRequestDto request1 = null;
        MatchingRequestDto request2 = null;

        lock.lock();
        log.info("lock successMatching: [{}]", Thread.currentThread().getName());
        try {
            if (matchingQueue.size() >= 2) {
                log.info("대기열: {}", matchingQueue.size());
                request1 = matchingQueue.poll();
                request2 = matchingQueue.poll();

                log.info("request1: {}", request1);
                log.info("request2: {}", request2);

                // map에서도 제거
                if (request1 != null) {
                    matchingMap.remove(request1.playerId());
                }
                if (request2 != null) {
                    matchingMap.remove(request2.playerId());
                }
            }
        } finally {
            lock.unlock();
            log.info("unlock successMatching: [{}]", Thread.currentThread().getName());
        }

        if (request1 != null && request2 != null) {
            log.info("lock 반환 후 DB 작업 및 future.complete()");
            try {
                Account player1 = getAccount(request1.playerId());
                Account player2 = getAccount(request2.playerId());

                GameRoom gameRoom = new GameRoom(player1, player2);
                gameRoomRepository.save(gameRoom);

                makeGameCard(gameRoom, player1, request1.cardIds());
                makeGameCard(gameRoom, player2, request2.cardIds());

                request1.future().complete(gameRoom);
                request2.future().complete(gameRoom);
                log.info("매칭 성공. gameId: {}, player1: {}, player2: {}", gameRoom.getId(), player1.getNickname(), player2.getNickname());
            } catch (Exception e) {
                log.error("매칭 성공 후 게임 생성 실패: {}", e.getMessage() , e);
                RuntimeException matchException = new RuntimeException("매칭 처리 중 서버 오류가 발생했습니다.", e);
                request1.future().completeExceptionally(matchException);
                request2.future().completeExceptionally(matchException);
            }
        }
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
    }

    private void makeGameCard(GameRoom gameRoom, Account player, List<Long> cardIds) {
        Map<Long, Card> cardMap = cardRepository.findAllById(cardIds)
                .stream().collect(Collectors.toMap(Card::getId, Function.identity()));

        List<Integer> orders = makeCardOrder(cardIds.size());

        List<GameCard> gameCards = new ArrayList<>();
        for (int i = 0; i < cardIds.size(); i++) {
            Long cardId = cardIds.get(i);
            Card card = cardMap.get(cardId);

            if (card == null) {
                throw new ResourceNotFoundException(CARD_NOT_FOUND);
            }

            gameCards.add(new GameCard(gameRoom, player, card, orders.get(i)));
        }

        gameCardRepository.saveAll(gameCards);
    }

    private List<Integer> makeCardOrder(int size) {
        List<Integer> orders = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            orders.add(i);
        }

        Collections.shuffle(orders);
        return orders;
    }
}
