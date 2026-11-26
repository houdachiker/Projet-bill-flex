package com.billing.abonnements.controller;

import com.billing.abonnements.dto.CreateSubscriptionRequest;
import com.billing.abonnements.dto.SubscriptionCreationResult;
import com.billing.abonnements.dto.SubscriptionDetailsDTO;
import com.billing.abonnements.dto.SubscriptionDTO;
import com.billing.abonnements.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping
    public List<SubscriptionDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public SubscriptionDTO findById(@PathVariable Long id) { return service.findById(id); }

    /** V4 : abonnement enrichi (plan + client recupere par Feign). */
    @GetMapping("/{id}/details")
    public SubscriptionDetailsDTO details(@PathVariable Long id) { return service.findDetails(id); }

    /** 201 Created la 1ere fois ; 200 OK + en-tete Idempotent-Replay si la creation est rejouee (V6). */
    @PostMapping
    public ResponseEntity<SubscriptionDTO> create(@Valid @RequestBody CreateSubscriptionRequest r) {
        SubscriptionCreationResult result = service.create(r);
        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.subscription());
        }
        return ResponseEntity.ok().header("Idempotent-Replay", "true").body(result.subscription());
    }

    @PatchMapping("/{id}/resilier")
    public SubscriptionDTO resilier(@PathVariable Long id) { return service.resilier(id); }
}
