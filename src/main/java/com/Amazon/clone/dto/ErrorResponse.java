package com.Amazon.clone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;    // للـ validation errors

    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(int status, String message, Map<String, String> errors) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
}