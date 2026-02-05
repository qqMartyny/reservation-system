package com.reserv.reservation_system;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    String messege,
    String erroeMessege,
    LocalDateTime errorTime
) {
    
}
