package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static chimhaha.chimcard.common.MessageConstants.GAME_ROOM_NOT_FOUND;
import static chimhaha.chimcard.common.PointConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameResultAsyncService {

    private final GameRoomRepository gameRoomRepository;

    @Async
    @Transactional
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    public void gameResult(Long gameRoomId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(GAME_ROOM_NOT_FOUND));

        Long winnerId = gameRoom.getWinnerId();

        Account player1 = gameRoom.getPlayer1();
        Account player2 = gameRoom.getPlayer2();

        if (winnerId == null) { // 무승부
            player1.increasePoint(POINT_DRAW_GAME);
            player1.updateRankScore(DRAW_RANK_SCORE);

            player2.increasePoint(POINT_DRAW_GAME);
            player2.updateRankScore(DRAW_RANK_SCORE);
            return;
        }

        if (winnerId.equals(player1.getId())) {
            //player1 승리
            player1.increasePoint(POINT_FOR_WINNER);
            player1.updateRankScore(WINNER_RANK_SCORE);

            player2.increasePoint(POINT_FOR_LOSER);
            player2.updateRankScore(LOSER_RANK_SCORE);
        } else if (winnerId.equals(player2.getId())) {
            //player2 승리
            player2.increasePoint(POINT_FOR_WINNER);
            player2.updateRankScore(WINNER_RANK_SCORE);

            player1.increasePoint(POINT_FOR_LOSER);
            player1.updateRankScore(LOSER_RANK_SCORE);
        }
    }
}
