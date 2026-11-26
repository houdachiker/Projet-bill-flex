package com.billing.notification.repository;

import com.billing.notification.model.Notification;
import com.billing.notification.model.NotificationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByInvoiceIdAndStatut(Long invoiceId, NotificationStatus statut);
    List<Notification> findByInvoiceId(Long invoiceId);
}
