package com.billing.facturation.dto;

import java.math.BigDecimal;

public record PlanDTO(Long id, String nom, BigDecimal prixMensuel, int quotaUtilisateurs, boolean actif) { }
