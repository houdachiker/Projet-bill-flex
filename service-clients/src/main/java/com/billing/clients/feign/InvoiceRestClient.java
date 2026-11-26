package com.billing.clients.feign;

import com.billing.clients.dto.InvoiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Client OpenFeign type vers service-facturation (resolu via Eureka). */
@FeignClient(name = "service-facturation")
public interface InvoiceRestClient {

    @GetMapping("/invoices/{id}")
    InvoiceDTO getInvoice(@PathVariable("id") Long id);
}
