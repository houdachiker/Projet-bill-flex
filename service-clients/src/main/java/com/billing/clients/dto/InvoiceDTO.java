package com.billing.clients.dto;

import java.math.BigDecimal;

/** Vue d'une facture de service-facturation (DTO dedie au client Feign). */
public record InvoiceDTO(Long id, Long clientId, String statut, BigDecimal montantTTC) { }
