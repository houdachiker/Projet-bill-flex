package com.billing.facturation.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

/** V5 - Message publie dans RabbitMQ quand une facture passe a l'etat EMISE. */
public record InvoiceEmittedEvent(
        Long invoiceId,
        Long clientId,
        String clientNom,
        String clientEmail,
        String periode,
        BigDecimal montantTTC,
        LocalDate dateEmission,
        LocalDate dateEcheance) { }
