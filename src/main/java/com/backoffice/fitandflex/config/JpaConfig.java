package com.backoffice.fitandflex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuración de JPA/Hibernate
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.backoffice.fitandflex.repository")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Configuración del EntityManagerFactory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.backoffice.fitandflex.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Configuración de propiedades JPA
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.setProperty("hibernate.show_sql", "true");
        jpaProperties.setProperty("hibernate.format_sql", "true");
        jpaProperties.setProperty("hibernate.use_sql_comments", "true");
        
        // Configuración de rendimiento
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "20");
        jpaProperties.setProperty("hibernate.order_inserts", "true");
        jpaProperties.setProperty("hibernate.order_updates", "true");
        jpaProperties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // Configuración de conexión
        jpaProperties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        jpaProperties.setProperty("hibernate.connection.autocommit", "false");
        
        em.setJpaProperties(jpaProperties);
        
        return em;
    }

    /**
     * Configuración del TransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
