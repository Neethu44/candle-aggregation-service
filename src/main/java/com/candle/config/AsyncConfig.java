package com.candle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "boundedTaskExecutor")
    public Executor boundedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Base number of threads
        executor.setCorePoolSize(50);
        // Maximum number of threads when queue is full
        executor.setMaxPoolSize(500);
        // Bounded queue: uses LinkedBlockingQueue under the hood
        executor.setQueueCapacity(750);
        executor.setThreadNamePrefix("CandleExec-");
        // If queue gets completely full, fallback to running in the caller's thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
