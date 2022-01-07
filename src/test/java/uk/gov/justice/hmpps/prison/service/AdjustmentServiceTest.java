package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment;
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdjustmentServiceTest {

    @Mock
    private OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    @Mock
    private OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;

    private AdjustmentService service;

    private final Long BOOKING_ID = 1L;
    private final List<String> SENTENCE_ADJUSTMENT_CODES = List.of("RSR", "S240A", "RST", "RX", "UR");
    private final List<String> BOOKING_ADJUSTMENT_CODES = List.of("SREM", "ADA", "RADA", "UAL", "LAL");

    @BeforeEach
    public void setUp() {
        service = new AdjustmentService(offenderSentenceAdjustmentRepository, offenderKeyDateAdjustmentRepository);
    }

    @Test
    void getBookingAndSentenceAdjustmentsReturnsCorrectData() {
        final var sentenceAdjustment = SentenceAdjustment.builder()
            .sentenceSeq(911)
            .active(true)
            .adjustDays(4)
            .adjustFromDate(LocalDate.of(2022, 1, 1))
            .adjustToDate(LocalDate.of(2022, 1, 4))
            .build();

        final var keyDateAdjustment = KeyDateAdjustment.builder()
            .active(true)
            .adjustDays(4)
            .adjustFromDate(LocalDate.of(2022, 1, 1))
            .adjustToDate(LocalDate.of(2022, 1, 4))
            .build();


        when(offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(BOOKING_ID, SENTENCE_ADJUSTMENT_CODES)).thenReturn(List.of(sentenceAdjustment));
        when(offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(BOOKING_ID, BOOKING_ADJUSTMENT_CODES)).thenReturn(List.of(keyDateAdjustment));

        final var bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID);

        final var expected = BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(List.of(
                uk.gov.justice.hmpps.prison.api.model.SentenceAdjustment
                    .builder()
                    .sentenceSequence(911)
                    .active(true)
                    .numberOfDays(4)
                    .fromDate(LocalDate.of(2022, 1, 1))
                    .toDate(LocalDate.of(2022, 1, 4))
                    .build()))
            .bookingAdjustments(List.of(
                BookingAdjustment
                    .builder()
                    .active(true)
                    .numberOfDays(4)
                    .fromDate(LocalDate.of(2022, 1, 1))
                    .toDate(LocalDate.of(2022, 1, 4))
                    .build()
            ))
            .build();

        assertEquals(expected, bookingAndSentenceAdjustments);
    }

    @Test
    void getBookingAndSentenceAdjustmentsReturnsNoData() {
        when(offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(BOOKING_ID, SENTENCE_ADJUSTMENT_CODES)).thenReturn(List.of());
        when(offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(BOOKING_ID, BOOKING_ADJUSTMENT_CODES)).thenReturn(List.of());

        final var bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID);

        final var expected = BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(emptyList())
            .bookingAdjustments(emptyList())
            .build();

        assertEquals(expected, bookingAndSentenceAdjustments);
    }
}
