package com.gomdolbook.api.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = {"com.gomdolbook.api.repository.reactive"})
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @NonNull
    @Primary
    @Bean
    public ConnectionFactory connectionFactory() {
        return MySqlConnectionFactory.from(MySqlConnectionConfiguration.builder()
            .host("127.0.0.1")
            .user("root")
            .port(3306)
            .password("11231123")
            .database("gomdolbook")
            .createDatabaseIfNotExist(true)
            .connectTimeout(Duration.ofSeconds(30))
            .build());
    }
}
