package com.billing.abonnements.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** DTO de la requete entrante (creation / modification d'un plan) : validation + mapping vers l'entite Plan. */
public record PlanRequest(
        @NotBlank(message = "Le nom du plan est obligatoire")
        @Size(max = 100, message = "Le nom du plan ne doit pas dépasser 100 caractères")
        String nom,

        @NotNull(message = "Le prix mensuel est obligatoire")
        @DecimalMin(value = "0.0", message = "Le prix mensuel doit être supérieur ou égal à 0")
        @Digits(integer = 10, fraction = 2, message = "Le prix mensuel doit contenir au maximum 2 décimales")
        BigDecimal prixMensuel,

        @NotNull(message = "Le quota d'utilisateurs est obligatoire")
        @Min(value = 1, message = "Le quota doit être supérieur ou égal à 1")
        Integer quotaUtilisateurs,

        Boolean actif) { }
