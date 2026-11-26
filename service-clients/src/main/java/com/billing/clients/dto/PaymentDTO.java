package com.billing.clients.dto;

import com.billing.clients.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(Long id, Long invoiceId, Long clientId, BigDecimal montant, PaymentStatus statut,
                         LocalDateTime datePaiement) { }
