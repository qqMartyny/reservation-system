package com.reserv.reservation_system.reservation.service;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reserv.reservation_system.reservation.domain.Reservation;
import com.reserv.reservation_system.reservation.domain.ReservationStatus;
import com.reserv.reservation_system.reservation.persistence.ReservationEntity;
import com.reserv.reservation_system.reservation.persistence.ReservationRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock 
    private ReservationRepository repository;

    @Mock
    private ReservationMapper mapper;

    @InjectMocks
    private ReservationService service;

    @Test
    void shouldReturnReservationById () {

        var entity = new ReservationEntity(
            1L,
            1L,
            1L,
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            ReservationStatus.PENDING
        );

        var domain = new Reservation(
            1L,
            1L,
            1L,
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            ReservationStatus.PENDING
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        var result = service.getReservationById(1L);

        assertThat(result).isEqualTo(domain);
        
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservationById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}
