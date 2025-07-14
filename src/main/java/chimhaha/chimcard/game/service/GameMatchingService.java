package chimhaha.chimcard.game.service;

import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.Card;
import chimhaha.chimcard.entity.GameCard;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.MatchingRequestDto;
import chimhaha.chimcard.game.dto.MatchingSuccessResult;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public void joinMatching(MatchingRequestDto dto) {
        lock.lock();
        try {
            log.info("async joinMatching: [{}]", Thread.currentThread().getName());
            if(matchingMap.containsKey(dto.playerId())) {
                throw new IllegalArgumentException("이미 매칭 대기열에 등록 되어있습니다.");
            }

            matchingMap.put(dto.playerId(), dto);
            matchingQueue.add(dto);
        } finally {
            lock.unlock();
        }
    }

    public void cancelMatching(Long playerId) {
        lock.lock();
        try {
            MatchingRequestDto cancelRequest = matchingMap.remove(playerId);
            log.info("cancelRequest: {}", cancelRequest);
            if (cancelRequest != null) {
                matchingQueue.remove(cancelRequest);
            }
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Optional<MatchingSuccessResult> successMatching() {
        MatchingRequestDto request1 = null;
        MatchingRequestDto request2 = null;

        lock.lock();
        try {
            if (matchingQueue.size() >= 2) {
                log.info("대기열: {}", matchingQueue.size());
                request1 = matchingQueue.poll();
                request2 = matchingQueue.poll();

                // map에서도 제거
                if (request1 != null) matchingMap.remove(request1.playerId());
                if (request2 != null) matchingMap.remove(request2.playerId());
            }
        } finally {
            lock.unlock();
        }

        if (request1 != null && request2 != null) {
            log.info("lock 반환 후 DB 작업");
            try {
                Account player1 = getAccount(request1.playerId());
                Account player2 = getAccount(request2.playerId());

                GameRoom gameRoom = new GameRoom(player1, player2);
                gameRoomRepository.save(gameRoom);

                makeGameCard(gameRoom, player1.getId(), request1.cardIds());
                makeGameCard(gameRoom, player2.getId(), request2.cardIds());

                log.info("매칭 성공. gameId: {}, player1: {}, player2: {}", gameRoom.getId(), player1.getNickname(), player2.getNickname());
                return Optional.of(new MatchingSuccessResult(gameRoom.getId(), player1.getId(), player2.getId()));
            } catch (Exception e) {
                log.error("매칭 성공 후 게임 생성 실패: {}", e.getMessage() , e);
                // (선택) 실패한 경우 대기열 재진입 로직 추가
            }
        }
        return Optional.empty();
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
    }

    private void makeGameCard(GameRoom gameRoom, Long player, List<Long> cardIds) {
        Map<Long, Card> cardMap = cardRepository.findAllById(cardIds)
                .stream().collect(Collectors.toMap(Card::getId, Function.identity()));

        List<GameCard> gameCards = new ArrayList<>();
        for (Long cardId : cardIds) {
            Card card = cardMap.get(cardId);

            if (card == null) {
                throw new ResourceNotFoundException(CARD_NOT_FOUND);
            }

            gameCards.add(new GameCard(gameRoom, player, card));
        }

        gameCardRepository.saveAll(gameCards);
    }
}
