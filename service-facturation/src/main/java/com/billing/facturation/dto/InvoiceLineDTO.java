package com.billing.facturation.dto;

import java.math.BigDecimal;

public record InvoiceLineDTO(Long id, String description, int quantite, BigDecimal prixUnitaire, BigDecimal montant) { }
