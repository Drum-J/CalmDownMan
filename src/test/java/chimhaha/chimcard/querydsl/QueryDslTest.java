package chimhaha.chimcard.querydsl;

import chimhaha.chimcard.config.P6SpyConfig;
import chimhaha.chimcard.config.QueryDslConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Import({QueryDslConfig.class, P6SpyConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryDslTest {

    @Autowired protected JPAQueryFactory query;
}
