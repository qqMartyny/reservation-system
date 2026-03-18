package com.reserv.reservation_system.reservation.domain;

import java.time.LocalDate;
public record Reservation(
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,

    ReservationStatus status
) {
    public Reservation {
        if(!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
    
}
