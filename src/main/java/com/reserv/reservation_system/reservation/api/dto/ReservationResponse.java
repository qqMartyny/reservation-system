package com.reserv.reservation_system.reservation.api.dto;

import java.time.LocalDate;

import com.reserv.reservation_system.reservation.domain.ReservationStatus;


public record ReservationResponse(
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,
    ReservationStatus status
) {
    
}
