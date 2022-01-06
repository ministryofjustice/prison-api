package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment;
import uk.gov.justice.hmpps.prison.api.support.BookingAdjustmentType;
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class AdjustmentService {

    private final OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    private final OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;

    @Transactional(readOnly = true)
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public BookingAndSentenceAdjustments getBookingAndSentenceAdjustments(Long bookingId) {
        return BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(getSentenceAdjustments(bookingId))
            .bookingAdjustments(getBookingAdjustments(bookingId))
            .build();
    }

    private List<SentenceAdjustment> getSentenceAdjustments(Long bookingId) {
        List<String> sentenceAdjustmentCodes = Stream.of(
            SentenceAdjustmentType.values()).map(SentenceAdjustmentType::getCode).collect(Collectors.toList()
        );
        return offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(bookingId, sentenceAdjustmentCodes)
            .stream()
            .map(e -> SentenceAdjustment.builder()
                .type(SentenceAdjustmentType.getByCode(e.getSentenceAdjustCode()))
                .sentenceSequence(e.getSentenceSeq())
                .numberOfDays(e.getAdjustDays())
                .fromDate(e.getAdjustFromDate())
                .toDate(e.getAdjustToDate())
                .active(e.isActive())
                .build()
            )
            .collect(Collectors.toList());
    }

    private List<BookingAdjustment> getBookingAdjustments(Long bookingId) {
        List<String> bookingAdjustmentCodes = Stream.of(
            BookingAdjustmentType.values()).map(BookingAdjustmentType::getCode).collect(Collectors.toList()
        );
        return offenderKeyDateAdjustmentRepository.findAllByOffenderBooking_BookingIdAndSentenceAdjustCodeIn(bookingId, bookingAdjustmentCodes)
            .stream()
            .map(e -> BookingAdjustment.builder()
                .type(BookingAdjustmentType.getByCode(e.getSentenceAdjustCode()))
                .numberOfDays(e.getAdjustDays())
                .fromDate(e.getAdjustFromDate())
                .toDate(e.getAdjustToDate())
                .active(e.isActive())
                .build()
            )
            .collect(Collectors.toList());
    }
}
