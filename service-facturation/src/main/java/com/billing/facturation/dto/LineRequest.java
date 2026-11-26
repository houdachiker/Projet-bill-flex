package com.billing.facturation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LineRequest(
        @NotBlank(message = "La description de la ligne est obligatoire")
        @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
        String description,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être supérieure ou égale à 1")
        Integer quantite,

        @NotNull(message = "Le prix unitaire est obligatoire")
        @DecimalMin(value = "0.0", message = "Le prix unitaire doit être supérieur ou égal à 0")
        @Digits(integer = 10, fraction = 2, message = "Le prix unitaire doit contenir au maximum 2 décimales")
        BigDecimal prixUnitaire) { }
