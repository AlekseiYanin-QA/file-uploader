package com.edu.fileupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ForkJoinPool;

/**
 * Конфигурация для настройки Fork/Join пула.
 */
@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors()); // Создаем пул с количеством потоков, равным количеству ядер процессора
    }
}