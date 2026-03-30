package it.aruba.delivery.request.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import it.aruba.delivery.request.dto.CreateDeliveryRequest;
import it.aruba.delivery.request.dto.DeliveryResponse;
import it.aruba.delivery.request.dto.RecipientDto;
import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.dto.RequestStatusChangedEvent;
import it.aruba.delivery.request.entity.DeliveryRequest;
import it.aruba.delivery.request.entity.DeliveryRequestRecipient;
import it.aruba.delivery.request.exception.InvalidRecipientException;
import it.aruba.delivery.request.exception.ResourceNotFoundException;
import it.aruba.delivery.request.http.RecipientClient;
import it.aruba.delivery.request.kafka.DeliveryEventPublisher;
import it.aruba.delivery.request.repository.DeliveryRequestRecipientRepository;
import it.aruba.delivery.request.repository.DeliveryRequestRepository;
import it.aruba.delivery.request.utils.DocumentDeliverySimulator;
import it.aruba.delivery.request.utils.RequestStatus;
import it.aruba.delivery.request.utils.ValidityStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class DeliveryService {

	private final DeliveryRequestRepository requestRepo;
	private final DeliveryRequestRecipientRepository recipientRepo;
	private final RecipientClient recipientClient;
	private final DeliveryEventPublisher eventPublisher;
	private final DocumentDeliverySimulator documentDeliverySimulator;

	@Transactional
	public DeliveryRequest create(CreateDeliveryRequest request) {

		log.info("Creating delivery request");
		// dal DTO costrusico l'entity
		DeliveryRequest dr = buildRequest(request);
		// verifica stato validità con chiamata REST al recipients-service
		try {
			validateRecipients(request.getRecipientIds());
			dr.setStatus(RequestStatus.PENDING);
		} catch (InvalidRecipientException ex) {

			log.warn("Validation failed: {}", ex.getMessage());

			dr.setStatus(RequestStatus.FAILED);
			dr.setFailureReason(ex.getMessage());
			dr.setCompletedAt(LocalDateTime.now());

			dr = requestRepo.save(dr);
			saveRecipients(dr.getId(), request.getRecipientIds());

			return dr;
		}

		// salvo richiesta e relazione
		dr = requestRepo.save(dr);
		saveRecipients(dr.getId(), request.getRecipientIds());

		// Inizio invio
		RequestStatus oldStatus = dr.getStatus();
		updateStatus(dr, RequestStatus.PROCESSING, null);
		publishStatusChanged(oldStatus, RequestStatus.PROCESSING, dr.getId());

		// simulazione invio documento
		boolean delivered = documentDeliverySimulator.sendDocument(dr.getDocumentName(), dr.getDocumentType());
		log.info("Document delivery result for request {}: {}", dr.getId(), delivered);

		// stato finale
		oldStatus = dr.getStatus();

		if (delivered) {
			updateStatus(dr, RequestStatus.COMPLETED, null);
			publishStatusChanged(oldStatus, RequestStatus.COMPLETED, dr.getId());
		} else {
			updateStatus(dr, RequestStatus.FAILED, "Document delivery failed");
			publishStatusChanged(oldStatus, RequestStatus.FAILED, dr.getId());
		}

		return dr;
	}

	private void updateStatus(DeliveryRequest request, RequestStatus newStatus, String failureReason) {
		request.setStatus(newStatus);
		request.setUpdatedAt(LocalDateTime.now());

		if (newStatus == RequestStatus.COMPLETED || newStatus == RequestStatus.FAILED) {
			request.setCompletedAt(LocalDateTime.now());
		}

		request.setFailureReason(failureReason);

		try {
			requestRepo.save(request);
		} catch (Exception ex) {
			log.error("Failed to update status for request {}", request.getId(), ex);
			throw ex;
		}
	}

	private void publishStatusChanged(RequestStatus oldStatus, RequestStatus newStatus, Integer requestId) {
		RequestStatusChangedEvent event = RequestStatusChangedEvent.builder().requestId(requestId).oldStatus(oldStatus)
				.newStatus(newStatus).timestamp(LocalDateTime.now()).build();
		try {
			eventPublisher.publishStatusChanged(event);
		} catch (Exception ex) {
			log.error("Kafka Event not send", ex);
		}
	}

	public List<DeliveryRequest> getAll() {
		return requestRepo.findAll();
	}

	public DeliveryResponse getByIdEnriched(Integer id) {
		// recupero la rhciesta di invio
		DeliveryRequest request = getRequestOrThrow(id);
		// gli id dei destinatari
		List<DeliveryRequestRecipient> links = getRecipientsLinks(id);
		// per ogni id chiamo il recipients-service per l'anagrafica
		List<RecipientDto> recipients = enrichRecipients(links);
		// Costruisci la response finale
		return buildResponse(request, recipients);
	}

	private DeliveryRequest getRequestOrThrow(Integer id) {
		return requestRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));
	}

	private List<DeliveryRequestRecipient> getRecipientsLinks(Integer id) {
		return recipientRepo.findByRequestId(id);
	}

	private List<RecipientDto> enrichRecipients(List<DeliveryRequestRecipient> links) {

		return links.stream().map(this::mapToRecipientDto).toList();
	}

	private RecipientDto mapToRecipientDto(DeliveryRequestRecipient link) {

		try {
			RecipientResponse r = recipientClient.getRecipient(link.getRecipientId());

			return RecipientDto.builder().id(r.getId()).name(r.getName()).surname(r.getSurname())
					.digitalAddress(r.getDigitalAddress()).build();

		} catch (Exception ex) {
			log.warn("Recipient service unavailable for id {}", link.getRecipientId());
			// nel caso il recipients-service è down non blocchiamo la get ma ritorniamo i
			// campi vuoti
			return RecipientDto.builder().id(link.getRecipientId()).name("UNKNOWN").digitalAddress("N/A").build();
		}
	}

	private DeliveryResponse buildResponse(DeliveryRequest request, List<RecipientDto> recipients) {

		return DeliveryResponse.builder().requestId(request.getId()).requestType(request.getRequestType())
				.status(request.getStatus().name()).createdAt(request.getCreatedAt())
				.completedAt(request.getCompletedAt()).documentName(request.getDocumentName())
				.documentType(request.getDocumentType()).recipients(recipients).build();
	}

	private DeliveryRequest buildRequest(CreateDeliveryRequest request) {
		return DeliveryRequest.builder().requestType(request.getRequestType()).createdAt(LocalDateTime.now())
				.documentName(request.getDocumentName()).documentType(request.getDocumentType()).build();
	}

	private void validateRecipients(List<String> recipientIds) {

		for (String recipientId : recipientIds) {

			RecipientResponse recipient = recipientClient.getRecipient(recipientId);

			if (recipient.getValidityStatus() != ValidityStatus.VALID) {
				throw new InvalidRecipientException("Invalid recipient: " + recipientId);
			}
		}
	}

	private void saveRecipients(Integer requestId, List<String> recipientIds) {

		for (String recipientId : recipientIds) {
			recipientRepo.save(new DeliveryRequestRecipient(null, requestId, recipientId));
		}
	}
}