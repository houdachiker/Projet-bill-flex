package com.billing.abonnements.controller;

import com.billing.abonnements.dto.PlanRequest;
import com.billing.abonnements.dto.PlanDTO;
import com.billing.abonnements.service.PlanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plans")
public class PlanController {
    private final PlanService service;

    public PlanController(PlanService service)
     { this.service = service; }

    @GetMapping
    public List<PlanDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public PlanDTO findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDTO create(@Valid @RequestBody PlanRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public PlanDTO update(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
