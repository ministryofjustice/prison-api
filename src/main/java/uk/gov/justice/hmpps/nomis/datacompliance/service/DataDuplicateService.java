package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.DuplicateOffenderRepository;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataDuplicateService {

    private final DuplicateOffenderRepository duplicateOffenderRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;

    public void checkForDataDuplicates(final String offenderNo, final Long retentionCheckId) {

        final var duplicateOffenders = ImmutableSet.<String>builder()
                .addAll(getOffendersWithMatchingPncNumber(offenderNo))
                .build();

        // TODO GDPR-110 duplicate checks including:
        //  * CRO
        //  * LIDS Number
        //  * Personal data

        dataComplianceEventPusher.send(DataDuplicateResult.builder()
                .offenderIdDisplay(offenderNo)
                .retentionCheckId(retentionCheckId)
                .duplicateOffenders(duplicateOffenders)
                .build());
    }

    private Set<String> getOffendersWithMatchingPncNumber(final String offenderNo) {

        final var duplicates = duplicateOffenderRepository.getOffendersWithMatchingPncNumber(offenderNo).stream()
                .map(DuplicateOffender::getOffenderNumber)
                .collect(toSet());

        if (!duplicates.isEmpty()) {
            log.info("Found offender(s) ({}) with matching PNCs for offender '{}'", duplicates, offenderNo);
        }

        return duplicates;
    }
}
