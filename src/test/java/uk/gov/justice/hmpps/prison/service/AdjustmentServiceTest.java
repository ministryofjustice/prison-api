package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment;
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.api.support.BookingAdjustmentType.ADDITIONAL_DAYS_AWARDED;
import static uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType.RECALL_SENTENCE_REMAND;

@ExtendWith(MockitoExtension.class)
public class AdjustmentServiceTest {

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    private AdjustmentService service;

    private final Long BOOKING_ID = 1L;

    @BeforeEach
    public void setUp() {
        service = new AdjustmentService(offenderBookingRepository);
    }

    @Test
    void getBookingAndSentenceAdjustmentsReturnsCorrectData() {
        final var sentenceAdjustment = SentenceAdjustment.builder()
            .sentenceSeq(911)
            .active(true)
            .sentenceAdjustCode("RSR")
            .adjustDays(4)
            .adjustFromDate(LocalDate.of(2022, 1, 1))
            .adjustToDate(LocalDate.of(2022, 1, 4))
            .build();

        final var keyDateAdjustment = KeyDateAdjustment.builder()
            .active(true)
            .sentenceAdjustCode("ADA")
            .adjustDays(4)
            .adjustFromDate(LocalDate.of(2022, 1, 1))
            .adjustToDate(LocalDate.of(2022, 1, 4))
            .build();

        when(offenderBookingRepository.findById(BOOKING_ID)).thenReturn(
            Optional.of(
                OffenderBooking.builder()
                    .sentenceAdjustments(List.of(sentenceAdjustment))
                    .keyDateAdjustments(List.of(keyDateAdjustment))
                    .build()
            )
        );

        final var bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID);

        final var expected = BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(List.of(
                SentenceAdjustmentValues
                    .builder()
                    .sentenceSequence(911)
                    .active(true)
                    .type(RECALL_SENTENCE_REMAND)
                    .numberOfDays(4)
                    .fromDate(LocalDate.of(2022, 1, 1))
                    .toDate(LocalDate.of(2022, 1, 4))
                    .build()))
            .bookingAdjustments(List.of(
                BookingAdjustment
                    .builder()
                    .active(true)
                    .type(ADDITIONAL_DAYS_AWARDED)
                    .numberOfDays(4)
                    .fromDate(LocalDate.of(2022, 1, 1))
                    .toDate(LocalDate.of(2022, 1, 4))
                    .build()
            ))
            .build();

        assertThat(bookingAndSentenceAdjustments).isEqualTo(expected);
    }

    @Test
    void getBookingAndSentenceAdjustmentsReturnsNoData() {
        when(offenderBookingRepository.findById(BOOKING_ID)).thenReturn(
            Optional.of(
                OffenderBooking.builder()
                    .build()
            )
        );

        final var bookingAndSentenceAdjustments = service.getBookingAndSentenceAdjustments(BOOKING_ID);

        final var expected = BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(emptyList())
            .bookingAdjustments(emptyList())
            .build();

        assertThat(bookingAndSentenceAdjustments).isEqualTo(expected);
    }
}
