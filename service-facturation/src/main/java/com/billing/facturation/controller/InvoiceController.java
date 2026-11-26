package com.billing.facturation.controller;

import com.billing.facturation.dto.CreateInvoiceRequest;
import com.billing.facturation.dto.InvoiceCreationResult;
import com.billing.facturation.dto.InvoiceDetailsDTO;
import com.billing.facturation.dto.InvoiceDTO;
import com.billing.facturation.model.InvoiceStatus;
import com.billing.facturation.service.InvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) { this.service = service; }

    @GetMapping
    public List<InvoiceDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public InvoiceDTO findById(@PathVariable Long id) { return service.findById(id); }

    /** V4 : facture enrichie (client + abonnement/plan recuperes par Feign). */
    @GetMapping("/{id}/details")
    public InvoiceDetailsDTO details(@PathVariable Long id) { return service.findDetails(id); }

    /** 201 Created la 1ere fois ; 200 OK + en-tete Idempotent-Replay si la creation est rejouee (V6). */
    @PostMapping
    public ResponseEntity<InvoiceDTO> create(@Valid @RequestBody CreateInvoiceRequest r) {
        InvoiceCreationResult result = service.create(r);
        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.invoice());
        }
        return ResponseEntity.ok().header("Idempotent-Replay", "true").body(result.invoice());
    }

    /** PATCH /invoices/1/status?statut=EMISE */
    @PatchMapping("/{id}/status")
    public InvoiceDTO changeStatus(@PathVariable Long id, @RequestParam InvoiceStatus statut) {
        return service.changeStatus(id, statut);
    }
}
