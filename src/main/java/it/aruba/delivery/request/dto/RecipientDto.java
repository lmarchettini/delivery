package it.aruba.delivery.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipientDto {

    private String id;
    private String name;
    private String surname;
    private String digitalAddress;
}