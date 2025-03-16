package com.hydroyura.prodms.tech.server.config;

import com.hydroyura.prodms.tech.server.db.repository.EquipmentRepository;
import com.hydroyura.prodms.tech.server.db.repository.EquipmentRepositoryJdbcTemplateImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


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
    EquipmentRepository equipmentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                            JdbcTemplate jdbcTemplate) {
        return new EquipmentRepositoryJdbcTemplateImpl(namedParameterJdbcTemplate, jdbcTemplate);
    }





}
