package com.Amazon.clone.dto.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 150)
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    private String postalCode;

    @JsonProperty("isDefault")
    private Boolean isDefault;

    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }
}
