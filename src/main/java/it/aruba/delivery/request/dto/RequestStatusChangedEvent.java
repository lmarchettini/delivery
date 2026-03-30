package it.aruba.delivery.request.dto;


import java.time.LocalDateTime;

import it.aruba.delivery.request.utils.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatusChangedEvent {

    private Integer requestId;
    private RequestStatus oldStatus;
    private RequestStatus newStatus;
    private LocalDateTime timestamp;
}