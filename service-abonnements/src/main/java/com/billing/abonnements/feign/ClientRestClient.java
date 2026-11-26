package com.billing.abonnements.feign;

import com.billing.abonnements.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-clients")
public interface ClientRestClient {

    @GetMapping("/clients/{id}")
    ClientDTO getClient(@PathVariable("id") Long id);
}