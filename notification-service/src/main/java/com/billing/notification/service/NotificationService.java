package com.billing.notification.service;

import com.billing.notification.dto.InvoiceEmittedEvent;
import com.billing.notification.dto.NotificationDTO;
import com.billing.notification.exception.ResourceNotFoundException;
import com.billing.notification.model.Notification;
import com.billing.notification.model.NotificationStatus;
import com.billing.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Traitement d'un evenement "facture emise" : construction du mail, envoi, trace en base. */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final JavaMailSender mailSender;
    private final String from;

    public NotificationService(NotificationRepository repository, JavaMailSender mailSender,
                               @Value("${notification.mail.from:billing@localhost}") String from) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.from = from;
    }

    public List<NotificationDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public NotificationDTO findById(Long id) {
        return toDTO(findEntityById(id));
    }

    private Notification findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
    }

    public List<NotificationDTO> findByInvoice(Long invoiceId) {
        return repository.findByInvoiceId(invoiceId).stream().map(this::toDTO).toList();
    }

    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getInvoiceId(), notification.getClientId(),
                notification.getDestinataire(), notification.getSujet(), notification.getContenu(),
                notification.getStatut(), notification.getErreur(), notification.getDateReception(),
                notification.getDateEnvoi());
    }

    public void handleInvoiceEmitted(InvoiceEmittedEvent e) {
        log.info("[CONSUMER] Evenement recu : facture {} (client {})", e.invoiceId(), e.clientId());

        // Idempotence cote consumer : RabbitMQ garantit "au moins une fois", un meme message peut etre livre 2 fois.
        if (repository.existsByInvoiceIdAndStatut(e.invoiceId(), NotificationStatus.SENT)) {
            log.warn("[CONSUMER] Facture {} deja notifiee : message ignore (doublon)", e.invoiceId());
            return;
        }

        Notification n = new Notification();
        n.setInvoiceId(e.invoiceId());
        n.setClientId(e.clientId());
        n.setDestinataire(e.clientEmail());
        n.setSujet("Facture " + e.invoiceId() + " - periode " + e.periode());
        n.setContenu(buildBody(e));
        n.setDateReception(LocalDateTime.now());

        if (e.clientEmail() == null || e.clientEmail().isBlank()) {
            n.setStatut(NotificationStatus.FAILED);
            n.setErreur("Adresse e-mail du client inconnue (service-clients indisponible lors de l'emission)");
            repository.save(n);
            log.error("[CONSUMER] Facture {} : pas d'adresse e-mail, notification en echec", e.invoiceId());
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(e.clientEmail());
            mail.setSubject(n.getSujet());
            mail.setText(n.getContenu());
            mailSender.send(mail);
            n.setStatut(NotificationStatus.SENT);
            n.setDateEnvoi(LocalDateTime.now());
            log.info("[CONSUMER] Mail ENVOYE a {} pour la facture {}", e.clientEmail(), e.invoiceId());
        } catch (Exception ex) {
            n.setStatut(NotificationStatus.FAILED);
            n.setErreur(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            log.error("[CONSUMER] Echec d'envoi du mail pour la facture {} : {}", e.invoiceId(), ex.getMessage());
        }
        repository.save(n);
    }

    private String buildBody(InvoiceEmittedEvent e) {
        return "Bonjour " + (e.clientNom() != null ? e.clientNom() : "") + ",\n\n"
                + "Votre facture numero " + e.invoiceId() + " (periode " + e.periode() + ") vient d'etre emise.\n"
                + "Montant TTC : " + e.montantTTC() + " EUR\n"
                + "Date d'emission : " + e.dateEmission() + "\n"
                + "Date d'echeance : " + e.dateEcheance() + "\n\n"
                + "Cordialement,\nService Facturation";
    }
}
