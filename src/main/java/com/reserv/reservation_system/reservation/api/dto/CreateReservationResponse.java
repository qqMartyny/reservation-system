package com.reserv.reservation_system.reservation.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record CreateReservationResponse(
    @NotNull Long id,
    @NotNull Long userId,
    @NotNull Long roomId,
    @NotNull @FutureOrPresent LocalDate startDate,
    @NotNull @FutureOrPresent LocalDate endDate
) {
    
}
