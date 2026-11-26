package com.billing.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Trace de chaque notification traitee : preuve de la reception du message et de l'envoi (ou de l'echec) du mail. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;
    private Long clientId;
    private String destinataire;
    private String sujet;

    @Column(length = 2000)
    private String contenu;

    @Enumerated(EnumType.STRING)
    private NotificationStatus statut;

    @Column(length = 500)
    private String erreur;

    private LocalDateTime dateReception;
    private LocalDateTime dateEnvoi;
}
