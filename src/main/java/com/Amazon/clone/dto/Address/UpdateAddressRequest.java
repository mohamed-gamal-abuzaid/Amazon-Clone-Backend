package com.Amazon.clone.dto.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 150, message = "Full name must be between 3 and 150 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 30, message = "Phone number must be between 10 and 30 characters")
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
}
