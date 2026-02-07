package com.reserv.reservation_system.reservation.availability;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.reserv.reservation_system.reservation.domain.ReservationStatus;
import com.reserv.reservation_system.reservation.persistence.ReservationRepository;

@Service
public class ReservationAvailabilityService {
    
    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository;

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean isReservationAvailable(
        Long roomId,
        LocalDate startDate,
        LocalDate endDate
    ) {

        if (endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date has to be at least 1 more day than start date");
        }

        List<Long> conflictsWith = repository.findConflictReservationIds(
                roomId, 
                startDate, 
                endDate, 
                ReservationStatus.APPROVED);
        if (conflictsWith.isEmpty())
            return true;

        log.info("Conflicts with id=", conflictsWith);
        return false;
    }    

}
