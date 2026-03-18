package com.reserv.reservation_system.reservation.api.dto;

import java.time.LocalDate;


public record CreateReservationResponse(
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate
) {
    
}
