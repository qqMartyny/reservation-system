package com.reserv.reservation_system.reservation;

import java.time.LocalDate;

import com.reserv.reservation_system.reservation.domain.Reservation;
import com.reserv.reservation_system.reservation.domain.ReservationStatus;
import com.reserv.reservation_system.reservation.persistence.ReservationEntity;

public class ReservationFixtures {
    
    public static ReservationEntity defaultEntity() {
        return new ReservationEntity(
            1L,
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            ReservationStatus.PENDING
        );
    }

    public static Reservation defaultDomain() {
        return new Reservation(
            1L, 
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            ReservationStatus.PENDING
        );
    }

    public static Reservation domainWithStatus(ReservationStatus status) {
        return new Reservation(
            1L, 
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            status
        );
    }
}
