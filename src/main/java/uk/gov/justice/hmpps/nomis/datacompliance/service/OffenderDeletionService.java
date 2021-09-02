package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.config.DataComplianceProperties;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OffenderDeletionService {

    private final DataComplianceProperties properties;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final TelemetryClient telemetryClient;

    public void deleteOffender(final OffenderDeletionGrant grant) {

        checkState(properties.isDeletionEnabled(), "Deletion is not enabled!");

        checkRequestedDeletion(grant);

        offenderDeletionRepository.setContext(AppModuleName.MERGE);

        final var offenderIds = offenderDeletionRepository.cleanseOffenderDataExcludingBaseRecord(grant.getOffenderNo());

        offenderDeletionRepository.setContext(AppModuleName.PRISON_API);

        dataComplianceEventPusher.send(new OffenderDeletionComplete(grant.getOffenderNo(), grant.getReferralId()));

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", grant.getOffenderNo(), "count", String.valueOf(offenderIds.size())), null);
    }

    private void checkRequestedDeletion(final OffenderDeletionGrant grant) {

        final var offenderAliases = offenderAliasPendingDeletionRepository
                .findOffenderAliasPendingDeletionByOffenderNumber(grant.getOffenderNo());

        final var offenderIds = offenderAliases.stream()
                .map(OffenderAliasPendingDeletion::getOffenderId)
                .collect(toSet());

        final var offenderBookIds = offenderAliases.stream()
                .map(OffenderAliasPendingDeletion::getOffenderBookings)
                .flatMap(Collection::stream)
                .map(OffenderBookingPendingDeletion::getBookingId)
                .collect(toSet());

        checkState(offenderIds.equals(grant.getOffenderIds()),
                "The requested offender ids (%s) do not match those currently linked to offender '%s' (%s)",
                grant.getOffenderIds(), grant.getOffenderNo(), offenderIds);

        checkState(offenderBookIds.equals(grant.getOffenderBookIds()),
                "The requested offender book ids (%s) do not match those currently linked to offender '%s' (%s)",
                grant.getOffenderBookIds(), grant.getOffenderNo(), offenderBookIds);
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class OffenderDeletionGrant {
        private final String offenderNo;
        private final Long referralId;
        @Singular private final Set<Long> offenderIds;
        @Singular private final Set<Long> offenderBookIds;
    }
}
