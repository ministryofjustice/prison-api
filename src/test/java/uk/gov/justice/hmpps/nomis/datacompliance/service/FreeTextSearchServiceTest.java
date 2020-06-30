package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.FreeTextRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreeTextSearchServiceTest {

    private static final String OFFENDER_NO = "A1234AA";
    private static final long OFFENDER_ID = 123;
    private static final long BOOK_ID = 456;
    private static final long RETENTION_CHECK_ID = 789;
    private static final String REGEX = "^(some|regex)$";
    private static final String MATCHING_TABLE_1 = "table1";
    private static final String MATCHING_TABLE_2 = "table2";

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private FreeTextRepository freeTextRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    private FreeTextSearchService freeTextSearchService;

    @BeforeEach
    void setUp() {
        freeTextSearchService = new FreeTextSearchService(offenderAliasPendingDeletionRepository, freeTextRepository, dataComplianceEventPusher);
    }

    @Test
    void checkForMatchingContent() {

        when(freeTextRepository.findMatchUsingOffenderIds(Set.of(OFFENDER_ID), REGEX))
                .thenReturn(List.of(new FreeTextMatch(MATCHING_TABLE_1)));
        when(freeTextRepository.findMatchUsingBookIds(Set.of(BOOK_ID), REGEX))
                .thenReturn(List.of(new FreeTextMatch(MATCHING_TABLE_2)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderId(OFFENDER_ID)
                        .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(BOOK_ID).build())
                        .build()));

        freeTextSearchService.checkForMatchingContent(OFFENDER_NO, RETENTION_CHECK_ID, List.of(REGEX));

        verify(dataComplianceEventPusher).send(FreeTextSearchResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .matchingTable(MATCHING_TABLE_1)
                .matchingTable(MATCHING_TABLE_2)
                .build());
    }

    @Test
    void checkForMatchingContentFindsNoMatch() {

        when(freeTextRepository.findMatchUsingOffenderIds(Set.of(OFFENDER_ID), REGEX)).thenReturn(emptyList());
        when(freeTextRepository.findMatchUsingBookIds(Set.of(BOOK_ID), REGEX)).thenReturn(emptyList());

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
                .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                        .offenderId(OFFENDER_ID)
                        .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(BOOK_ID).build())
                        .build()));

        freeTextSearchService.checkForMatchingContent(OFFENDER_NO, RETENTION_CHECK_ID, List.of(REGEX));

        verify(dataComplianceEventPusher).send(FreeTextSearchResult.builder()
                .offenderIdDisplay(OFFENDER_NO)
                .retentionCheckId(RETENTION_CHECK_ID)
                .build());
    }

    @Test
    void checkForMatchingContentThrowsIfOffenderNotFound() {

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> freeTextSearchService.checkForMatchingContent(OFFENDER_NO, RETENTION_CHECK_ID, List.of(REGEX)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expecting to find at least one offender id for offender: 'A1234AA'");

        verifyNoInteractions(dataComplianceEventPusher);
    }
}