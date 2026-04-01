package it.aruba.delivery.request.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import it.aruba.delivery.request.dto.CreateDeliveryRequest;
import it.aruba.delivery.request.dto.DeliveryResponse;
import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.entity.DeliveryRequest;
import it.aruba.delivery.request.entity.DeliveryRequestRecipient;
import it.aruba.delivery.request.exception.ResourceNotFoundException;
import it.aruba.delivery.request.kafka.DeliveryEventPublisher;
import it.aruba.delivery.request.repository.DeliveryRequestRecipientRepository;
import it.aruba.delivery.request.repository.DeliveryRequestRepository;
import it.aruba.delivery.request.utils.DocumentDeliverySimulator;
import it.aruba.delivery.request.utils.RequestStatus;
import it.aruba.delivery.request.utils.ValidityStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

	@Mock
	private DeliveryRequestRepository requestRepo;

	@Mock
	private DeliveryRequestRecipientRepository recipientRepo;

	@Mock
	private RecipientService recipientService;

	@Mock
	private DeliveryEventPublisher eventPublisher;

	@Mock
	private DocumentDeliverySimulator documentDeliverySimulator;

	@InjectMocks
	private DeliveryService service;

	private CreateDeliveryRequest request;

	@BeforeEach
	void setup() {
		request = CreateDeliveryRequest.builder().requestType("EMAIL").documentName("doc.pdf").documentType("PDF")
				.recipientIds(List.of("1", "2")).build();
	}

	// creazione con successo
	@Test
    void should_create_and_complete_delivery() {

        when(recipientService.getRecipient(any()))
                .thenReturn(validRecipient());
        //quando salvi setta l'id a 1
        when(requestRepo.save(any()))
                .thenAnswer(invocation -> {
                    DeliveryRequest dr = invocation.getArgument(0);
                    dr.setId(1);
                    return dr;
                });

        when(documentDeliverySimulator.sendDocument(any(), any()))
                .thenReturn(true);

        DeliveryRequest result = service.create(request);

        assertEquals(RequestStatus.COMPLETED, result.getStatus());

        verify(requestRepo, atLeastOnce()).save(any());
        verify(recipientRepo, times(2)).save(any());
        verify(eventPublisher, atLeastOnce()).publishStatusChanged(any());
    }

	// destinatario invalido
	@Test
    void should_fail_when_recipient_invalid() {

        when(recipientService.getRecipient(any()))
                .thenReturn(invalidRecipient());

        when(requestRepo.save(any()))
                .thenAnswer(invocation -> {
                    DeliveryRequest dr = invocation.getArgument(0);
                    dr.setId(1);
                    return dr;
                });

        DeliveryRequest result = service.create(request);

        assertEquals(RequestStatus.FAILED, result.getStatus());

        verify(documentDeliverySimulator, never())
                .sendDocument(any(), any());
    }
	
	private RecipientResponse validRecipient() {
        return RecipientResponse.builder()
                .id("1")
                .name("Mario")
                .surname("Rossi")
                .digitalAddress("mail@test.com")
                .validityStatus(ValidityStatus.VALID)
                .build();
    }

    private RecipientResponse invalidRecipient() {
        return RecipientResponse.builder()
                .id("1")
                .validityStatus(ValidityStatus.INVALID)
                .build();
    }
}