package com.reserv.reservation_system;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    public Reservation getReservationById(Long id) {
        return new Reservation(
            id,
            1L,
            143L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        );    
    }

    public List<Reservation> findAllReservations() {
        return List.of(new Reservation(
            1L,
            1L,
            143L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        ), new Reservation(
            2L,
            1L,
            143L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        )
        );
    }
    
}