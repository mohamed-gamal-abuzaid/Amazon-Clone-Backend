package com.Amazon.clone.controller;

import com.Amazon.clone.dto.Address.AddressResponse;
import com.Amazon.clone.dto.Address.CreateAddressRequest;
import com.Amazon.clone.dto.Address.UpdateAddressRequest;
import com.Amazon.clone.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "APIs for managing user addresses")
public class AddressController {

    private final AddressService addressService;


    @GetMapping
    @Operation(summary = "Get my addresses", description = "Get all addresses for the authenticated user")
    public ResponseEntity<List<AddressResponse>> getUserAddresses(
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }


    @PostMapping
    @Operation(summary = "Add new address", description = "Add a new address for the authenticated user")
    public ResponseEntity<AddressResponse> createAddress(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        AddressResponse response = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Update an existing address for the authenticated user")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address", description = "Delete an address for the authenticated user")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId
    ) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Set default address", description = "Set an address as the default for the authenticated user")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId
    ) {
        AddressResponse response = addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(response);
    }
}
