package it.aruba.delivery.request.dto;



import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryResponse {

    private Integer requestId;
    private String requestType;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    private String documentName;
    private String documentType;

    private List<RecipientDto> recipients;
}