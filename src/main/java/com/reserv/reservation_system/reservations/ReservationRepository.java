package com.reserv.reservation_system.reservations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
