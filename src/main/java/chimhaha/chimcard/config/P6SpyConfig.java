package chimhaha.chimcard.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;
import java.util.Locale;

@Configuration
public class P6SpyConfig implements MessageFormattingStrategy {

    // 느린 쿼리 임계값 (ms)
    private static final long SLOW_QUERY_THRESHOLD_MS = 500;

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        // 쿼리가 없거나 임계값보다 짧으면 로그 출력 안 함
        if (sql == null || sql.trim().isEmpty() || elapsed < SLOW_QUERY_THRESHOLD_MS) {
            return String.format("[%s] | %d ms", category, elapsed);
        }

        return String.format("[SLOW QUERY] [%s] | %d ms | %s", category, elapsed, formatSql(category, sql));
    }

    private String formatSql(String category, String sql) {
        if (Category.STATEMENT.getName().equals(category)) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter") || trimmedSQL.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }
            return sql;
        }
        return sql;
    }
}
