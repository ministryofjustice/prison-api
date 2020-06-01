package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderIdentifierPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.DuplicateOffenderRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.service.IdentifierValidation.ChecksumComponents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataDuplicateService {

    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final DuplicateOffenderRepository duplicateOffenderRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;

    public void checkForDataDuplicates(final String offenderNo, final Long retentionCheckId) {

        final var offenderAliases =
                offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNo);

        final var duplicateOffenders = ImmutableSet.<String>builder()
                .addAll(getOffendersWithMatchingPncNumbers(offenderNo, offenderAliases))
                .addAll(getOffendersWithMatchingCroNumbers(offenderNo, offenderAliases))
                .build();

        // TODO GDPR-110 duplicate checks including:
        //  * LIDS Number
        //  * Personal data

        dataComplianceEventPusher.send(DataDuplicateResult.builder()
                .offenderIdDisplay(offenderNo)
                .retentionCheckId(retentionCheckId)
                .duplicateOffenders(duplicateOffenders)
                .build());
    }

    private Set<String> getOffendersWithMatchingCroNumbers(final String offenderNo,
                                                           final List<OffenderAliasPendingDeletion> offenderAliases) {

        final var croNumbers = getFormattedCroNumbersFrom(offenderAliases);

        if (croNumbers.isEmpty()) {
            return emptySet();
        }

        final var duplicates = duplicateOffenderRepository.getOffendersWithMatchingCroNumbers(offenderNo, croNumbers).stream()
                .map(DuplicateOffender::getOffenderNumber)
                .collect(toSet());

        if (!duplicates.isEmpty()) {
            log.info("Found offender(s) ({}) with matching CROs for offender '{}'", duplicates, offenderNo);
        }

        return duplicates;
    }

    private Set<String> getFormattedCroNumbersFrom(final List<OffenderAliasPendingDeletion> offenderAliases) {
        return getIdentifiersFrom(offenderAliases, OffenderIdentifierPendingDeletion::isCro)
                .map(IdentifierValidation::getValidCroComponents)
                .flatMap(Optional::stream)
                .map(this::formatChecksumComponentsWithNoLeadingZeros)
                .collect(toSet());
    }

    private Set<String> getOffendersWithMatchingPncNumbers(final String offenderNo,
                                                           final List<OffenderAliasPendingDeletion> offenderAliases) {

        final var pncNumbers = getFormattedPncNumbersFrom(offenderAliases);

        if (pncNumbers.isEmpty()) {
            return emptySet();
        }

        final var duplicates = duplicateOffenderRepository.getOffendersWithMatchingPncNumbers(offenderNo, pncNumbers).stream()
                .map(DuplicateOffender::getOffenderNumber)
                .collect(toSet());

        if (!duplicates.isEmpty()) {
            log.info("Found offender(s) ({}) with matching PNCs for offender '{}'", duplicates, offenderNo);
        }

        return duplicates;
    }

    private Set<String> getFormattedPncNumbersFrom(final List<OffenderAliasPendingDeletion> offenderAliases) {
        return getIdentifiersFrom(offenderAliases, OffenderIdentifierPendingDeletion::isPnc)
                .map(IdentifierValidation::getValidPncComponents)
                .flatMap(Optional::stream)
                .map(this::formatChecksumComponentsWithNoLeadingZeros)
                .collect(toSet());
    }

    private String formatChecksumComponentsWithNoLeadingZeros(final ChecksumComponents components) {
        return components.getYear() + "/" + parseInt(components.getSerial()) + components.getChecksum();
    }

    private Stream<String> getIdentifiersFrom(final Collection<OffenderAliasPendingDeletion> offenderAliasPendingDeletions,
                                              final Predicate<OffenderIdentifierPendingDeletion> typeFilter) {
        return offenderAliasPendingDeletions.stream()
                .map(OffenderAliasPendingDeletion::getOffenderIdentifiers)
                .flatMap(Collection::stream)
                .filter(typeFilter)
                .map(OffenderIdentifierPendingDeletion::getIdentifier)
                .map(String::toUpperCase)
                .map(String::strip);
    }
}
