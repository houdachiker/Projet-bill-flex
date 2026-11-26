package com.billing.facturation.feign;

import com.billing.facturation.dto.ClientDTO;
import com.billing.facturation.dto.SubscriptionDTO;
import com.billing.facturation.exception.ResourceNotFoundException;
import com.billing.facturation.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * V4 + V6 : point d'entree UNIQUE vers les services distants.
 *
 *  - V4 : traduit les erreurs Feign en exceptions metier (404 distant -> ResourceNotFoundException).
 *  - V6 : chaque appel est protege par un Retry (reessaie 3 fois si le service est down/lent)
 *         puis un CircuitBreaker (coupe les appels si trop d'echecs). Le timeout est configure sur Feign
 *         (spring.cloud.openfeign.client.config.default.read-timeout). Voir config-repo/service-facturation.properties.
 *
 * Ordre des aspects Resilience4j : Retry( CircuitBreaker( appel Feign ) ).
 */
@Component
public class RemoteReferenceService {
    private static final Logger log = LoggerFactory.getLogger(RemoteReferenceService.class);

    private final ClientRestClient clientRestClient;
    private final SubscriptionRestClient subscriptionRestClient;

    public RemoteReferenceService(ClientRestClient clientRestClient, SubscriptionRestClient subscriptionRestClient) {
        this.clientRestClient = clientRestClient;
        this.subscriptionRestClient = subscriptionRestClient;
    }

    @Retry(name = "clientService")
    @CircuitBreaker(name = "clientService", fallbackMethod = "clientCircuitOpen")
    public ClientDTO getClient(Long id) {
        try {
            return clientRestClient.getClient(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Client introuvable : " + id);
        }
    }

    @Retry(name = "subscriptionService")
    @CircuitBreaker(name = "subscriptionService", fallbackMethod = "subscriptionCircuitOpen")
    public SubscriptionDTO getSubscription(Long id) {
        try {
            return subscriptionRestClient.getSubscription(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Abonnement introuvable : " + id);
        }
    }

    // Fallbacks : appeles UNIQUEMENT quand le circuit est ouvert (appel refuse sans meme tenter le reseau).
    @SuppressWarnings("unused")
    private ClientDTO clientCircuitOpen(Long id, CallNotPermittedException e) {
        log.warn("Circuit breaker OUVERT pour service-clients");
        throw new ServiceUnavailableException("service-clients indisponible (circuit breaker ouvert)", e);
    }

    @SuppressWarnings("unused")
    private SubscriptionDTO subscriptionCircuitOpen(Long id, CallNotPermittedException e) {
        log.warn("Circuit breaker OUVERT pour service-abonnements");
        throw new ServiceUnavailableException("service-abonnements indisponible (circuit breaker ouvert)", e);
    }
}
