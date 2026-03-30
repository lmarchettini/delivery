package it.aruba.delivery.request.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import it.aruba.delivery.request.entity.DeliveryRequest;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Integer> {
}