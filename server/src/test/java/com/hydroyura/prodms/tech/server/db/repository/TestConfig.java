package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.server.config.DatabaseConfig;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(value = {DatabaseConfig.class, JdbcTemplateAutoConfiguration.class})
public class TestConfig {

}
