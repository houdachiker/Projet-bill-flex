package com.billing.clients.dto;

import com.billing.clients.model.ModePaiement;

public record ClientDTO(Long id,
     String nom, 
     String email, 
     String numeroTva,
      ModePaiement modePaiement) { }
