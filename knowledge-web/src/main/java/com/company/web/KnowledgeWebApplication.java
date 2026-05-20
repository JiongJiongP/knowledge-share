package com.company.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company")
public class KnowledgeWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeWebApplication.class, args);
    }
}
