package it.aruba.delivery.request.service;

import it.aruba.delivery.request.dto.RecipientResponse;

public interface RecipientService {
    RecipientResponse getRecipient(String recipientId);

}