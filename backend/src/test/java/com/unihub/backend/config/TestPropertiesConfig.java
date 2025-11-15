package com.unihub.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@TestConfiguration
public class TestPropertiesConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        // Importante: isso impede que o contexto morra se faltar alguma ${...}
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
}
