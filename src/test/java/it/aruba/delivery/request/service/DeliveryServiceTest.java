package it.aruba.delivery.request.service;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.aruba.delivery.request.dto.CreateDeliveryRequest;
import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.entity.DeliveryRequest;
import it.aruba.delivery.request.repository.DeliveryRequestRecipientRepository;
import it.aruba.delivery.request.repository.DeliveryRequestRepository;
import it.aruba.delivery.request.utils.DocumentDeliverySimulator;
import it.aruba.delivery.request.utils.RequestStatus;
import it.aruba.delivery.request.utils.ValidityStatus;
import it.aruba.delivery.request.http.RecipientClient;
import it.aruba.delivery.request.kafka.DeliveryEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRequestRepository requestRepo;

    @Mock
    private DeliveryRequestRecipientRepository recipientRepo;

    @Mock
    private RecipientClient recipientClient;

    @Mock
    private DeliveryEventPublisher eventPublisher;

    @Mock
    private DocumentDeliverySimulator documentDeliverySimulator;

    @InjectMocks
    private DeliveryService service;

    @Test
    void shouldCreateCompletedRequest_whenRecipientsValidAndDeliveryOk() {

        //mock recipient VALID
        RecipientResponse recipient = new RecipientResponse();
        recipient.setId("1");
        recipient.setValidityStatus(ValidityStatus.VALID);

        when(recipientClient.getRecipient(any()))
                .thenReturn(recipient);

        when(documentDeliverySimulator.sendDocument(any(), any()))
                .thenReturn(true);

        
        when(requestRepo.save(any()))
                .thenAnswer(invocation -> {
                    DeliveryRequest dr = invocation.getArgument(0);
                    dr.setId(1);
                    return dr;
                });

        // request
        CreateDeliveryRequest request = new CreateDeliveryRequest();
        request.setRecipientIds(List.of("1"));
        request.setRequestType("EMAIL");
        request.setDocumentName("test.pdf");
        request.setDocumentType("PDF");

        //call
        DeliveryRequest result = service.create(request);

        //assert
        assertEquals(RequestStatus.COMPLETED, result.getStatus());
    }
}