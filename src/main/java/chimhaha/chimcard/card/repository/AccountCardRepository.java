package chimhaha.chimcard.card.repository;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.AccountCard;
import chimhaha.chimcard.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountCardRepository extends JpaRepository<AccountCard, Long> {
    Optional<AccountCard> findByAccountAndCard(Account account, Card card);

    List<AccountCard> findByAccount(Account account);
}
