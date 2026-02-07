package com.reserv.reservation_system.reservation.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.reserv.reservation_system.reservation.availability.ReservationAvailabilityService;
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

    private final ReservationMapper mapper;

    private final ReservationAvailabilityService availabilityService;

    public ReservationService(
        ReservationRepository repository, 
        ReservationMapper mapper,
        ReservationAvailabilityService availabilityService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id) {

        ReservationEntity reservationEntity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
        ReservationSearchFilter filter
    ) {

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream()
                .map(it -> mapper.toDomain(it)
                ).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("status should be empty");
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

    @Transactional
    public Reservation approveReservation(Long id) {

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No Reservation with id: " + id
                ));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't approve reservation with status=" 
            + reservationEntity.getStatus());
        }

        List<ReservationEntity> conflicts = repository.findAndLockConflictingReservations(
            reservationEntity.getRoomId(),
            reservationEntity.getStartDate(),
            reservationEntity.getEndDate(),
            ReservationStatus.APPROVED
        );

        if (!conflicts.isEmpty()) {
            log.warn(
                "Cannot approve reservation id={} due to conflicts: {}",
                id,
                conflicts.stream().map(ReservationEntity::getId).toList()
            );
            throw new IllegalStateException(
                "Cannot approve reservation beacause of conflicts: {}"
                + conflicts.stream().map(ReservationEntity::getId).toList()
            );
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }
}