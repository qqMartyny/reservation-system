package com.reserv.reservation_system;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap = Map.of(
        1L, new Reservation(
            1L,
            12L,
            143L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        ), 
        2L, new Reservation(
            2L,
            13L,
            14123L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        ),
        3L, new Reservation(
            3L,
            13L,
            14343L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ReservationStatus.APPROVED
        ));

    public Reservation getReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("No Reservation with id: " + id);
        }
        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }
    
}