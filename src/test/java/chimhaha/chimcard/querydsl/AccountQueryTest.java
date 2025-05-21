package chimhaha.chimcard.querydsl;

import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.entity.QAccount;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AccountQueryTest extends QueryDslTest {
    
    @Test
    void findMemberByQueryDsl() throws Exception {
        //given
        List<Account> list = query.selectFrom(QAccount.account).fetch();
        //when

        //then
        System.out.println(list);
    }
}
