package it.aruba.delivery.request.service.impl;

import org.springframework.stereotype.Service;

import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.http.RecipientClient;
import it.aruba.delivery.request.service.RecipientService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientClient recipientClient;

    @Override
    public RecipientResponse getRecipient(String recipientId) {
        return recipientClient.getRecipient(recipientId);
    }
}