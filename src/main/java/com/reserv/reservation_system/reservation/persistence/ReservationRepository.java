package com.reserv.reservation_system.reservation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reserv.reservation_system.reservation.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    
    @Modifying
    @Query(
        // language=HQL
        """
            UPDATE ReservationEntity r
            SET r.status = :status
            WHERE r.id = :id
            """)
    void setStatus(
        @Param("id") Long id,
        @Param("status") ReservationStatus status
    );
}
