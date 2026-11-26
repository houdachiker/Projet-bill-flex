package com.billing.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Contrat du message publie par service-facturation (memes champs, serialise en JSON). */
public record InvoiceEmittedEvent(
        Long invoiceId,
        Long clientId,
        String clientNom,
        String clientEmail,
        String periode,
        BigDecimal montantTTC,
        LocalDate dateEmission,
        LocalDate dateEcheance) { }
