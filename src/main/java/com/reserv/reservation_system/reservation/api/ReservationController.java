package com.reserv.reservation_system.reservation.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reserv.reservation_system.reservation.domain.Reservation;
import com.reserv.reservation_system.reservation.service.ReservationSearchFilter;
import com.reserv.reservation_system.reservation.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
   
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);


    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation>  getReservationById(@PathVariable("id") Long id) {
        
        log.info("Called getReservationById() with id " + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam("roomId") Long roomId,
            @RequestParam("userId") Long userId,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("pageNumber") Integer pageNumber
    ) {
        log.info("Called getAllReservations()");
        var filter = new ReservationSearchFilter(
                roomId, 
                userId, 
                pageSize, 
                pageNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.searchAllByFilter());
    }

    @PostMapping
    public ResponseEntity<Reservation>  createReservation(
        @RequestBody @Valid Reservation reservationToCreate
    ) {
        
        log.info("Called createReservation");

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
        @PathVariable("id") Long id,
        @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Called updateReservation with id {id} for reservation {reservationToUpdate}",
            id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
        @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation with id {id} ", id);

        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve") 
    public ResponseEntity<Reservation> approveReservation(
        @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation with id {id} ", id);

        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
