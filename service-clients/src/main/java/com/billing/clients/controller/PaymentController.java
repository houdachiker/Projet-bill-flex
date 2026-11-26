package com.billing.clients.controller;

import com.billing.clients.dto.CreatePaymentRequest;
import com.billing.clients.dto.PaymentDTO;
import com.billing.clients.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @GetMapping
    public List<PaymentDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public PaymentDTO findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDTO create(@Valid @RequestBody CreatePaymentRequest r) { return service.create(r); }

    @PatchMapping("/{id}/encaisser")
    public PaymentDTO encaisser(@PathVariable Long id) { return service.encaisser(id); }

    @PatchMapping("/{id}/annuler")
    public PaymentDTO annuler(@PathVariable Long id) { return service.annuler(id); }
}
