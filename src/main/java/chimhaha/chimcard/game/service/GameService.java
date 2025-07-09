package chimhaha.chimcard.game.service;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.GameRoom;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.GameInfoDto;
import chimhaha.chimcard.game.dto.MyGameCardDto;
import chimhaha.chimcard.game.repository.GameCardRepository;
import chimhaha.chimcard.game.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRoomRepository gameRoomRepository;
    private final GameCardRepository gameCardRepository;

    /**
     * 게임 입장 시 상대 닉네임과 내 게임 카드 로딩
     */
    public GameInfoDto gameInfo(Long gameRoomId, Long playerId) {
        GameRoom gameRoom = gameRoomRepository.findWithPlayersById(gameRoomId) // fetchJoin 으로 조회
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게임입니다."));

        Account player1 = gameRoom.getPlayer1();
        Account player2 = gameRoom.getPlayer2();

        // 상대 닉네임 가져오기
        String otherPlayer = player1.getId().equals(playerId) ? player2.getNickname() : player1.getNickname();

        // 내 카드 가져오기
        List<MyGameCardDto> myCards = gameCardRepository.findWithCardByGameRoomAndPlayerId(gameRoomId, playerId)
                .stream().map(card -> new MyGameCardDto(card.getCard())).toList();

        return new GameInfoDto(otherPlayer, myCards);
    }
}
