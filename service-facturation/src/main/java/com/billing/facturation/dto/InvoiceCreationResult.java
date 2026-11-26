package com.billing.facturation.dto;

/** created = false quand la creation etait un rejeu (meme abonnement + meme periode) : idempotence. */
public record InvoiceCreationResult(InvoiceDTO invoice, boolean created) { }
