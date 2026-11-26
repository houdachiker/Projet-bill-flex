package com.billing.abonnements.service;

import com.billing.abonnements.dto.ClientDTO;
import com.billing.abonnements.dto.CreateSubscriptionRequest;
import com.billing.abonnements.dto.PlanDTO;
import com.billing.abonnements.dto.SubscriptionDTO;
import com.billing.abonnements.dto.SubscriptionCreationResult;
import com.billing.abonnements.dto.SubscriptionDetailsDTO;
import com.billing.abonnements.exception.BusinessException;
import com.billing.abonnements.exception.ResourceNotFoundException;
import com.billing.abonnements.feign.RemoteClientService;
import com.billing.abonnements.model.Plan;
import com.billing.abonnements.model.Subscription;
import com.billing.abonnements.model.SubscriptionStatus;
import com.billing.abonnements.repository.PlanRepository;
import com.billing.abonnements.repository.SubscriptionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final PlanService planService;
    private final PlanRepository planRepository;
    private final RemoteClientService remoteClient;   // V4 : appel Feign vers service-clients (resilient, V6)

    public SubscriptionService(SubscriptionRepository repository, PlanService planService, PlanRepository planRepository,
                               RemoteClientService remoteClient) {
        this.repository = repository;
        this.planService = planService;
        this.planRepository = planRepository;
        this.remoteClient = remoteClient;
    }

    public List<SubscriptionDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public SubscriptionDTO findById(@Positive Long id) {
        return toDTO(findEntityById(id));
    }

    private Subscription findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable : " + id));
    }

    /** V4 - Consultation enrichie : l'abonnement (avec son plan) + le client recupere dans service-clients. */
    public SubscriptionDetailsDTO findDetails(@Positive Long id) {
        Subscription s = findEntityById(id);
        ClientDTO client = remoteClient.getClient(s.getClientId());
        return new SubscriptionDetailsDTO(toDTO(s), client);
    }

    /**
     * V4 - validation prealable : le client doit exister dans service-clients (404 sinon, rien n'est enregistre).
     * V6 - idempotence : si le client a deja un abonnement ACTIF a ce plan, on le renvoie (created = false)
     *      au lieu de creer un doublon.
     */
    public SubscriptionCreationResult create(@Valid CreateSubscriptionRequest r) {
        Optional<Subscription> existing = repository
                .findFirstByClientIdAndPlanIdAndStatut(r.clientId(), r.planId(), SubscriptionStatus.ACTIF);
        if (existing.isPresent()) {
            return new SubscriptionCreationResult(toDTO(existing.get()), false);
        }

        remoteClient.getClient(r.clientId());

        PlanDTO planDTO = planService.findById(r.planId());
        if (!planDTO.actif()) throw new BusinessException("Ce plan n'est plus propose a la vente");
        Plan plan = planRepository.getReferenceById(r.planId());
        LocalDate debut = r.dateDebut() != null ? r.dateDebut() : LocalDate.now();
        Subscription s = new Subscription();
        s.setClientId(r.clientId());
        s.setPlan(plan);
        s.setDateDebut(debut);
        s.setProchaineEcheance(debut.plusMonths(1));
        s.setStatut(SubscriptionStatus.ACTIF);
        return new SubscriptionCreationResult(toDTO(repository.save(s)), true);
    }

    public SubscriptionDTO resilier(@Positive Long id) {
        Subscription s = findEntityById(id);
        s.setStatut(SubscriptionStatus.RESILIE);
        s.setDateFin(LocalDate.now());
        return toDTO(repository.save(s));
    }

    private SubscriptionDTO toDTO(Subscription subscription) {
        Plan plan = subscription.getPlan();
        return new SubscriptionDTO(subscription.getId(), subscription.getClientId(),
                new PlanDTO(plan.getId(), plan.getNom(), plan.getPrixMensuel(), plan.getQuotaUtilisateurs(),
                        plan.isActif()),
                subscription.getDateDebut(), subscription.getDateFin(),
                subscription.getProchaineEcheance(), subscription.getStatut());
    }
}
