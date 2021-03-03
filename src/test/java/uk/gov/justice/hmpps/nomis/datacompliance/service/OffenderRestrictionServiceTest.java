package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;
import java.util.Set;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderRestrictionServiceTest {

    private static final String OFFENDER_NO = "A1234AA";
    private static final long OFFENDER_ID = 123;
    private static final long BOOK_ID = 456;
    private static final String OFFENDER_RESTRICTION_CODE = "CHILD";
    private static final long RETENTION_CHECK_ID = 789;
    public static final long OFFENDER_SENT_CONDITION_ID = 1109L;
    private static final String REGEX = "^(some|regex)$";
    private static final String COMMENT_TEXT = "some comment text";

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private OffenderRestrictionsRepository offenderRestrictionsRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    @Mock
    private OffenderSentConditionsRepository offenderSentConditionsRepository;


    private OffenderRestrictionService offenderRestrictionService;

    @BeforeEach
    void setUp() {
        offenderRestrictionService = new OffenderRestrictionService(dataComplianceEventPusher, offenderAliasPendingDeletionRepository, offenderRestrictionsRepository, offenderSentConditionsRepository);
    }

    @Test
    void checkForOffenderRestrictions() {

        when(offenderRestrictionsRepository.findOffenderRestrictions(Set.of(BOOK_ID), Set.of(OFFENDER_RESTRICTION_CODE), REGEX))
            .thenReturn(List.of(new OffenderRestrictions(BOOK_ID, OFFENDER_ID, OFFENDER_RESTRICTION_CODE, COMMENT_TEXT)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
            .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                .offenderId(OFFENDER_ID)
                .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(BOOK_ID).build())
                .build()));

        when(offenderSentConditionsRepository.findChildRelatedConditionsByBookings(Set.of(BOOK_ID)))
            .thenReturn(List.of(new OffenderSentConditions(OFFENDER_SENT_CONDITION_ID, BOOK_ID, "N", "Y")));

        offenderRestrictionService.checkForOffenderRestrictions(OffenderRestrictionRequest.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restrictionCode(OFFENDER_RESTRICTION_CODE)
            .regex(REGEX)
            .build());

        verify(dataComplianceEventPusher).send(OffenderRestrictionResult.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restricted(true)
            .build());
    }


    @Test
    void checkForOffenderRestrictionsAndSentConditionsMatch() {

        when(offenderRestrictionsRepository.findOffenderRestrictions(Set.of(BOOK_ID), Set.of(OFFENDER_RESTRICTION_CODE), REGEX))
            .thenReturn(EMPTY_LIST);

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
            .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                .offenderId(OFFENDER_ID)
                .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(BOOK_ID).build())
                .build()));

        when(offenderSentConditionsRepository.findChildRelatedConditionsByBookings(Set.of(BOOK_ID)))
            .thenReturn(List.of(new OffenderSentConditions(OFFENDER_SENT_CONDITION_ID, BOOK_ID, "Y", "N")));

        offenderRestrictionService.checkForOffenderRestrictions(OffenderRestrictionRequest.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restrictionCode(OFFENDER_RESTRICTION_CODE)
            .regex(REGEX)
            .build());

        verify(dataComplianceEventPusher).send(OffenderRestrictionResult.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restricted(true)
            .build());
    }

    @Test
    void checkForOffenderRestrictionsNoMatch() {

        when(offenderRestrictionsRepository.findOffenderRestrictions(Set.of(BOOK_ID), Set.of(OFFENDER_RESTRICTION_CODE), REGEX))
            .thenReturn(EMPTY_LIST);

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
            .thenReturn(List.of(OffenderAliasPendingDeletion.builder()
                .offenderId(OFFENDER_ID)
                .offenderBooking(OffenderBookingPendingDeletion.builder().bookingId(BOOK_ID).build())
                .build()));

        when(offenderSentConditionsRepository.findChildRelatedConditionsByBookings(Set.of(BOOK_ID)))
            .thenReturn(EMPTY_LIST);

        offenderRestrictionService.checkForOffenderRestrictions(OffenderRestrictionRequest.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restrictionCode(OFFENDER_RESTRICTION_CODE)
            .regex(REGEX)
            .build());

        verify(dataComplianceEventPusher).send(OffenderRestrictionResult.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restricted(false)
            .build());
    }

    @Test
    void checkForOffenderRestrictionsThrowsIfOffenderNotFound() {

        final OffenderRestrictionRequest offenderRestrictionRequest = OffenderRestrictionRequest.builder()
            .offenderIdDisplay(OFFENDER_NO)
            .retentionCheckId(RETENTION_CHECK_ID)
            .restrictionCode(OFFENDER_RESTRICTION_CODE)
            .regex(REGEX)
            .build();

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NO))
            .thenReturn(emptyList());

        assertThatThrownBy(() -> offenderRestrictionService.checkForOffenderRestrictions(offenderRestrictionRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Expecting to find at least one offender id for offender: 'A1234AA'");

        verifyNoInteractions(dataComplianceEventPusher);
    }

}