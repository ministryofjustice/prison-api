package uk.gov.justice.hmpps.nomis.prison.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.DuplicateOffenderRepository;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model.DuplicateOffender;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model.OffenderIdentifierPendingDeletion;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataDuplicateServiceTest {

    private static final String OFFENDER_NO = "A1234AA";
    private static final String OFFENDER_PNC = "1999/0123456X";
    private static final String FORMATTED_OFFENDER_PNC = "99/123456X";
    private static final String OFFENDER_CRO = "000001/11X";
    private static final String FORMATTED_OFFENDER_CRO = "11/1X";
    private static final String DUPLICATE_1 = "B1234BB";
    private static final String DUPLICATE_2 = "C1234CC";
    private static final String DUPLICATE_3 = "D1234DD";
    private static final long RETENTION_CHECK_ID = 123;

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private DuplicateOffenderRepository duplicateOffenderRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    private DataDuplicateService dataDuplicateService;

    @BeforeEach
    void setUp() {
        dataDuplicateService = new DataDuplicateService(offenderAliasPendingDeletionRepository, duplicateOffenderRepository, dataComplianceEventPusher);
    }

    @Test
    void checkForDuplicateIds() {

        mockIdentifiers(OFFENDER_NO, Map.of(
                "PNC", OFFENDER_PNC,
                "CRO", OFFENDER_CRO));

        when(duplicateOffenderRepository.getOffendersWithMatchingPncNumbers(OFFENDER_NO, Set.of(FORMATTED_OFFENDER_PNC)))
                .thenReturn(List.of(new DuplicateOffender(DUPLICATE_1)));
        when(duplicateOffenderRepository.getOffendersWithMatchingCroNumbers(OFFENDER_NO, Set.of(FORMATTED_OFFENDER_CRO)))
                .thenReturn(List.of(new DuplicateOffender(DUPLICATE_2)));
        when(duplicateOffenderRepository.getOffendersWithMatchingLidsNumbers(OFFENDER_NO))
                .thenReturn(List.of(new DuplicateOffender(DUPLICATE_3)));

        dataDuplicateService.checkForDuplicateIds(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).sendDuplicateIdResult(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .duplicateOffender(DUPLICATE_3)
                .duplicateOffender(DUPLICATE_2)
                .duplicateOffender(DUPLICATE_1)
                .build());
    }

    @Test
    void checkForDuplicateIdsReturnsEmptyIfNoMatchingIdentifiers() {

        mockIdentifiers(OFFENDER_NO, Map.of(
                "PNC", OFFENDER_PNC,
                "CRO", OFFENDER_CRO));

        when(duplicateOffenderRepository.getOffendersWithMatchingPncNumbers(OFFENDER_NO, Set.of(FORMATTED_OFFENDER_PNC)))
                .thenReturn(emptyList());
        when(duplicateOffenderRepository.getOffendersWithMatchingCroNumbers(OFFENDER_NO, Set.of(FORMATTED_OFFENDER_CRO)))
                .thenReturn(emptyList());
        when(duplicateOffenderRepository.getOffendersWithMatchingLidsNumbers(OFFENDER_NO))
                .thenReturn(emptyList());

        dataDuplicateService.checkForDuplicateIds(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).sendDuplicateIdResult(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .build());
    }

    @Test
    void checkForDuplicateIdsReturnsEmptyIfNoValidIdentifiers() {

        mockIdentifiers(OFFENDER_NO, Map.of(
                "PNC", "AN_INVALID_PNC",
                "CRO", "AN_INVALID_CRO"));

        when(duplicateOffenderRepository.getOffendersWithMatchingLidsNumbers(OFFENDER_NO))
                .thenReturn(emptyList());

        dataDuplicateService.checkForDuplicateIds(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).sendDuplicateIdResult(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .build());
    }

    @Test
    void checkForDataDuplicates() {

        when(duplicateOffenderRepository.getOffendersWithMatchingDetails(OFFENDER_NO))
                .thenReturn(List.of(new DuplicateOffender(DUPLICATE_1)));

        dataDuplicateService.checkForDataDuplicates(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).sendDuplicateDataResult(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .duplicateOffenders(List.of(DUPLICATE_1))
                .build());
    }

    @Test
    void checkForDataDuplicatesReturnsEmpty() {

        when(duplicateOffenderRepository.getOffendersWithMatchingDetails(OFFENDER_NO)).thenReturn(emptyList());

        dataDuplicateService.checkForDataDuplicates(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).sendDuplicateDataResult(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .duplicateOffenders(emptyList())
                .build());
    }

    private void mockIdentifiers(final String offenderNo, final Map<String, String> identifierMap) {

        final var identifiers = identifierMap.entrySet().stream()
                .map(entry -> OffenderIdentifierPendingDeletion.builder()
                        .identifierType(entry.getKey())
                        .identifier(entry.getValue())
                        .build())
                .collect(toSet());

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNo))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderIdentifiers(identifiers)
                        .build()));
    }
}
