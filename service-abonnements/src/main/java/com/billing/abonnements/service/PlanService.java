package com.billing.abonnements.service;

import com.billing.abonnements.dto.PlanDTO;
import com.billing.abonnements.dto.PlanRequest;
import com.billing.abonnements.exception.ResourceNotFoundException;
import com.billing.abonnements.model.Plan;
import com.billing.abonnements.repository.PlanRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PlanService {
    private final PlanRepository repository;

    public PlanService(PlanRepository repository) { this.repository = repository; }

    public List<PlanDTO> findAll() { return repository.findAll().stream().map(this::toDTO).toList(); }

    public PlanDTO findById(@Positive Long id) {
        return toDTO(findPlanEntityById(id));
    }

    private Plan findPlanEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan introuvable : " + id));
    }

    /** Mapping DTO (PlanRequest) -> entite. */
    public PlanDTO create(@Valid PlanRequest r) {
        Plan plan = new Plan();
        apply(plan, r);
        return toDTO(repository.save(plan));
    }

    public PlanDTO update(@Positive Long id, @Valid PlanRequest r) {
        Plan p = findPlanEntityById(id);
        apply(p, r);
        return toDTO(repository.save(p));
    }

    public void delete(@Positive Long id) {
        repository.delete(findPlanEntityById(id));
    }

    private PlanDTO toDTO(Plan plan) {
        return new PlanDTO(plan.getId(), plan.getNom(), plan.getPrixMensuel(), plan.getQuotaUtilisateurs(),
                plan.isActif());
    }

    private void apply(Plan p, PlanRequest r) {
        p.setNom(r.nom());
        p.setPrixMensuel(r.prixMensuel());
        p.setQuotaUtilisateurs(r.quotaUtilisateurs());
        p.setActif(r.actif() == null || r.actif());   // actif par defaut
    }
}
