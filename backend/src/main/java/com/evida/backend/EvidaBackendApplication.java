package com.evida.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EvidaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidaBackendApplication.class, args);
    }
}