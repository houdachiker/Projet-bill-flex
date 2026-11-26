package com.billing.facturation.service;

import com.billing.facturation.dto.ClientDTO;
import com.billing.facturation.dto.CreateInvoiceRequest;
import com.billing.facturation.dto.InvoiceCreationResult;
import com.billing.facturation.dto.InvoiceDetailsDTO;
import com.billing.facturation.dto.InvoiceDTO;
import com.billing.facturation.dto.InvoiceLineDTO;
import com.billing.facturation.dto.LineRequest;
import com.billing.facturation.dto.SubscriptionDTO;
import com.billing.facturation.exception.BusinessException;
import com.billing.facturation.exception.ResourceNotFoundException;
import com.billing.facturation.feign.RemoteReferenceService;
import com.billing.facturation.messaging.InvoiceEmittedEvent;
import com.billing.facturation.model.Invoice;
import com.billing.facturation.model.InvoiceLine;
import com.billing.facturation.model.InvoiceStatus;
import com.billing.facturation.repository.InvoiceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository repository;
    private final RemoteReferenceService remote;           // V4 : appels Feign (resilients, V6)
    private final ApplicationEventPublisher eventPublisher; // V5 : evenement -> RabbitMQ
    private final BigDecimal tvaRate;

    public InvoiceService(InvoiceRepository repository,
                          RemoteReferenceService remote,
                          ApplicationEventPublisher eventPublisher,
                          @Value("${facturation.tva-rate:0.20}") BigDecimal tvaRate) {
        this.repository = repository;
        this.remote = remote;
        this.eventPublisher = eventPublisher;
        this.tvaRate = tvaRate;
    }

    public List<InvoiceDTO> findAll() { return repository.findAll().stream().map(this::toDTO).toList(); }

    public InvoiceDTO findById(@Positive Long id) {
        return toDTO(findEntityById(id));
    }

    private Invoice findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
    }

    /** V4 - Consultation enrichie : facture + client (service-clients) + abonnement/plan (service-abonnements). */
    public InvoiceDetailsDTO findDetails(@Positive Long id) {
        Invoice inv = findEntityById(id);
        ClientDTO client = remote.getClient(inv.getClientId());
        SubscriptionDTO subscription = inv.getSubscriptionId() != null
                ? remote.getSubscription(inv.getSubscriptionId())
                : null;
        return new InvoiceDetailsDTO(toDTO(inv), client, subscription);
    }

    /**
     * Creation d'une facture.
     *  V6 - IDEMPOTENCE : rejouer la meme requete (meme abonnement + meme periode) ne cree pas de doublon,
     *       la facture deja creee est renvoyee (created = false -> HTTP 200 au lieu de 201).
     *  V4 - VALIDATION PREALABLE des references distantes : le client doit exister, l'abonnement doit exister,
     *       appartenir a ce client et etre ACTIF. Sinon : 404 / 409 et RIEN n'est enregistre.
     */
    public InvoiceCreationResult create(@Valid CreateInvoiceRequest r) {
        boolean hasSubscription = r.subscriptionId() != null;
        if (hasSubscription && (r.periode() == null || r.periode().isBlank())) {
            throw new BusinessException("La periode est obligatoire quand un abonnement est indique");
        }

        if (hasSubscription) {
            Optional<Invoice> existing = repository.findBySubscriptionIdAndPeriode(r.subscriptionId(), r.periode());
            if (existing.isPresent()) {
                Invoice inv = existing.get();
                if (!inv.getClientId().equals(r.clientId())) {
                    throw new BusinessException("Une facture existe deja pour cet abonnement et cette periode (autre client)");
                }
                log.info("Creation rejouee : facture {} deja existante (abonnement {}, periode {})",
                        inv.getId(), r.subscriptionId(), r.periode());
                return new InvoiceCreationResult(toDTO(inv), false);
            }
        }

        remote.getClient(r.clientId());
        if (hasSubscription) {
            SubscriptionDTO sub = remote.getSubscription(r.subscriptionId());
            if (!r.clientId().equals(sub.clientId())) {
                throw new BusinessException("L'abonnement " + sub.id() + " n'appartient pas au client " + r.clientId());
            }
            if (!"ACTIF".equals(sub.statut())) {
                throw new BusinessException("L'abonnement " + sub.id() + " n'est pas actif (statut : " + sub.statut() + ")");
            }
        }

        Invoice inv = new Invoice();
        inv.setClientId(r.clientId());
        inv.setSubscriptionId(r.subscriptionId());
        inv.setPeriode(r.periode());
        inv.setDateEcheance(r.dateEcheance());
        inv.setStatut(InvoiceStatus.BROUILLON);

        BigDecimal ht = BigDecimal.ZERO;
        for (LineRequest lr : r.lignes()) {
            InvoiceLine l = new InvoiceLine();
            l.setDescription(lr.description());
            l.setQuantite(lr.quantite());
            l.setPrixUnitaire(lr.prixUnitaire());
            l.setMontant(lr.prixUnitaire().multiply(BigDecimal.valueOf(lr.quantite())));
            l.setInvoice(inv);
            inv.getLignes().add(l);
            ht = ht.add(l.getMontant());
        }
        BigDecimal tva = ht.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP);
        inv.setMontantHT(ht.setScale(2, RoundingMode.HALF_UP));
        inv.setTva(tva);
        inv.setMontantTTC(inv.getMontantHT().add(tva));
        return new InvoiceCreationResult(toDTO(repository.save(inv)), true);
    }

    /**
     * Refuse les transitions interdites (ex : annuler une facture PAYEE).
     * V5 : quand la facture passe a EMISE, un evenement est publie (envoye a RabbitMQ apres le commit).
     */
    @Transactional
    public InvoiceDTO changeStatus(@Positive Long id, @NotNull InvoiceStatus target) {
        Invoice inv = findEntityById(id);
        if (!inv.getStatut().canTransitionTo(target)) {
            throw new BusinessException("Transition interdite : " + inv.getStatut() + " -> " + target);
        }
        inv.setStatut(target);
        if (target == InvoiceStatus.EMISE) inv.setDateEmission(LocalDate.now());
        Invoice saved = repository.save(inv);
        if (target == InvoiceStatus.EMISE) {
            eventPublisher.publishEvent(buildEmittedEvent(saved));
        }
        return toDTO(saved);
    }

    private InvoiceDTO toDTO(Invoice invoice) {
        List<InvoiceLineDTO> lignes = invoice.getLignes().stream()
                .map(line -> new InvoiceLineDTO(line.getId(), line.getDescription(), line.getQuantite(),
                        line.getPrixUnitaire(), line.getMontant()))
                .toList();
        return new InvoiceDTO(invoice.getId(), invoice.getClientId(), invoice.getSubscriptionId(),
                invoice.getPeriode(), invoice.getStatut(), invoice.getDateEmission(), invoice.getDateEcheance(),
                invoice.getMontantHT(), invoice.getTva(), invoice.getMontantTTC(), lignes);
    }

    private InvoiceEmittedEvent buildEmittedEvent(Invoice inv) {
        String nom = null;
        String email = null;
        try {
            ClientDTO client = remote.getClient(inv.getClientId());
            nom = client.nom();
            email = client.email();
        } catch (RuntimeException e) {
            // La facture est emise quand meme ; le consumer marquera la notification en echec (pas d'e-mail).
            log.warn("Impossible de recuperer le client {} pour la notification : {}", inv.getClientId(), e.getMessage());
        }
        return new InvoiceEmittedEvent(inv.getId(), inv.getClientId(), nom, email, inv.getPeriode(),
                inv.getMontantTTC(), inv.getDateEmission(), inv.getDateEcheance());
    }
}
