package com.Amazon.clone.dto.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private UUID id;
    private String fullName;
    private String phone;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String addressLine;
    private String addressLine2;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
