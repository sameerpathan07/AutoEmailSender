package com.sameer.coldmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ColdMailApplication {
    public static void main(String[] args) {
        SpringApplication.run(ColdMailApplication.class, args);
    }
}
