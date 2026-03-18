package com.reserv.reservation_system.reservation.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reserv.reservation_system.reservation.persistence.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock 
    private ReservationRepository repository;

    @Mock
    private ReservationMapper mapper;

    @InjectMocks
    private ReservationService service;
}
