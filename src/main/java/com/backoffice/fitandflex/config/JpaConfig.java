package com.backoffice.fitandflex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de JPA/Hibernate
 * Spring Boot auto-configuration will handle the rest
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.backoffice.fitandflex.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Spring Boot will automatically configure DataSource, EntityManagerFactory, and TransactionManager
    // based on the properties in application-prod.properties
}
