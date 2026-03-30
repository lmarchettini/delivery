package it.aruba.delivery.request.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import it.aruba.delivery.request.dto.RecipientResponse;
import it.aruba.delivery.request.exception.ExternalServiceException;
import it.aruba.delivery.request.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RecipientClient {
	
	@Value("${recipients.service.url}")
    private String recipientsServiceUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	public RecipientResponse getRecipient(String id) {

		String url = recipientsServiceUrl + "/recipients/" + id;

		try {
			log.info("Calling Recipient Service for id: {}", id);

			return restTemplate.getForObject(url, RecipientResponse.class);

		} catch (HttpClientErrorException.NotFound ex) {
			log.warn("Recipient not found: {}", id);
			throw new ResourceNotFoundException("Recipient not found: " + id);

		} catch (HttpServerErrorException ex) {
			log.error("Recipient service error", ex);
			throw new ExternalServiceException("Recipient service error");

		} catch (Exception ex) {
			log.error("Recipient service unavailable", ex);
			throw new ExternalServiceException("Recipient service unavailable");
		}
	}
}