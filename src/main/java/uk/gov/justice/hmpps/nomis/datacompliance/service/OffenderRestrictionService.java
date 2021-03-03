package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.OffenderRestrictionRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderRestrictionResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderRestrictions;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderSentConditions;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderRestrictionsRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderSentConditionsRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OffenderRestrictionService {

    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final OffenderRestrictionsRepository offenderRestrictionsRepository;
    private final OffenderSentConditionsRepository offenderSentConditionsRepository;

    public void checkForOffenderRestrictions(final OffenderRestrictionRequest offenderRestrictionRequest){

        final String offenderNumber = offenderRestrictionRequest.getOffenderIdDisplay();
        final var offenderAliases = offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber);

        checkState(!offenderAliases.isEmpty(),
            "Expecting to find at least one offender id for offender: '%s'", offenderNumber);

        final Set<Long> bookIds = getBookIds(offenderAliases);

        final List<OffenderRestrictions> offenderRestrictions = offenderRestrictionsRepository.findOffenderRestrictions(bookIds, offenderRestrictionRequest.getRestrictionCodes(), offenderRestrictionRequest.getRegex());
        final List<OffenderSentConditions> childRelatedConditionsByBookings = offenderSentConditionsRepository.findChildRelatedConditionsByBookings(bookIds);

        pushOffenderRestrictionResult(offenderRestrictionRequest, isRestricted(offenderRestrictions, childRelatedConditionsByBookings));
    }


    private void pushOffenderRestrictionResult(final OffenderRestrictionRequest offenderRestrictionRequest, final boolean restricted) {
        dataComplianceEventPusher.send(OffenderRestrictionResult.builder()
            .offenderIdDisplay(offenderRestrictionRequest.getOffenderIdDisplay())
            .retentionCheckId(offenderRestrictionRequest.getRetentionCheckId())
            .restricted(restricted)
            .build());
    }

    private Set<Long> getBookIds(final Collection<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
            .map(OffenderAliasPendingDeletion::getOffenderBookings)
            .flatMap(Collection::stream)
            .map(OffenderBookingPendingDeletion::getBookingId)
            .collect(toSet());
    }

    private boolean isRestricted(List<OffenderRestrictions> offenderRestrictions, List<OffenderSentConditions> childRelatedConditionsByBookings) {
        return !(offenderRestrictions.isEmpty() && childRelatedConditionsByBookings.isEmpty());
    }
}

