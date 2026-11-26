package com.billing.facturation.messaging;

import com.billing.facturation.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * V5 - PUBLISHER. Le message n'est envoye qu'APRES le commit de la transaction : on ne notifie jamais
 * un changement d'etat qui aurait ete annule. Si le broker est down, la facture reste EMISE
 * (l'erreur est journalisee) : la facturation ne depend pas de la messagerie.
 */
@Component
public class InvoiceEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(InvoiceEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public InvoiceEventPublisher(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvoiceEmitted(InvoiceEmittedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
            log.info("[PUBLISHER] Evenement facture EMISE publie dans RabbitMQ : facture {} -> {}",
                    event.invoiceId(), event.clientEmail());
        } catch (Exception e) {
            log.error("[PUBLISHER] Echec de publication RabbitMQ pour la facture {} : {}", event.invoiceId(), e.getMessage());
        }
    }
}
