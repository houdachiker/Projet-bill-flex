package com.billing.facturation.dto;

/** Vue d'un client de service-clients (DTO dedie au client Feign : seuls les champs utiles). */
public record ClientDTO(Long id, String nom, String email) { }
