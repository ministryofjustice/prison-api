package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderIdentifierPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.DuplicateOffenderRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataDuplicateServiceTest {

    private static final String OFFENDER_NO = "A1234AA";
    private static final String OFFENDER_PNC = "1999/0123456X";
    private static final String STRIPPED_OFFENDER_PNC = "99/123456X";
    private static final String DUPLICATE_OFFENDER_1 = "B1234BB";
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
    void checkForDataDuplicates() {

        mockIdentifier(OFFENDER_NO, OFFENDER_PNC, "PNC");

        when(duplicateOffenderRepository.getOffendersWithMatchingPncNumbers(OFFENDER_NO, Set.of(STRIPPED_OFFENDER_PNC)))
                .thenReturn(List.of(new DuplicateOffender(DUPLICATE_OFFENDER_1)));

        dataDuplicateService.checkForDataDuplicates(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).send(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .duplicateOffender(DUPLICATE_OFFENDER_1)
                .build());
    }

    @Test
    void checkForDataDuplicatesReturnsEmptyIfNoMatchingIdentifiers() {

        mockIdentifier(OFFENDER_NO, OFFENDER_PNC, "PNC");

        when(duplicateOffenderRepository.getOffendersWithMatchingPncNumbers(OFFENDER_NO, Set.of(STRIPPED_OFFENDER_PNC)))
                .thenReturn(emptyList());

        dataDuplicateService.checkForDataDuplicates(OFFENDER_NO, RETENTION_CHECK_ID);
        
        verify(dataComplianceEventPusher).send(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .build());
    }

    @Test
    void checkForDataDuplicatesReturnsEmptyIfNoValidIdentifiers() {

        mockIdentifier(OFFENDER_NO, "INVALID", "PNC");

        dataDuplicateService.checkForDataDuplicates(OFFENDER_NO, RETENTION_CHECK_ID);

        verify(dataComplianceEventPusher).send(DataDuplicateResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .build());
    }

    private void mockIdentifier(final String offenderNo, final String identifier, final String identifierType) {
        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNo))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderIdentifier(OffenderIdentifierPendingDeletion.builder()
                                .identifier(identifier)
                                .identifierType(identifierType)
                                .build())
                        .build()));
    }
}
