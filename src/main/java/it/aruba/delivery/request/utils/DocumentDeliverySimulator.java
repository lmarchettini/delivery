package it.aruba.delivery.request.utils;


import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocumentDeliverySimulator {

    public boolean sendDocument(String documentName, String documentType) {
        log.info("Simulating document delivery: {} ({})", documentName, documentType);

        //simuliamo i 2 casi di invio documento, inviato con successo e invio fallito per poter eseguire i TEST
        if (documentName != null && documentName.toLowerCase().contains("fail")) {
            return false;
        }

        return true;
    }
}