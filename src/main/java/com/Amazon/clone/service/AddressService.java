package com.Amazon.clone.service;

import com.Amazon.clone.dto.Address.AddressResponse;
import com.Amazon.clone.dto.Address.CreateAddressRequest;
import com.Amazon.clone.dto.Address.UpdateAddressRequest;
import com.Amazon.clone.entity.Address;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.exception.ResourceNotFoundException;
import com.Amazon.clone.repository.AddressRepository;
import com.Amazon.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findAllByUserId(userId)
                .stream()
                .map(this::buildAddressResponse)
                .toList();
    }


    @Transactional
    public AddressResponse createAddress(UUID userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean shouldBeDefault = request.isDefault() || !addressRepository.existsByUserId(userId);

        Address address = new Address();
        address.setUser(user);
        addData(address, request.getFullName(), request.getPhone(), request.getAddressLine(), request.getAddressLine2(), request.getCity(), request.getState(), request.getCountry(), request.getPostalCode());

        if (shouldBeDefault) {
            markAsDefault(userId, address);
        }

        return buildAddressResponse(addressRepository.save(address));
    }


    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addData(address, request.getFullName(), request.getPhone(), request.getAddressLine(), request.getAddressLine2(), request.getCity(), request.getState(), request.getCountry(), request.getPostalCode());

        return buildAddressResponse(addressRepository.save(address));
    }

    private void addData(Address address, String fullName, String phone, String addressLine, String addressLine2, String city, String state, String country, String postalCode) {
        address.setFullName(fullName);
        address.setPhone(phone);
        address.setAddressLine(addressLine);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setState(state);
        address.setCountry(country);
        address.setPostalCode(postalCode);
    }


    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }


    @Transactional
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        markAsDefault(userId, address);

        return buildAddressResponse(addressRepository.save(address));
    }



    private void markAsDefault(UUID userId, Address address) {
        addressRepository.clearDefaultForUser(userId);
        address.setDefaultAddress(true);
    }

    private AddressResponse buildAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .isDefault(address.isDefaultAddress())
                .build();
    }
}
