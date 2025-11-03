package org.example.booking.config;

import org.example.booking.repository.base.SliceableRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = "org.example.booking.repository",
        repositoryBaseClass = SliceableRepositoryImpl.class
)
public class JpaConfig {
}