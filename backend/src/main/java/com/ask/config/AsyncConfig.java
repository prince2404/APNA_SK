package com.ask.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for background tasks (audit logging, email, SMS, notifications).
 * These tasks run in a separate thread pool so they never slow down API responses.
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Value("${ask.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${ask.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${ask.async.queue-capacity:100}")
    private int queueCapacity;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("ASK-Async-");
        executor.initialize();
        log.info("Async executor configured: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }
}
