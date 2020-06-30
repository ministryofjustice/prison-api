package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.FreeTextRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FreeTextSearchService {

    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final FreeTextRepository freeTextRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;

    public void checkForMatchingContent(final String offenderNumber, final long retentionCheckId, final String regex) {
        dataComplianceEventPusher.send(FreeTextSearchResult.builder()
                .offenderIdDisplay(offenderNumber)
                .retentionCheckId(retentionCheckId)
                .matchingTables(getTablesWithMatchingContent(offenderNumber, regex))
                .build());
    }

    private Set<String> getTablesWithMatchingContent(final String offenderNumber, final String regex) {

        final var offenderAliases = offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber);

        checkState(!offenderAliases.isEmpty(),
                "Expecting to find at least one offender id for offender: '%s'", offenderNumber);

        return ImmutableSet.<String>builder()
                .addAll(getTableNames(freeTextRepository.findMatchUsingOffenderIds(getOffenderIds(offenderAliases), regex)))
                .addAll(getTableNames(freeTextRepository.findMatchUsingBookIds(getBookIds(offenderAliases), regex)))
                .build();
    }

    private Set<Long> getOffenderIds(final Collection<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
                .map(OffenderAliasPendingDeletion::getOffenderId)
                .collect(toSet());
    }

    private Set<Long> getBookIds(final Collection<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
                .map(OffenderAliasPendingDeletion::getOffenderBookings)
                .flatMap(Collection::stream)
                .map(OffenderBookingPendingDeletion::getBookingId)
                .collect(toSet());
    }

    private Set<String> getTableNames(final Collection<FreeTextMatch> matches) {
        return matches.stream().map(FreeTextMatch::getTableName).collect(toSet());
    }
}
