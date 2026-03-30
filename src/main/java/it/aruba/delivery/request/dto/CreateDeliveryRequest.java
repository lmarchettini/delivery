package it.aruba.delivery.request.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateDeliveryRequest {

	@NotEmpty(message = "Recipient list cannot be empty")
    private List<String> recipientIds;
    @NotBlank(message = "Request type is required")
    private String requestType;
    @NotBlank(message = "Document name is required")
    private String documentName;
    @NotBlank(message = "Document type is required")
    private String documentType;
}