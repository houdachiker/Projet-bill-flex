package com.billing.abonnements.dto;

import java.math.BigDecimal;

public record PlanDTO(
    Long id, 
    String nom,
     BigDecimal prixMensuel,
    int quotaUtilisateurs,
    boolean actif) { }
