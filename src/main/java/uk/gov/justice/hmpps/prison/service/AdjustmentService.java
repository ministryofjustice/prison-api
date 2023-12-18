package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.BookingAndSentenceAdjustments;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

@Service
@Slf4j
@AllArgsConstructor
public class AdjustmentService {

    private final OffenderBookingRepository offenderBookingRepository;

    @Transactional(readOnly = true)
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    public BookingAndSentenceAdjustments getBookingAndSentenceAdjustments(Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        return BookingAndSentenceAdjustments.builder()
            .sentenceAdjustments(offenderBooking.getSentenceAdjustments())
            .bookingAdjustments(offenderBooking.getBookingAdjustments())
            .build();
    }
}
