package com.reserv.reservation_system;

import java.time.LocalDate;

public record Reservation(
    Long id,
    Long userId,
    Short roomId,
    LocalDate startDate,
    LocalDate endDate,
    ReservationStatus status
) {
    
}
