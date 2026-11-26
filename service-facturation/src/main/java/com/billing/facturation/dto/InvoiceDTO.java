package com.billing.facturation.dto;

import com.billing.facturation.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDTO(Long id, Long clientId, Long subscriptionId, String periode, InvoiceStatus statut,
                         LocalDate dateEmission, LocalDate dateEcheance, BigDecimal montantHT, BigDecimal tva,
                         BigDecimal montantTTC, List<InvoiceLineDTO> lignes) { }
