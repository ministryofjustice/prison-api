package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ReturnToCustodyDate;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFixedTermRecall;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFixedTermRecallRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional(readOnly = true)
public class OffenderFixedTermRecallService {

    private final OffenderFixedTermRecallRepository repository;

    public OffenderFixedTermRecallService(
        final OffenderFixedTermRecallRepository repository) {
        this.repository = repository;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public ReturnToCustodyDate getReturnToCustodyDate(Long bookingId) {
        return repository.findById(bookingId).map(OffenderFixedTermRecall::mapToReturnToCustody)
            .orElseThrow(EntityNotFoundException.withMessage(format("No fixed term recall found for booking %d", bookingId)));
    }
}
