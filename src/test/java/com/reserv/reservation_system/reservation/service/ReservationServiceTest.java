package com.reserv.reservation_system.reservation.service;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reserv.reservation_system.reservation.ReservationFixtures;
import com.reserv.reservation_system.reservation.domain.ReservationStatus;
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

        var entity = ReservationFixtures.defaultEntity(ReservationStatus.PENDING);
        var domain = ReservationFixtures.defaultDomain(ReservationStatus.PENDING);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        var result = service.getReservationById(entity.getId());

        assertThat(result).isEqualTo(domain);
        
        assertThat(result.id()).isEqualTo(entity.getId());
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void shouldThrowEntityNotFoundException() {
        
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservationById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void shouldThrowExceptionWhenStatusIsSpecified() {

        var domain = ReservationFixtures.defaultDomain(
            ReservationStatus.APPROVED
        );
        
        assertThatThrownBy(() -> service.createReservation(domain))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void succesfulReservationCreating() {

        var inputDomain = ReservationFixtures.defaultDomain(null);
        var entity = ReservationFixtures.defaultEntity(ReservationStatus.PENDING);
        var outputDomain = ReservationFixtures.defaultDomain(ReservationStatus.PENDING);

        when(mapper.toEntity(inputDomain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(outputDomain);

        service.createReservation(inputDomain);

        assertThat(entity.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

}
