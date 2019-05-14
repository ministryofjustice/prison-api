package net.syscon.elite.service.impl.curfews;

import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.OffenderCurfew;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.syscon.elite.service.impl.curfews.OffenderCurfewServiceImpl.currentOffenderCurfews;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)

public class OffenderCurfewServiceImplTest {

    private static final String AGENCY_ID = "LEI";
    private static final String USERNAME = "ERIC";
    private static final int CUTOFF_DAYS_OFFSET = 28;

    private static final LocalDate TODAY = LocalDate.of(2017, 6, 15);
    private static final LocalDate HDCED = LocalDate.of(2018, 12, 12);

    // Meh. There is no mapping from a LocalDate to an Instant - because a LocalDate doesn't fully encode an instant.
    // Nevertheless, clock and TODAY should be aligned in the sense asserted by the first test below.
    private static final Clock clock = Clock.fixed(Instant.parse("2017-06-15T00:00:00Z"), ZoneId.of("UTC"));


    private OffenderCurfewService offenderCurfewService;

    @Mock
    private BookingService bookingService;

    @Mock
    private OffenderCurfewRepository offenderCurfewRepository;

    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    @Before
    public void configureService() {
        offenderCurfewService = new OffenderCurfewServiceImpl(
                offenderCurfewRepository,
                caseloadToAgencyMappingService,
                bookingService,
                referenceDomainService,
                clock);
    }

    /**
     * Demonstrates that 'clock' and TODAY are aligned...
     */
    @Test
    public void dayRepresentedByFixedClockAndTODAYshouldBeEquivalent() {
        assertThat(LocalDate.now(clock)).isEqualTo(TODAY);
    }

