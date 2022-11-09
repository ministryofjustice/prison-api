package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFixedTermRecall;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFixedTermRecallRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link OffenderFixedTermRecallService}.
 */
@ExtendWith(MockitoExtension.class)
public class OffenderFixedTermRecallServiceTest {
    @Mock
    private OffenderFixedTermRecallRepository repository;

    private OffenderFixedTermRecallService offenderFixedTermRecallService;

    @BeforeEach
    public void init() {
        offenderFixedTermRecallService = new OffenderFixedTermRecallService(repository);
    }

    @Test
    public void testGetReturnToCustodyDate() {

        final var bookingId = 1L;
        final var returnToCustodyDate = LocalDate.now();

        when(repository.findById(bookingId)).thenReturn(Optional.of(OffenderFixedTermRecall.builder()
            .returnToCustodyDate(returnToCustodyDate)
            .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
            .build()));

        final var result = offenderFixedTermRecallService.getReturnToCustodyDate(bookingId);

        assertThat(result.getBookingId()).isEqualTo(bookingId);
        assertThat(result.getReturnToCustodyDate()).isEqualTo(returnToCustodyDate);
    }

    @Test
    public void testGetReturnToCustodyDate_notFound() {
        final var bookingId = 1L;

        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offenderFixedTermRecallService.getReturnToCustodyDate(bookingId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("No fixed term recall found for booking 1");
    }

    @Test
    public void testGetFixedTermRecallDetails() {

        final var bookingId = 1L;
        final var returnToCustodyDate = LocalDate.now();
        final var recallLength = 14;

        when(repository.findById(bookingId)).thenReturn(Optional.of(OffenderFixedTermRecall.builder()
            .returnToCustodyDate(returnToCustodyDate)
            .recallLength(recallLength)
            .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
            .build()));

        final var result = offenderFixedTermRecallService.getFixedTermRecallDetails(bookingId);

        assertThat(result).isEqualTo(FixedTermRecallDetails
            .builder()
            .bookingId(bookingId)
            .returnToCustodyDate(returnToCustodyDate)
            .recallLength(recallLength)
            .build());
    }

    @Test
    public void testFixedTermRecallDetails_notFound() {
        final var bookingId = 1L;

        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offenderFixedTermRecallService.getFixedTermRecallDetails(bookingId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("No fixed term recall found for booking 1");
    }
}
