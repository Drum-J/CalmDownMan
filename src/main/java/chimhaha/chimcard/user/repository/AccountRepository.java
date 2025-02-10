package chimhaha.chimcard.user.repository;

import chimhaha.chimcard.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
}
