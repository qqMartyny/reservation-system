package com.reserv.reservation_system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("No Reservation with id: " + id);
        }
        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(it ->
                    new Reservation(
                            it.getId(), 
                            it.getUserId(), 
                            it.getRoomId(), 
                            it.getStartDate(), 
                            it.getEndDate(), 
                            it.getStatus())
                ).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
    
        if (reservationToCreate.id() != null) {
            log.info(
                "Client tried to set id while creating a new Reservation:\n"
                + reservationToCreate
            );
            throw new IllegalArgumentException("id should be empty");
        }

        if (reservationToCreate.status() != null) {
            log.info(
                "Client tried to set status while creating a new Reservation:\n"
                + reservationToCreate
            );
            throw new IllegalArgumentException("status should be empty");
        }

        var newReservation = new Reservation(
            idCounter.incrementAndGet(),
            reservationToCreate.userId(),
            reservationToCreate.roomId(),
            reservationToCreate.startDate(),
            reservationToCreate.endDate(),
            ReservationStatus.PENDING
        );
        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("No Reservation with id: " + id);
        }

        var reservation = reservationMap.get(id);

        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't modify reservation with status=" 
            + reservation.status());
        }

        var updatedReservation = new Reservation(
            idCounter.incrementAndGet(),
            reservationToUpdate.userId(),
            reservationToUpdate.roomId(),
            reservationToUpdate.startDate(),
            reservationToUpdate.endDate(),
            ReservationStatus.PENDING
        );

        reservationMap.put(updatedReservation.id(), updatedReservation);
        return updatedReservation;
    }

    public void deleteReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("No Reservation with id: " + id);
        }
        reservationMap.remove(id);
    }

    public Reservation approveReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("No Reservation with id: " + id);
        }
        var reservation = reservationMap.get(id);

        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't approve reservation with status=" 
            + reservation.status());
        }
        var isConflict = isReservationConflict(reservation);
        if (isConflict) {
            throw new IllegalStateException("Can't approve reservation because of conflict");
        }

        var approvedReservation = new Reservation(
            idCounter.incrementAndGet(),
            reservation.userId(),
            reservation.roomId(),
            reservation.startDate(),
            reservation.endDate(),
            ReservationStatus.APPROVED
        );
        reservationMap.put(approvedReservation.id(), approvedReservation);
        return approvedReservation;
    }

    private boolean isReservationConflict(Reservation reservation) {
        for (Reservation existingReservation : reservationMap.values()) {
            if (reservation.id().equals(existingReservation.id()))
                continue;
            if (!reservation.roomId().equals(existingReservation.roomId()))
                continue;
            if (!existingReservation.status().equals(ReservationStatus.APPROVED))
                continue;
            if (reservation.startDate().isBefore(existingReservation.endDate())
                && existingReservation.startDate().isBefore(reservation.endDate()))
            return true;
        }

        return false;
    }
}