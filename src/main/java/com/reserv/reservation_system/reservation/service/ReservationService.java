package com.reserv.reservation_system.reservation.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.reserv.reservation_system.reservation.domain.Reservation;
import com.reserv.reservation_system.reservation.domain.ReservationStatus;
import com.reserv.reservation_system.reservation.persistence.ReservationEntity;
import com.reserv.reservation_system.reservation.persistence.ReservationRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    public Reservation getReservationById(Long id) {

        ReservationEntity reservationEntity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));
        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(it -> toDomainReservation(it)
                ).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("status should be empty");
        }

        if (!reservationToCreate.endDate()
                .isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("End date has to be at least 1 more day than start date");
        }

        var entityToSave = new ReservationEntity(
            null,
            reservationToCreate.userId(),
            reservationToCreate.roomId(),
            reservationToCreate.startDate(),
            reservationToCreate.endDate(),
            ReservationStatus.PENDING
        );
        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));
        
        if (!reservationToUpdate.endDate()
                .isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("End date has to be at least 1 more day than start date");
        }

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't modify reservation with status=" 
            + reservationEntity.getStatus());
        }

        var reservationToSave = new ReservationEntity(
            reservationEntity.getId(),
            reservationToUpdate.userId(),
            reservationToUpdate.roomId(),
            reservationToUpdate.startDate(),
            reservationToUpdate.endDate(),
            ReservationStatus.PENDING
        );

        var updatedEntity =  repository.save(reservationToSave);
        return toDomainReservation(updatedEntity);
    }

    @Transactional
    public void cancelReservation(Long id) {

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));

        if (reservationEntity.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with us");
        }

        if (reservationEntity.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("The reservation is already cancelled");
        }
        
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Succesfully cancelled reservation with id=" + id);
    }

    public Reservation approveReservation(Long id) {

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                    "No Reservation with id: " + id
                ));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't approve reservation with status=" 
            + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(reservationEntity);

        if (isConflict) {
            throw new IllegalStateException("Can't approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflict(ReservationEntity reservation) {

        List<ReservationEntity> allReservations = repository.findAll();

        for (ReservationEntity existingReservation : allReservations) {
            if (reservation.getId().equals(existingReservation.getId()))
                continue;
            if (!reservation.getRoomId().equals(existingReservation.getRoomId()))
                continue;
            if (!existingReservation.getStatus().equals(ReservationStatus.APPROVED))
                continue;
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate())
                && existingReservation.getStartDate().isBefore(reservation.getEndDate()))
            return true;
        }

        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
            reservation.getId(),
            reservation.getUserId(),
            reservation.getRoomId(),
            reservation.getStartDate(),
            reservation.getEndDate(),
            reservation.getStatus()        
        );
    }
}