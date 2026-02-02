package com.reserv.reservation_system;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

    @GetMapping("/getAll")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("Called getAllReservations()");
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.findAllReservations());
    }

    @PostMapping
    public ResponseEntity<Reservation>  createReservation(@RequestBody Reservation reservationToCreate) {
        
        log.info("Called createReservation");

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
        
        // return reservationService.createReservation(reservationToCreate);
        
        // return new Reservation(
        //     reservationToCreate.id(),
        //     reservationToCreate.userId(),
        //     reservationToCreate.roomId(),
        //     reservationToCreate.startDate(),
        //     reservationToCreate.endDate(),
        //     reservationToCreate.status() 
        // );
    }
}
