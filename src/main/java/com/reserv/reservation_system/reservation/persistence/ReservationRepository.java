package com.reserv.reservation_system.reservation.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reserv.reservation_system.reservation.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    
    @Modifying
    @Query(
        """
            UPDATE ReservationEntity r
            SET r.status = :status
            WHERE r.id = :id
        """)
    void setStatus(
        @Param("id") Long id,
        @Param("status") ReservationStatus status
    );

    @Query(
        """
            SELECT r.id from ReservationEntity r
            WHERE r.roomId = :roomId
            AND :startDate < r.endDate
            AND r.startDate < :endDate
            AND r.status = :status
        """)
    List<Long> findConflictReservationIds(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate starDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") ReservationStatus status
    );

    @Query(
        """
            SELECT r.id from ReservationEntity r
            WHERE (:roomId IS NULL OR r.roomId = :roomId)
            AND (:userId IS NULL OR r.userId = :userId)
        """)
    List<ReservationEntity> searchAllByFilter(
        @Param("roomId") Long roomId,
        @Param("userId") Long userId,
        Pageable pageable
    );
}
