package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.OffenderSentenceCalc;
import net.syscon.elite.api.model.OffenderSentenceCalculation;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseloadToAgencyMappingService;
import net.syscon.elite.service.OffenderCurfewService;
import net.syscon.elite.service.ReferenceDomainService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static net.syscon.elite.service.impl.OffenderCurfewServiceImpl.currentOffenderCurfews;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        OffenderCurfew curfew = offenderCurfew(1, 2, null);
        assertThat(extractCurfewIds(currentOffenderCurfews(singleton(curfew)))).containsOnly(1L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingNullAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
        List<OffenderCurfew> curfews = asList(
                offenderCurfew(1, 1, null),
                offenderCurfew(2, 1, null)
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(2L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingSameAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
        List<OffenderCurfew> curfews = asList(
                offenderCurfew(2, 1, "2018-05-01"),
                offenderCurfew(1, 1, "2018-05-01")
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(2L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingDifferentAssessmentDatesWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
        List<OffenderCurfew> curfews = asList(
                offenderCurfew(1, 1, "2018-05-02"),
                offenderCurfew(2, 1, "2018-05-01")
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(1L);
    }

    @Test
    public void givenTwoCurfewsForOneOffenderBookIdHavingDifferentWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
        List<OffenderCurfew> curfews = asList(
                offenderCurfew(2, 1, "2018-05-01"),
                offenderCurfew(1, 1, null)
        );

        assertThat(extractCurfewIds(currentOffenderCurfews(curfews))).containsOnly(1L);
    }


    @Test
    public void givenCurfewsForSeveralOffenderBookIdsWhenFilteredForCurrentCurfewTheHighestCurfewForEachOffenderBookIdIsRetained() {
        List<OffenderCurfew> curfews = asList(
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
        assertThat(OffenderCurfewServiceImpl.offendersLackingCurfewApprovalStatus(
                Stream.of(
                        offenderCurfewStatus(1, 1, null),
                        offenderCurfewStatus(2, 2, "REJECTED"),
                        offenderCurfewStatus(3, 3, "APPROVED"),
                        offenderCurfewStatus(4, 4, null)
                ))).containsOnly(1L, 4L);
    }

    @Test
    public void givenNoOffenderCurfewsWithoutApprovalStatusAndAnEarliestDateForArdOrCrdThenOffenderSentencesAreFilteredCorrectly() {
        final LocalDate HDCED = LocalDate.of(9999, 1, 1);
        final LocalDate EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final LocalDate DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        Predicate<OffenderSentenceCalculation> filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.emptySet(), EARLIEST_DATE);

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
        final LocalDate HDCED = LocalDate.of(9999, 1, 1);
        final LocalDate EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final LocalDate DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        Predicate<OffenderSentenceCalculation> filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.singleton(1L), EARLIEST_DATE);

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
        final LocalDate HDCED = LocalDate.of(9999, 1, 1);
        final LocalDate EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final LocalDate DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        Predicate<OffenderSentenceCalculation> filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.singleton(1L), EARLIEST_DATE);

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
        assertThat(offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME)).isEmpty();
    }

    @Test
    public void givenOffendersWhenEveryOffenderHasANOMISApprovalStatusThenResultsAreFilteredByClockDate() {

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));

        when(bookingService.getOffenderSentenceCalculationsForAgency(Set.of(AGENCY_ID)))
                .thenReturn(offenderSentenceCalculations());


        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                Arrays.asList(
                        offenderCurfewStatus(1, 1, "ANY"),
                        offenderCurfewStatus(1, 2, "ANY"),
                        offenderCurfewStatus(1, 3, "ANY"),
                        offenderCurfewStatus(1, 4, "ANY"),
                        offenderCurfewStatus(1, 5, "ANY")));

        final List<OffenderSentenceCalc> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceCalc::getBookingId)
                .collect(toList())
        ).isEmpty();
    }

    @Test
    public void givenOffendersWhenNoOffenderHasANOMISApprovalStatusThenAllOffendersAreCandidates() {

        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                Arrays.asList(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(1, 2, null),
                        offenderCurfew(1, 3, null),
                        offenderCurfew(1, 4, null),
                        offenderCurfew(1, 5, null)));

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        when(bookingService.getOffenderSentenceCalculationsForAgency(Collections.singleton(AGENCY_ID))).thenReturn(offenderSentenceCalculations());

        final List<OffenderSentenceCalc> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceCalc::getBookingId)
                .collect(Collectors.toList())
        ).containsExactly(3L, 4L);
    }

    private List<OffenderSentenceCalculation> offenderSentenceCalculations() {

        return asList(
                offenderSentenceDetail(1L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 2), null, HDCED),
                offenderSentenceDetail(2L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 1), null, HDCED),
                offenderSentenceDetail(3L, TODAY.plusDays(CUTOFF_DAYS_OFFSET), null, HDCED),
                offenderSentenceDetail(4L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 1), null, HDCED),
                offenderSentenceDetail(5L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 2), null, null));
    }

    private OffenderSentenceCalculation offenderSentenceDetail(
            LocalDate automaticReleaseDate,
            LocalDate conditionalReleaseDate,
            LocalDate homeDetentionCurfewEligibilityDate,
            LocalDate automaticReleaseOverrideDate,
            LocalDate conditionalReleaseOverrideDate) {

        return OffenderSentenceCalculation.builder()
                .bookingId(1L)
                .automaticReleaseDate(automaticReleaseOverrideDate != null ? automaticReleaseOverrideDate : automaticReleaseDate)
                .conditionalReleaseDate(conditionalReleaseOverrideDate != null ? conditionalReleaseOverrideDate : conditionalReleaseDate)
                .homeDetCurfEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();

    }
    private OffenderSentenceCalculation offenderSentenceDetail(
            Long bookingId,
            LocalDate automaticReleaseDate,
            LocalDate conditionalReleaseDate,
            LocalDate homeDetentionCurfewEligibilityDate) {

        return OffenderSentenceCalculation.builder()
                .bookingId(bookingId)
                .automaticReleaseDate(automaticReleaseDate)
                .conditionalReleaseDate(conditionalReleaseDate)
                .homeDetCurfEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();
    }

    private static OffenderCurfew offenderCurfew(long offenderCurfewId, long offenderBookId, String assessmentDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
                .build();
    }

    private static OffenderCurfew offenderCurfewStatus(long offenderCurfewId, long offenderBookId, String approvalStatus) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .approvalStatus(approvalStatus)
                .build();
    }

    private static LocalDate toLocalDate(String string) {
        if (string == null) return null;
        return LocalDate.parse(string);
    }

    private static List<Long> extractCurfewIds(Stream<OffenderCurfew> curfews) {
        return curfews.map(OffenderCurfew::getOffenderCurfewId).collect(toList());
    }

    private static List<Agency> agencyIdsToAgencies(String... agencyIds) {
        return Arrays
                .stream(agencyIds)
                .map(id -> Agency
                        .builder()
                        .agencyId(id)
                        .build())
                .collect(toList());
    }
}
