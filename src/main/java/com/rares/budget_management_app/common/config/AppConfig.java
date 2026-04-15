package com.rares.budget_management_app.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}