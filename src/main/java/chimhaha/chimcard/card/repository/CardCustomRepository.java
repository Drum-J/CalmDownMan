package chimhaha.chimcard.card.repository;

import chimhaha.chimcard.card.dto.MyCardDetailDto;
import chimhaha.chimcard.card.dto.QMyCardDetailDto;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.Card;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static chimhaha.chimcard.entity.QAccount.account;
import static chimhaha.chimcard.entity.QAccountCard.accountCard;
import static chimhaha.chimcard.entity.QCard.card;
import static chimhaha.chimcard.entity.QCardSeason.cardSeason;
import static com.querydsl.jpa.JPAExpressions.select;

@Repository
@RequiredArgsConstructor
public class CardCustomRepository {
    private final JPAQueryFactory query;

    public List<MyCardDetailDto> getMyCards(Long accountId, Long cardSeasonId) {
        return query
                .select(new QMyCardDetailDto(
                        accountCard.card().id,
                        accountCard.card().title,
                        accountCard.card().attackType.stringValue(),
                        accountCard.card().grade,
                        accountCard.card().power,
                        accountCard.card().imageUrl,
                        accountCard.card().cardSeason().seasonName,
                        accountCard.count
                ))
                .from(accountCard)
                .where(accountCard.account().id.eq(accountId),accountCardSeasonEq(cardSeasonId))
                .orderBy(accountCard.card().id.asc())
                .fetch();
    }

    private BooleanExpression accountCardSeasonEq(Long cardSeasonId) {
        return cardSeasonId != null ? accountCard.card().cardSeason().id.eq(cardSeasonId) : null;
    }

    public List<Card> getNotMyCards(Long accountId, Long cardSeasonId) {
        return query
                .selectFrom(card)
                .where(card.id.notIn(
                        select(accountCard.card().id)
                            .from(accountCard)
                            .where(accountCard.account().id.eq(accountId))
                ), cardSeasonEq(cardSeasonId))
                .fetch();
    }

    private BooleanExpression cardSeasonEq(Long cardSeasonId) {
        return cardSeasonId != null ? cardSeason.id.eq(cardSeasonId) : null;
    }

    public List<Account> getCardOwner(Long id) {
        return query
                .selectFrom(account)
                .where(account.id.in(
                        select(accountCard.account().id)
                                .from(accountCard)
                                .where(accountCard.card().id.eq(id))
                ))
                .fetch();
    }

    public List<AccountCard> getMyCardByCardIds(Long accountId, Set<Long> cardIds) {
        return query
                .selectFrom(accountCard)
                .where(
                        accountCard.account().id.eq(accountId),
                        accountCard.card().id.in(cardIds)
                )
                .fetch();
    }

    public List<AccountCard> getMyCardByCards(Account account, Set<Card> cards) {
        return query
                .selectFrom(accountCard)
                .where(
                        accountCard.account().eq(account),
                        accountCard.card().in(cards)
                )
                .fetch();
    }
}