    @Test
    public void givenTwoOffenderCurfewWhenComparedThenOrderingIsCorrect() {

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(2, 1, null)
                )).isEqualTo(-1);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(0);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(2, 1, null),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(1);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(1, 1, "2018-01-01"),
                        offenderCurfew(2, 1, "2018-01-01")
                )).isEqualTo(-1);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, "2018-01-01")
                )).isEqualTo(1);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, "2018-01-02")
                )).isEqualTo(-1);

        assertThat(
                OffenderCurfewServiceImpl.OFFENDER_CURFEW_COMPARATOR.compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(-1);
    }


    @Test
    public void givenNoCurfewsForAgencyWhenFilteredForCurrentCurfewThenTheResultShouldBeEmpty() {
        assertThat(currentOffenderCurfews(Collections.emptyList()).collect(toList())).isEmpty();
    }


    @Test
    public void givenOneCurfewWhenFilteredForCurrentCurfewThenTheCurfewShouldBeReturned() {
        final var curfew = offenderCurfew(1, 2, null);
        assertThat(extractCurfewIds(currentOffenderCurfews(List.of(curfew)))).containsOnly(1L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingNullAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
        final var curfews = List.of(
                offenderCurfew(1, 1, null),
                offenderCurfew(2, 1, null)
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(2L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingSameAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
        final var curfews = List.of(
                offenderCurfew(2, 1, "2018-05-01"),
                offenderCurfew(1, 1, "2018-05-01")
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(2L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingDifferentAssessmentDatesWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
        final var curfews = List.of(
                offenderCurfew(1, 1, "2018-05-02"),
                offenderCurfew(2, 1, "2018-05-01")
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(1L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingDifferentWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
        final var curfews = List.of(
                offenderCurfew(2, 1, "2018-05-01"),
                offenderCurfew(1, 1, null)
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(1L);
    }


    @Test
    public void givenCurfewsForSeveralOffenderBookIdsWhenFilteredForCurrentCurfewTheHighestCurfewForEachOffenderBookIdIsRetained() {
        final var curfews = List.of(
                offenderCurfew(1, 1, null),
                offenderCurfew(2, 1, null),

                offenderCurfew(3, 2, "2018-01-01"),
                offenderCurfew(4, 2, null),

                offenderCurfew(5, 3, "2018-01-01"),
                offenderCurfew(6, 3, "2018-01-01"),

                offenderCurfew(7, 4, "2018-01-01"),
                offenderCurfew(8, 4, "2018-01-02"),
                offenderCurfew(9, 4, "2018-01-01")

        );

        assertThat(currentOffenderCurfews(curfews).collect(toList()))
                .containsOnly(
                        offenderCurfew(2, 1, null),
                        offenderCurfew(4, 2, null),
                        offenderCurfew(6, 3, "2018-01-01"),
                        offenderCurfew(8, 4, "2018-01-02")
                );
    }

    @Test
    public void givenOffendersWhenFilteringForThoseWithoutApprovalStatusThenCorrectSubsetOfOffenderBookIdIsReturned() {
        assertThat(OffenderCurfewServiceImpl.offenderBookingIdsForNewHDCProcess(
                Stream.of(
                        offenderCurfewStatus(1, 1, null),
                        offenderCurfewStatus(2, 2, "REJECTED"),
                        offenderCurfewStatus(3, 3, "APPROVED"),
                        offenderCurfewStatus(4, 4, null)
                ), Optional.empty())).containsOnly(1L, 4L);
    }

    @Test
    public void filteringForThoseWithApprovalStatusAndMinimumChecksPassedDate() {
        assertThat(OffenderCurfewServiceImpl.offenderBookingIdsForNewHDCProcess(
                Stream.of(
                        offenderCurfewStatus(1, 1, null),
                        offenderCurfewStatus(2, 2, "REJECTED", LocalDate.of(2019, 4, 2)),
                        offenderCurfewStatus(3, 3, "APPROVED", LocalDate.of(2019, 4, 3)),
                        offenderCurfewStatus(4, 4, null),
                        offenderCurfewStatus(5, 5, "APPROVED") // N.B. it isn't possible for an offender to have an approvalStatus, but no assessmentDate
                ), Optional.of(LocalDate.of(2019,4,3)))).containsOnly(1L, 3L, 4L);
    }



    @Test
    public void givenNoOffenderCurfewsWithoutApprovalStatusAndAnEarliestDateForArdOrCrdThenOffenderSentencesAreFilteredCorrectly() {
        final var HDCED = LocalDate.of(9999, 1, 1);
        final var EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final var DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        final var filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.emptySet(), EARLIEST_DATE);

        assertThat(filter.test(offenderSentenceDetail(1L, null, null, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, EARLIEST_DATE, null))).isFalse();

        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, null, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, null, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, null, EARLIEST_DATE, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, null, DAY_BEFORE, HDCED))).isFalse();

        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, DAY_BEFORE, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, EARLIEST_DATE, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isFalse();
    }

    @Test
    public void givenOffenderCurfewsWithoutApprovalStatusThenOffenderSentencesAreFilteredCorrectly() {
        final var HDCED = LocalDate.of(9999, 1, 1);
        final var EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final var DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        final var filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.singleton(1L), EARLIEST_DATE);

        // no ard or crd
        assertThat(filter.test(offenderSentenceDetail(1L, null, null, HDCED))).isFalse();

        // no hdced
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, EARLIEST_DATE, null))).isFalse();

        // ard too early
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, null, HDCED))).isFalse();

        // crd too early
        assertThat(filter.test(offenderSentenceDetail(1L, null, DAY_BEFORE, HDCED))).isFalse();

        // both ard and crd too early
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isFalse();

        // ard on earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, null, HDCED))).isTrue();

        // crd on earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, null, EARLIEST_DATE, HDCED))).isTrue();

        // ard and crd on earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, EARLIEST_DATE, HDCED))).isTrue();

        // ard before, crd on earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, EARLIEST_DATE, HDCED))).isTrue();

        //  ard on earliest date, crd before earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, DAY_BEFORE, HDCED))).isTrue();

        // ard and crd after earliest date
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE.plusDays(1), EARLIEST_DATE.plusDays(1), HDCED))).isTrue();

    }

    @Test
    public void givenOffenderCurfewsWithoutApprovalStatusThenOverrideDatesAreFilteredCorrectly() {
        final var HDCED = LocalDate.of(9999, 1, 1);
        final var EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final var DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        final var filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.singleton(1L), EARLIEST_DATE);

        // no ard or crd
        assertThat(filter.test(offenderSentenceDetail(null, null, HDCED, null, null))).isFalse();

        // no hdced
        assertThat(filter.test(offenderSentenceDetail(EARLIEST_DATE, EARLIEST_DATE, null, null, null))).isFalse();

        // ard & ard override too early
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, null, HDCED, DAY_BEFORE, null))).isFalse();

        // crd and crd override too early
        assertThat(filter.test(offenderSentenceDetail(null, DAY_BEFORE, HDCED, null, DAY_BEFORE))).isFalse();

        // both ard and crdand overrides too early
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, DAY_BEFORE, HDCED, DAY_BEFORE, DAY_BEFORE))).isFalse();

        // ard override on earliest date
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, null, HDCED, EARLIEST_DATE, null))).isTrue();

        // crd override on earliest date
        assertThat(filter.test(offenderSentenceDetail(null, DAY_BEFORE, HDCED, null, EARLIEST_DATE))).isTrue();

        // overrides on earliest date
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, DAY_BEFORE, HDCED, EARLIEST_DATE, EARLIEST_DATE))).isTrue();

        // ard before, crd on earliest date
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, DAY_BEFORE, HDCED, DAY_BEFORE, EARLIEST_DATE))).isTrue();

        //  ard on earliest date, crd before earliest date
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, DAY_BEFORE, HDCED, EARLIEST_DATE, DAY_BEFORE))).isTrue();

        // ard and crd after earliest date
        assertThat(filter.test(offenderSentenceDetail(DAY_BEFORE, DAY_BEFORE, HDCED, EARLIEST_DATE.plusDays(1), EARLIEST_DATE.plusDays(1)))).isTrue();

    }

    @Test
    public void givenNoOffendersInAgencyThenNoResults() {
        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        assertThat(offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME, Optional.empty())).isEmpty();
    }

    @Test
    public void givenOffendersWhenEveryOffenderHasANOMISApprovalStatusThenResultsAreFilteredByClockDate() {

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));

        when(bookingService.getOffenderSentenceCalculationsForAgency(Set.of(AGENCY_ID)))
                .thenReturn(offenderSentenceCalculations());


        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                List.of(
                        offenderCurfewStatus(1, 1, "ANY"),
                        offenderCurfewStatus(1, 2, "ANY"),
                        offenderCurfewStatus(1, 3, "ANY"),
                        offenderCurfewStatus(1, 4, "ANY"),
                        offenderCurfewStatus(1, 5, "ANY")));

        final var eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME, Optional.empty());

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceCalc::getBookingId)
                .collect(toList())
        ).isEmpty();
    }

    @Test
    public void givenOffendersWhenNoOffenderHasANOMISApprovalStatusThenAllOffendersAreCandidates() {

        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                List.of(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(1, 2, null),
                        offenderCurfew(1, 3, null),
                        offenderCurfew(1, 4, null),
                        offenderCurfew(1, 5, null)));

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        when(bookingService.getOffenderSentenceCalculationsForAgency(Collections.singleton(AGENCY_ID))).thenReturn(offenderSentenceCalculations());

        final var eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME, Optional.empty());

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceCalc::getBookingId)
                .collect(Collectors.toList())
        ).containsExactly(3L, 4L);
    }

    @Test
    public void badApprovalStatus() {
        val rejectedStatus = "XXX";

        when(referenceDomainService.isReferenceCodeActive(eq("HDC_APPROVE"), anyString())).thenReturn(false);

        val badApprovalStatus = ApprovalStatus.builder().approvalStatus(rejectedStatus).build();

        assertThatThrownBy(() -> offenderCurfewService.setApprovalStatus(1L, badApprovalStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Approval status code 'XXX' is not a valid NOMIS value.");
    }

    @Test
    public void badRefusedReason() {
            val rejectedStatus = "REJECTED";
            val refusedReason = "XXX";

            when(referenceDomainService.isReferenceCodeActive("HDC_APPROVE", rejectedStatus)).thenReturn(true);
            when(referenceDomainService.isReferenceCodeActive(eq("HDC_REJ_RSN"), anyString())).thenReturn(false);

        val badApprovalStatus = ApprovalStatus.builder().approvalStatus(rejectedStatus).refusedReason(refusedReason).build();

        assertThatThrownBy(() -> offenderCurfewService.setApprovalStatus(1L, badApprovalStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refused reason code 'XXX' is not a valid NOMIS value.");
    }

    private List<OffenderSentenceCalculation> offenderSentenceCalculations() {

        return List.of(
                offenderSentenceDetail(1L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 2), null, HDCED),
                offenderSentenceDetail(2L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 1), null, HDCED),
                offenderSentenceDetail(3L, TODAY.plusDays(CUTOFF_DAYS_OFFSET), null, HDCED),
                offenderSentenceDetail(4L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 1), null, HDCED),
                offenderSentenceDetail(5L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 2), null, null));
    }

    private OffenderSentenceCalculation offenderSentenceDetail(
            final LocalDate automaticReleaseDate,
            final LocalDate conditionalReleaseDate,
            final LocalDate homeDetentionCurfewEligibilityDate,
            final LocalDate automaticReleaseOverrideDate,
            final LocalDate conditionalReleaseOverrideDate) {

        return OffenderSentenceCalculation.builder()
                .bookingId(1L)
                .automaticReleaseDate(automaticReleaseOverrideDate != null ? automaticReleaseOverrideDate : automaticReleaseDate)
                .conditionalReleaseDate(conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : conditionalReleaseDate)
                .homeDetCurfEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();

    }
    private OffenderSentenceCalculation offenderSentenceDetail(
            final Long bookingId,
            final LocalDate automaticReleaseDate,
            final LocalDate conditionalReleaseDate,
            final LocalDate homeDetentionCurfewEligibilityDate) {

        return OffenderSentenceCalculation.builder()
                .bookingId(bookingId)
                .automaticReleaseDate(automaticReleaseDate)
                .conditionalReleaseDate(conditionalReleaseDate)
                .homeDetCurfEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();
    }

    private static OffenderCurfew offenderCurfew(final long offenderCurfewId, final long offenderBookId, final String assessmentDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
                .build();
    }

    private static OffenderCurfew offenderCurfewStatus(final long offenderCurfewId, final long offenderBookId, final String approvalStatus) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .approvalStatus(approvalStatus)
                .build();
    }

    private static OffenderCurfew offenderCurfewStatus(final long offenderCurfewId, final long offenderBookId, final String approvalStatus, final LocalDate assessmentDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .approvalStatus(approvalStatus)
                .assessmentDate(assessmentDate)
                .build();
    }

    private static LocalDate toLocalDate(final String string) {
        if (string == null) return null;
        return LocalDate.parse(string);
    }

    private static List<Long> extractCurfewIds(final Stream<OffenderCurfew> curfews) {
        return curfews.map(OffenderCurfew::getOffenderCurfewId).collect(toList());
    }

    private static List<Agency> agencyIdsToAgencies(final String... agencyIds) {
        return Arrays
                .stream(agencyIds)
                .map(id -> Agency
                        .builder()
                        .agencyId(id)
                        .build())
                .collect(toList());
    }
}
