package com.gomdolbook.api.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.gomdolbook.api.repository.jpa"})
@Import(DataSourceAutoConfiguration.class)
public class JpaConfig {

}
