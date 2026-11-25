package com.example.demo.config;

import com.example.demo.security.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class TripDeliveryAsyncConfig {

    @Bean(name = "tripDeliveryExecutor")
    public Executor tripDeliveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("trip-delivery-");
        executor.setTaskDecorator(runnable -> {
            long tenantId = TenantContext.requireTenantId();
            return () -> {
                TenantContext.setTenantId(tenantId);
                try {
                    runnable.run();
                } finally {
                    TenantContext.clear();
                }
            };
        });
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
