package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CalcReasonType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class OffenderDatesService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final ReferenceCodeRepository<CalcReasonType> calcReasonTypeReferenceCodeRepository;
    private final Clock clock;

    @Transactional
    public SentenceCalcDates updateOffenderKeyDates(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        final var staff = requestToUpdateOffenderDates.getSubmissionUser() != null && staffUserAccountRepository.findById(requestToUpdateOffenderDates.getSubmissionUser()).map(StaffUserAccount::getStaff).isPresent()
            ? staffUserAccountRepository.findById(requestToUpdateOffenderDates.getSubmissionUser()).map(StaffUserAccount::getStaff).get()
            : null;

        final var sentenceCalculation =
            SentenceCalculation.builder()
                .offenderBooking(offenderBooking)
                .calcReasonType(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("OVERRIDE")).orElseThrow(EntityNotFoundException.withId("OVERRIDE"))) // Confirm this with CRD team
                .calculationDate(LocalDate.now(clock))
                .comments("Calculated externally") // Confirm this with CRD team 
                .staff(staff)
                .crdCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getConditionalReleaseDate())
                .ledCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getLicenceExpiryDate())
                .sedCalculatedDate(requestToUpdateOffenderDates.getKeyDates().getSentenceExpiryDate())
                .build();
        offenderBooking.addSentenceCalculation(sentenceCalculation);
        return offenderBooking.getSentenceCalcDates(Optional.of(sentenceCalculation));
    }
}
