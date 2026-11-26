package com.billing.clients.dto;

import com.billing.clients.model.ModePaiement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO de la requete entrante (creation / modification d'un client) : validation + mapping vers l'entite Client. */
public record ClientRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        @Size(max = 254, message = "Email trop long")
        String email,

        @Size(max = 50, message = "Numéro de TVA trop long")
        String numeroTva,

        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement
) { }