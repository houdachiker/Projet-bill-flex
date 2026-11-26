package com.billing.facturation.dto;

import java.time.LocalDate;

/** Vue d'un abonnement de service-abonnements (plan inclus). */
public record SubscriptionDTO(Long id, Long clientId, PlanDTO plan, LocalDate dateDebut, LocalDate dateFin,
                              LocalDate prochaineEcheance, String statut) { }
