package com.billing.notification.controller;

import com.billing.notification.dto.NotificationDTO;
import com.billing.notification.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Consultation des notifications traitees : confirmation de reception / d'envoi. */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public List<NotificationDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public NotificationDTO findById(@PathVariable Long id) { return service.findById(id); }

    @GetMapping("/invoice/{invoiceId}")
    public List<NotificationDTO> findByInvoice(@PathVariable Long invoiceId) {
        return service.findByInvoice(invoiceId);
    }
}
