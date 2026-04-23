package com.example.ara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AraApplication {
    public static void main(String[] args) {
        SpringApplication.run(AraApplication.class, args);
    }
}
