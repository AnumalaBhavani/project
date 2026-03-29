package com.alumni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlumniPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlumniPlatformApplication.class, args);
    }
}
