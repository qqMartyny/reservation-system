package com.reserv.reservation_system.common.exception;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    String message,
    String errorMessage,
    LocalDateTime errorTime
) {
    
}
