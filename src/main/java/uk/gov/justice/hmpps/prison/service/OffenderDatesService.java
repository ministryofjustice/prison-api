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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceCalculationRepository;

import java.time.LocalDate;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class OffenderDatesService {

    private final OffenderSentenceRepository offenderSentenceRepository;
    private final SentenceCalculationRepository sentenceCalculationRepository;

    @Transactional
    public void updateOffender(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        updateOffenderSentences(bookingId, requestToUpdateOffenderDates);
        return;
    }

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
