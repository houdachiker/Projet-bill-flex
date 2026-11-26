package com.billing.abonnements.dto;

import com.billing.abonnements.model.SubscriptionStatus;
import java.time.LocalDate;

public record SubscriptionDTO(Long id, Long clientId, PlanDTO plan, LocalDate dateDebut, LocalDate dateFin,
                              LocalDate prochaineEcheance, SubscriptionStatus statut) { }
