package it.aruba.delivery.request.dto;


import it.aruba.delivery.request.utils.ValidityStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipientResponse {

    private String id;
    private String name;
    private String surname;
    private String digitalAddress;
    private ValidityStatus validityStatus;
}