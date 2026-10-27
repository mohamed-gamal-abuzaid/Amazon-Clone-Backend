package com.Amazon.clone.dto.User;

import com.Amazon.clone.dto.Address.AddressResponse;
import com.Amazon.clone.dto.Auth.UserResponse;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private  String fullName;
    private  String email;
    private  String phone;

}
