package com.company.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company")
@MapperScan(value = "com.company", annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class KnowledgeWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeWebApplication.class, args);
    }
}
