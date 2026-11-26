package com.billing.notification.listener;

import com.billing.notification.config.RabbitConfig;
import com.billing.notification.dto.InvoiceEmittedEvent;
import com.billing.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** CONSUMER : ecoute la queue et delegue le traitement au service. */
@Component
public class InvoiceEventListener {
    private final NotificationService service;

    public InvoiceEventListener(NotificationService service) { this.service = service; }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onInvoiceEmitted(InvoiceEmittedEvent event) {
        service.handleInvoiceEmitted(event);
    }
}
