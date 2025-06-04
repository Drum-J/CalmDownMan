package chimhaha.chimcard.trade.service;

import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.TradePost;
import chimhaha.chimcard.entity.TradePostCard;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.trade.dto.TradePostCardRequestDto;
import chimhaha.chimcard.trade.repository.TradePostRepository;
import chimhaha.chimcard.user.repository.AccountRepository;
import chimhaha.chimcard.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final CardCustomRepository cardCustomRepository;
    private final AccountRepository accountRepository;
    private final AccountCardRepository accountCardRepository;
    private final TradePostRepository tradePostRepository;

    @Transactional
    public void tradePost(Long accountId, TradePostCardRequestDto dto) {
        Account owner = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        Map<Long, Long> cardCountMap = CardUtils.cardCountMap(dto.cardIds());

        List<AccountCard> accountCards = cardCustomRepository.getMyCardByCardIds(accountId, cardCountMap.keySet());
        Map<Long, AccountCard> ownerCardMap = CardUtils.accountCardMapLong(accountCards);

        TradePost tradePost = new TradePost(owner, dto.title(), dto.content());

        for (Map.Entry<Long, Long> entry : cardCountMap.entrySet()) {
            Long cardId = entry.getKey();
            Long count = entry.getValue();

            AccountCard accountCard = ownerCardMap.get(cardId);
            if (accountCard == null) {
                throw new ResourceNotFoundException("보유하지 않은 카드입니다. cardId=" + cardId);
            }

            if (accountCard.decreaseCount(count)) {
                accountCardRepository.delete(accountCard);
            }

            new TradePostCard(tradePost, accountCard.getCard(), count);
        }

        tradePostRepository.save(tradePost);
    }
}
