package com.reserv.reservation_system;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {
   
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);


    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable("id") Long id) {
        
        log.info("Called getReservationById() with id " + id);
        return reservationService.getReservationById(id);
    }

    @GetMapping("/getAll")
    public List<Reservation> getAllReservations() {
        log.info("Called getAllReservations()");
        return reservationService.findAllReservations();
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservationToCreate) {
        
        log.info("Called createReservation");

        return reservationService.createReservation(reservationToCreate);
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
