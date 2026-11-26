package com.billing.facturation.dto;

/** V4 - DTO compose : la facture + les donnees agregees depuis service-clients et service-abonnements. */
public record InvoiceDetailsDTO(InvoiceDTO invoice, ClientDTO client, SubscriptionDTO subscription) { }
