package com.billing.clients.service;

import com.billing.clients.dto.CreatePaymentRequest;
import com.billing.clients.dto.InvoiceDTO;
import com.billing.clients.dto.PaymentDTO;
import com.billing.clients.exception.BusinessException;
import com.billing.clients.exception.ResourceNotFoundException;
import com.billing.clients.feign.RemoteInvoiceService;
import com.billing.clients.model.Payment;
import com.billing.clients.model.PaymentStatus;
import com.billing.clients.repository.PaymentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PaymentService {
    /** Une facture n'est payable que si elle a ete emise (ou est en retard). */
    private static final Set<String> PAYABLE_STATUSES = Set.of("EMISE", "EN_RETARD");

    private final PaymentRepository repository;
    private final ClientService clientService;
    private final RemoteInvoiceService remoteInvoice;   // V4 : Feign vers service-facturation

    public PaymentService(PaymentRepository repository, ClientService clientService,
                          RemoteInvoiceService remoteInvoice) {
        this.repository = repository;
        this.clientService = clientService;
        this.remoteInvoice = remoteInvoice;
    }

    public List<PaymentDTO> findAll() { return repository.findAll().stream().map(this::toDTO).toList(); }

    public PaymentDTO findById(@Positive Long id) {
        return toDTO(findEntityById(id));
    }

    private Payment findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));
    }

    /**
     * V4 - validation prealable des references : le client existe (base locale) ET la facture existe dans
     * service-facturation, appartient a ce client et est payable. Sinon 404 / 409 et rien n'est enregistre.
     */
    public PaymentDTO create(@Valid CreatePaymentRequest r) {
        clientService.findById(r.clientId());

        InvoiceDTO invoice = remoteInvoice.getInvoice(r.invoiceId());
        if (!r.clientId().equals(invoice.clientId())) {
            throw new BusinessException("La facture " + invoice.id() + " n'appartient pas au client " + r.clientId());
        }
        if (!PAYABLE_STATUSES.contains(invoice.statut())) {
            throw new BusinessException("La facture " + invoice.id() + " n'est pas payable (statut : " + invoice.statut() + ")");
        }

        Payment p = new Payment();
        p.setClientId(r.clientId());
        p.setInvoiceId(r.invoiceId());
        p.setMontant(r.montant());
        p.setStatut(PaymentStatus.EN_ATTENTE);
        return toDTO(repository.save(p));
    }

    /** Un seul encaissement par paiement, et jamais sur un paiement annule. */
    public PaymentDTO encaisser(@Positive Long id) {
        Payment p = findEntityById(id);
        if (p.getStatut() == PaymentStatus.ANNULE) throw new BusinessException("Paiement annule : encaissement impossible");
        if (p.getStatut() == PaymentStatus.ENCAISSE) throw new BusinessException("Paiement deja encaisse");
        p.setStatut(PaymentStatus.ENCAISSE);
        p.setDatePaiement(LocalDateTime.now());
        return toDTO(repository.save(p));
    }

    public PaymentDTO annuler(@Positive Long id) {
        Payment p = findEntityById(id);
        if (p.getStatut() == PaymentStatus.ENCAISSE) throw new BusinessException("Paiement deja encaisse : annulation impossible");
        p.setStatut(PaymentStatus.ANNULE);
        return toDTO(repository.save(p));
    }

    private PaymentDTO toDTO(Payment payment) {
        return new PaymentDTO(payment.getId(), payment.getInvoiceId(), payment.getClientId(), payment.getMontant(),
                payment.getStatut(), payment.getDatePaiement());
    }
}
