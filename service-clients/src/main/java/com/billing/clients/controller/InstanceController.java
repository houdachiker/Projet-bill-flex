package com.billing.clients.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * V2/V3 - Sert a prouver le LOAD BALANCING : si on lance 2 instances de service-clients (ports 8081 et 9081),
 * des appels repetes via la gateway montrent alternativement les 2 ports.
 */
@RestController
public class InstanceController {

    @Value("${spring.application.name}")
    private String application;

    @Value("${server.port}")
    private String port;

    @GetMapping("/instance-info")
    public Map<String, String> info() {
        return Map.of("service", application, "port", port);
    }
}
