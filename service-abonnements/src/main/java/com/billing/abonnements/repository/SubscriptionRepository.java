package com.billing.abonnements.repository;

import com.billing.abonnements.model.Subscription;
import com.billing.abonnements.model.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByClientId(Long clientId);

    /** Sert a l'idempotence de la creation : un client ne peut avoir qu'un abonnement ACTIF par plan. */
    Optional<Subscription> findFirstByClientIdAndPlanIdAndStatut(Long clientId, Long planId, SubscriptionStatus statut);
}
