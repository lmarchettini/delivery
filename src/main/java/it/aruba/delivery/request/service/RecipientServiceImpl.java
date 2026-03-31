package it.aruba.delivery.request.service;

import org.springframework.stereotype.Service;

import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.http.RecipientClient;
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