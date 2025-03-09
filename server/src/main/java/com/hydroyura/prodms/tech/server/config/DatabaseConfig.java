package com.hydroyura.prodms.tech.server.config;

import com.hydroyura.prodms.tech.server.db.entity.Blank;
import com.hydroyura.prodms.tech.server.db.entity.Equipment;
import com.hydroyura.prodms.tech.server.db.entity.EquipmentSet;
import com.hydroyura.prodms.tech.server.db.entity.Process;
import com.hydroyura.prodms.tech.server.db.entity.ProcessStep;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DatabaseConfig {

    @Value("${db.connection.string}")
    private String connectionString;

    @Bean
    @Order(1)
    DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(connectionString);
        return new HikariDataSource(config);
    }

    @Bean
    @Order(2)
    Flyway flyway(DataSource dataSource) {
        var flyway =  Flyway
            .configure()
            .locations("classpath:db/migration")
            .dataSource(dataSource)
            .baselineOnMigrate(Boolean.TRUE)
            .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    @Order(3)
    Collection<Class<?>> entityClasses() {
        return List.of(Equipment.class, EquipmentSet.class, Process.class, Blank.class, ProcessStep.class);
    }

    @Bean
    @Order(4)
    EntityManagerFactory entityManagerFactory(DataSource dataSource, Collection<Class<?>> entityClasses) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.connection.driver_class", hikariDataSource.getDriverClassName());
        configuration.setProperty("hibernate.connection.url", hikariDataSource.getJdbcUrl());

        entityClasses.forEach(configuration::addAnnotatedClass);

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            return  sessionFactory.unwrap(EntityManagerFactory.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }







}
