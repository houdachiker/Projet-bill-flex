package com.billing.clients.feign;

import com.billing.clients.dto.InvoiceDTO;
import com.billing.clients.exception.ResourceNotFoundException;
import com.billing.clients.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * V4 + V6 : appel protege (Retry + CircuitBreaker + timeout Feign) vers service-facturation.
 * Un 404 distant devient une ResourceNotFoundException : on ne peut pas payer une facture qui n'existe pas.
 */
@Component
public class RemoteInvoiceService {
    private static final Logger log = LoggerFactory.getLogger(RemoteInvoiceService.class);

    private final InvoiceRestClient invoiceRestClient;

    public RemoteInvoiceService(InvoiceRestClient invoiceRestClient) { this.invoiceRestClient = invoiceRestClient; }

    @Retry(name = "invoiceService")
    @CircuitBreaker(name = "invoiceService", fallbackMethod = "circuitOpen")
    public InvoiceDTO getInvoice(Long id) {
        try {
            return invoiceRestClient.getInvoice(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Facture introuvable : " + id);
        }
    }

    @SuppressWarnings("unused")
    private InvoiceDTO circuitOpen(Long id, CallNotPermittedException e) {
        log.warn("Circuit breaker OUVERT pour service-facturation");
        throw new ServiceUnavailableException("service-facturation indisponible (circuit breaker ouvert)", e);
    }
}
