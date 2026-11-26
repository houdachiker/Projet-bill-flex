package com.billing.abonnements.feign;

import com.billing.abonnements.dto.ClientDTO;
import com.billing.abonnements.exception.ResourceNotFoundException;
import com.billing.abonnements.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * V4 + V6 : appel protege vers service-clients.
 *  - V4 : un 404 distant devient une ResourceNotFoundException (validation prealable de la reference clientId).
 *  - V6 : Retry (3 tentatives) + CircuitBreaker ; timeout = read-timeout Feign (config-repo/service-abonnements.properties).
 */
@Component
public class RemoteClientService {
    private static final Logger log = LoggerFactory.getLogger(RemoteClientService.class);

    private final ClientRestClient clientRestClient;

    public RemoteClientService(ClientRestClient clientRestClient) { this.clientRestClient = clientRestClient; }

    @Retry(name = "clientService")
    @CircuitBreaker(name = "clientService", fallbackMethod = "circuitOpen")
    public ClientDTO getClient(Long id) {
        try {
            return clientRestClient.getClient(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Client introuvable : " + id);
        }
    }

    /** Fallback : appele uniquement quand le circuit est ouvert. */
    @SuppressWarnings("unused")
    private ClientDTO circuitOpen(Long id, CallNotPermittedException e) {
        log.warn("Circuit breaker OUVERT pour service-clients");
        throw new ServiceUnavailableException("service-clients indisponible (circuit breaker ouvert)", e);
    }
}
