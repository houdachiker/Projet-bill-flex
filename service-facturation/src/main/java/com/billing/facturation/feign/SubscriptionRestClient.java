package com.billing.facturation.feign;

import com.billing.facturation.dto.SubscriptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Client OpenFeign type vers service-abonnements. */
@FeignClient(name = "service-abonnements")
public interface SubscriptionRestClient {

    @GetMapping("/subscriptions/{id}")
    SubscriptionDTO getSubscription(@PathVariable("id") Long id);
}
