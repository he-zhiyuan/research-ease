package com.researchease;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.researchease.mapper")
public class ResearchEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchEaseApplication.class, args);
    }
}
