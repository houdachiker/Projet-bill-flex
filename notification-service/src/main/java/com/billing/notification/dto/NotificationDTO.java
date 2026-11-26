package com.billing.notification.dto;

import com.billing.notification.model.NotificationStatus;
import java.time.LocalDateTime;

public record NotificationDTO(Long id, Long invoiceId, Long clientId, String destinataire, String sujet, String contenu,
                              NotificationStatus statut, String erreur, LocalDateTime dateReception,
                              LocalDateTime dateEnvoi) { }
