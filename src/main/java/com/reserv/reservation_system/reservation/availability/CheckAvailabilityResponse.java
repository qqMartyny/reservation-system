package com.reserv.reservation_system.reservation.availability;

public record CheckAvailabilityResponse(
    String message,
    AvailabilityStatus status
) {
    
}
