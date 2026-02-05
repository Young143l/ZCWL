package com.example.zcwl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication
@EnableRetry
public class ZcwlApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZcwlApplication.class, args);
    }

}