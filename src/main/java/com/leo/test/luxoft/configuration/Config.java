package com.leo.test.luxoft.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Senchenko Victor
 */
@Configuration
public class Config {
    @Value("${spring.my.thread.pool.size}")
    private int poolSize;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(poolSize);
    }
}
