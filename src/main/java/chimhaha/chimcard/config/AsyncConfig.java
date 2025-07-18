package chimhaha.chimcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Core thread 수 (최소)
        executor.setMaxPoolSize(10); // Max thread 수 (최대)
        executor.setQueueCapacity(50); // 대기열 용량
        executor.setThreadNamePrefix("TradeAsync-");
        executor.initialize();

        return executor;
    }
}
