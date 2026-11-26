package com.billing.abonnements.dto;

/** created = false : la creation etait un rejeu (meme client + meme plan deja ACTIF) -> idempotence. */
public record SubscriptionCreationResult(SubscriptionDTO subscription, boolean created) { }
