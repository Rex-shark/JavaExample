package org.example.webcrawlerdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 排程任務線程池配置
 * 允許多個排程任務同時執行，避免任務串行等待
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 線程池大小（最多同時執行幾個任務）
        scheduler.setPoolSize(10);

        // 線程名稱前綴（方便日誌追蹤）
        scheduler.setThreadNamePrefix("scheduled-task-");

        // 拒絕策略：當線程池滿時，由呼叫者執行
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任務完成才關閉
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // 最多等待 60 秒
        scheduler.setAwaitTerminationSeconds(60);

        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);

        log.info("✅ 排程任務線程池已配置：PoolSize = {}", scheduler.getPoolSize());
    }
}
