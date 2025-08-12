package chimhaha.chimcard.game.service;

import chimhaha.chimcard.game.dto.RankResponseDto;
import chimhaha.chimcard.game.repository.GameCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankService {

    private final GameCustomRepository gameCustomRepository;

    public List<RankResponseDto> top10Rank() {
        return gameCustomRepository.top10Rank();
    }
}
