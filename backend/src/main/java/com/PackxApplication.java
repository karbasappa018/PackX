package com.packx.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PackxApplication {

    public static void main(String[] args) {
        SpringApplication.run(PackxApplication.class, args);
    }
}
