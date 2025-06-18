package chimhaha.chimcard.utils;

import chimhaha.chimcard.entity.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CardUtils {

    public static <T> Map<T, Long> cardCountMap(List<T> cards) {
        return cards.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static Map<Long, AccountCard> accountCardMapLong(List<AccountCard> accountCards) {
        return accountCards.stream().collect(Collectors.toMap(ac -> ac.getCard().getId(), Function.identity()));
    }

    public static Map<Card, AccountCard> accountCardMapCard(List<AccountCard> accountCards) {
        return accountCards.stream().collect(Collectors.toMap(AccountCard::getCard, Function.identity()));
    }

    public static Map<Card, Long> getPostCards(TradePost tradePost) {
        return tradePost.getOwnerCards().stream()
                .collect(Collectors.toMap(TradePostCard::getCard, TradePostCard::getCount));
    }

    public static Map<Card, Long> getRequestCards(TradeRequest tradeRequest) {
        return tradeRequest.getRequesterCards().stream()
                .collect(Collectors.toMap(TradeRequestCard::getCard, TradeRequestCard::getCount));
    }
}
