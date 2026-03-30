package it.aruba.delivery.request.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.aruba.delivery.request.dto.CreateDeliveryRequest;
import it.aruba.delivery.request.dto.DeliveryResponse;
import it.aruba.delivery.request.entity.DeliveryRequest;
import it.aruba.delivery.request.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/delivery-requests")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService service;

	@PostMapping
	public ResponseEntity<DeliveryRequest> create(@Valid @RequestBody CreateDeliveryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
	}

	@GetMapping
	public ResponseEntity<List<DeliveryRequest>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<DeliveryResponse> getById(@PathVariable("id") Integer id) {
	    return ResponseEntity.ok(service.getByIdEnriched(id));
	}

}