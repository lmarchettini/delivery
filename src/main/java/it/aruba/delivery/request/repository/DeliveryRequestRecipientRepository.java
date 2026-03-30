package it.aruba.delivery.request.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.aruba.delivery.request.entity.DeliveryRequestRecipient;

public interface DeliveryRequestRecipientRepository extends JpaRepository<DeliveryRequestRecipient, Integer> {
    List<DeliveryRequestRecipient> findByRequestId(Integer requestId);
}