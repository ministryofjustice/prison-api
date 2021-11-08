package uk.gov.justice.hmpps.prison.service;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;

import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class OffenderDatesService {

    private final OffenderSentenceRepository offenderSentenceRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final Clock clock;

    @Transactional
    public OffenderKeyDates updateOffenderKeyDates(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        // should we do any validation here of the payload compared with the existing dates we store for a sentence
        final var sentenceCalculation =
            SentenceCalculation.builder()
                .offenderBooking(offenderBooking)
                .reasonCode("NEW") // is this an enum in NOMIS?
                .calculationDate(LocalDate.now(clock)) // the payload will potentially include it?
                // do we need
                // STAFF_ID
                .crdCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getConditionalReleaseDate())
                .ledCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getLicenceExpiryDate())
                .sedCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getSentenceExpiryDate())
                .build();
        offenderBooking.addSentenceCalculation(sentenceCalculation);
        return requestToUpdateOffenderDates.getKeyDates();
    }

    // may not be required but leaving here until we have confirmation
    private void updateOffenderSentences(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        OffenderBooking offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        requestToUpdateOffenderDates.getSentenceDates().forEach(sentenceToUpdate -> {
            var pk = new OffenderSentence.PK(offenderBooking, sentenceToUpdate.getSentenceSequence());
            var existingOffenderSentence = offenderSentenceRepository.findById(pk).orElseThrow();
            var updatedOffenderSentenceBuilder =
                existingOffenderSentence.toBuilder();
            OffenderKeyDates sentenceOffenderKeyDates = sentenceToUpdate.getOffenderKeyDates();
            Optional<LocalDate> conditionalReleaseDate = Optional.fromNullable(sentenceOffenderKeyDates.getConditionalReleaseDate());
            Optional<LocalDate> licenceExpiryDate = Optional.fromNullable(sentenceOffenderKeyDates.getLicenceExpiryDate());
            Optional<LocalDate> sentenceExpiryDate = Optional.fromNullable(sentenceOffenderKeyDates.getSentenceExpiryDate());
        });
    }
}
