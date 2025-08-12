package chimhaha.chimcard.game.repository;

import chimhaha.chimcard.entity.GameStatus;
import chimhaha.chimcard.entity.QAccount;
import chimhaha.chimcard.game.dto.GameRecordDto;
import chimhaha.chimcard.game.dto.QGameRecordDto;
import chimhaha.chimcard.game.dto.QRankResponseDto;
import chimhaha.chimcard.game.dto.RankResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static chimhaha.chimcard.entity.GameStatus.FINISHED;
import static chimhaha.chimcard.entity.QAccount.*;
import static chimhaha.chimcard.entity.QGameRoom.gameRoom;

@Repository
@RequiredArgsConstructor
public class GameCustomRepository {

    private final JPAQueryFactory query;

    public List<GameRecordDto> getMyGameRecords(Long playerId) {
        QAccount player1 = new QAccount("player1");
        QAccount player2 = new QAccount("player2");

        return query
                .select(new QGameRecordDto(
                        player1.nickname,
                        player2.nickname,
                        new CaseBuilder()
                                .when(gameRoom.winnerId.eq(playerId)).then("win")
                                .when(gameRoom.winnerId.eq(0L)).then("draw")
                                .otherwise("lose"),
                        gameRoom.createAt
                ))
                .from(gameRoom)
                .join(player1)
                .on(gameRoom.player1().eq(player1))
                .join(player2)
                .on(gameRoom.player2().eq(player2))
                .where(
                        isPlayer(playerId, player1, player2),
                        statusEq(FINISHED)
                )
                .orderBy(gameRoom.createAt.desc())
                .limit(20)
                .fetch();
    }

    public List<RankResponseDto> top10Rank() {
        return query
                .select(
                        new QRankResponseDto(
                                account.username,
                                account.nickname,
                                account.profileImage,
                                account.rankScore,
                                account.win.doubleValue()
                                        .divide(account.win.add(account.draw).add(account.lose))
                                        .multiply(100)
                                        .round()
                                        .as("winRate")
                        )
                )
                .from(account)
                .orderBy(account.rankScore.desc())
                .limit(10)
                .fetch();
    }

    private BooleanExpression isPlayer(Long playerId, QAccount player1, QAccount player2) {
        return playerId != null
                ? player1.id.eq(playerId).or(player2.id.eq(playerId))
                : null;
    }

    private BooleanExpression statusEq(GameStatus gameStatus) {
        return gameStatus != null
                ? gameRoom.status.eq(gameStatus)
                : null;
    }
}
