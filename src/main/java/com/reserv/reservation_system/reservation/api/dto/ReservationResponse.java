package com.reserv.reservation_system.reservation.api.dto;

import java.time.LocalDate;


public record ReservationResponse(
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate
) {
    
}
