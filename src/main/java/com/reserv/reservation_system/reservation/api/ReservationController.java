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

import com.reserv.reservation_system.reservation.api.dto.ReservationRequest;
import com.reserv.reservation_system.reservation.api.dto.ReservationResponse;
import com.reserv.reservation_system.reservation.domain.Reservation;
import com.reserv.reservation_system.reservation.service.ReservationMapper;
import com.reserv.reservation_system.reservation.service.ReservationSearchFilter;
import com.reserv.reservation_system.reservation.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
   
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);


    private final ReservationService reservationService;
    private final ReservationMapper mapper;

    public ReservationController(
        ReservationService reservationService,
        ReservationMapper mapper
    ) {
        this.reservationService = reservationService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse>  getReservationById(@PathVariable("id") Long id) {
        
        log.info("Called getReservationById() with id {}", id);

        return ResponseEntity.ok(
            mapper.toResponse(reservationService.getReservationById(id))
        );
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        log.info("Called getAllReservations()");
        var filter = new ReservationSearchFilter(
                roomId, 
                userId, 
                pageSize, 
                pageNumber);

        List<ReservationResponse> response = reservationService.searchAllByFilter(filter)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse>  createReservation(
        @RequestBody @Valid ReservationRequest request
    ) {
        
        log.info("Called createReservation");

        Reservation domain = mapper.toDomain(request);
        Reservation saved = reservationService.createReservation(domain);
        ReservationResponse response = mapper.toResponse(saved);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
        @PathVariable("id") Long id,
        @RequestBody @Valid ReservationRequest request
    ) {
        log.info("Called updateReservation with id {} for reservation {}",
            id, request);

        Reservation domain = mapper.toDomain(request);
        Reservation saved = reservationService.updateReservation(id, domain);
        ReservationResponse response = mapper.toResponse(saved);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
        @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation with id {}", id);

        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve") 
    public ResponseEntity<ReservationResponse> approveReservation(
        @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation with id {}", id);

        return ResponseEntity.ok(
            mapper.toResponse(reservationService.approveReservation(id))
        );
    }
}
