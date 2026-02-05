package com.reserv.reservation_system.reservation.service;

import java.time.LocalDate;
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

    private ReservationMapper mapper;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Reservation getReservationById(Long id) {

        ReservationEntity reservationEntity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(it -> mapper.toDomain(it)
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

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
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

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedEntity = repository.save(reservationToSave);

        return mapper.toDomain(updatedEntity);
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

        var isConflict = isReservationConflict(
            reservationEntity.getRoomId(),
            reservationEntity.getStartDate(),
            reservationEntity.getEndDate()
        );

        if (isConflict) {
            throw new IllegalStateException("Can't approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

    private boolean isReservationConflict(
        Long roomId,
        LocalDate startDate,
        LocalDate endDate
    ) {

        List<Long> conflictsWith = repository.findConflictReservationIds(
                roomId, 
                startDate, 
                endDate, 
                ReservationStatus.APPROVED);
        if (conflictsWith.isEmpty())
            return false;

        log.info("Conflicts with id=", conflictsWith);
        return true;
    }

    
}