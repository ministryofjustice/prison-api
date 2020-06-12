package net.syscon.elite.service;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtHearingCancellationServiceTest {

    @Mock
    private CourtEventRepository eventRepository;

    @Mock
    private CourtEvent courtEvent;

    private CourtHearingCancellationService cancellationService;

    @BeforeEach
    void setUp() {
        cancellationService = new CourtHearingCancellationService(eventRepository);
    }

    @Test
    void cancel_fails_when_not_found() {
        when(eventRepository.findByOffenderBooking_BookingIdAndId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cancellationService.cancel(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Count hearing '2' with booking '1' not found.");
    }

    @Test
    void cancel_succeeds_when_found() {
        when(eventRepository.findByOffenderBooking_BookingIdAndId(any(), any())).thenReturn(Optional.of(courtEvent));

        cancellationService.cancel(1L , 2L);

        verify(eventRepository).delete(courtEvent);
    }
}
