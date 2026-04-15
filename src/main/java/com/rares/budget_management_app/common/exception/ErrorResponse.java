package com.rares.budget_management_app.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int errorCode;
    private String message;
    private int httpStatus;
    private LocalDateTime timestamp;
}
