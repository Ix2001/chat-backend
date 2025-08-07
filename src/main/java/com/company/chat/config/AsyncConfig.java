package com.company.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.Executor;

/**
 * Конфигурация пула для @Async (обработки событий).
 */
@Configuration
public class AsyncConfig {
    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(10);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("evt-");
        exec.initialize();
        return exec;
    }
}
