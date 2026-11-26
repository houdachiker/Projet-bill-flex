package com.billing.facturation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FacturationApplication {
    public static void main(String[] args) {
        SpringApplication.run(FacturationApplication.class, args);
    }
}