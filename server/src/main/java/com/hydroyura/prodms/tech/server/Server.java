package com.hydroyura.prodms.tech.server;

import com.hydroyura.prodms.tech.server.props.DefaultParams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = DefaultParams.class)
public class Server {

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

}
