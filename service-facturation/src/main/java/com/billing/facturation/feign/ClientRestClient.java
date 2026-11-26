package com.billing.facturation.feign;

import com.billing.facturation.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Client OpenFeign type vers service-clients. "service-clients" est resolu via Eureka (+ load balancing). */
@FeignClient(name = "service-clients")
public interface ClientRestClient {

    @GetMapping("/clients/{id}")
    ClientDTO getClient(@PathVariable("id") Long id);
}
