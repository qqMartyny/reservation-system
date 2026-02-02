package com.reserv.reservation_system;

import java.time.LocalDate;

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
    
}