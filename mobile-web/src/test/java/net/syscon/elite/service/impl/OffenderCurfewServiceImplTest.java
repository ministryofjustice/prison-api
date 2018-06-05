package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseloadToAgencyMappingService;
import net.syscon.elite.service.OffenderCurfewService;
import net.syscon.elite.service.support.OffenderCurfew;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Before
    public void configureService() {
        offenderCurfewService = new OffenderCurfewServiceImpl(offenderCurfewRepository, caseloadToAgencyMappingService, bookingService, clock);
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
                        offenderCurfew(1, 1,  null, null),
                        offenderCurfew(2, 2,  null, "REJECTED"),
                        offenderCurfew(3, 3,  null, "APPROVED"),
                        offenderCurfew(4, 4,  null, null)
                ))).containsOnly(1L, 4L);
    }

    @Test
    public void givenNoOffenderCurfewsWithoutApprovalStatusAndAnEarliestDateForArdOrCrdThenOffenderSentencesAreFilteredCorrectly() {
        final LocalDate HDCED = LocalDate.of(9999,1,1);
        final LocalDate EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final LocalDate DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        Predicate<OffenderSentenceDetail> filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.emptySet(), EARLIEST_DATE);

        assertThat(filter.test(offenderSentenceDetail(1L, null, null, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, EARLIEST_DATE, null))).isFalse();

        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, null, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, null, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, null, EARLIEST_DATE, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, null, DAY_BEFORE, HDCED))).isFalse();

        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, DAY_BEFORE, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isFalse();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, EARLIEST_DATE, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isFalse();
    }

    @Test
    public void givenOffenderCurfewsWithoutApprovalStatusThenOffenderSentencesAreFilteredCorrectly() {
        final LocalDate HDCED = LocalDate.of(9999,1,1);
        final LocalDate EARLIEST_DATE = LocalDate.of(2081, 1, 1);
        final LocalDate DAY_BEFORE = EARLIEST_DATE.minusDays(1);

        Predicate<OffenderSentenceDetail> filter = OffenderCurfewServiceImpl.offenderIsEligibleForHomeCurfew(Collections.singleton(1L), EARLIEST_DATE);

        assertThat(filter.test(offenderSentenceDetail(1L, null, null, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, EARLIEST_DATE, EARLIEST_DATE, null))).isFalse();

        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, null, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, null, DAY_BEFORE, HDCED))).isTrue();

        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isTrue();
        assertThat(filter.test(offenderSentenceDetail(1L, DAY_BEFORE, DAY_BEFORE, HDCED))).isTrue();
    }

    @Test
    public void givenNoOffendersInAgencyThenNoResults() {
        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        assertThat(offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME)).isEmpty();
    }

    @Test
    public void givenOffendersWhenEveryOffenderHasANOMISApprovalStatusThenResultsAreFilteredByClockDate() {

        when(bookingService.getOffenderSentencesSummary(null, USERNAME, Collections.emptyList() ))
                .thenReturn(offenderSentenceDetails());

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));

        when(offenderCurfewRepository.offenderCurfews(singleton(AGENCY_ID))).thenReturn(Collections.emptyList());

        final List<OffenderSentenceDetail> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceDetail::getBookingId)
                .collect(toList())
        ).containsExactly(3L, 4L);
    }


        @Test
    public void givenOffendersWhenNoOffenderHasANOMISApprovalStatusThenAllOffendersAreCandidates() {

        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                Arrays.asList(
                        offenderCurfew(1, 1,  null),
                        offenderCurfew(1, 2,  null),
                        offenderCurfew(1, 3,  null),
                        offenderCurfew(1, 4,  null),
                        offenderCurfew(1, 5,  null)));

            when(bookingService.getOffenderSentencesSummary(null, USERNAME, Collections.emptyList()))
                    .thenReturn(offenderSentenceDetails());

            when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));

            final List<OffenderSentenceDetail> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceDetail::getBookingId)
                .collect(Collectors.toList())
        ).containsExactly(1L,  2L,  3L,  4L);
    }

    private List<OffenderSentenceDetail> offenderSentenceDetails() {

        return asList(
                offenderSentenceDetail(1L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 2), null, HDCED),
                offenderSentenceDetail(2L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 1), null, HDCED),
                offenderSentenceDetail(3L, TODAY.plusDays(CUTOFF_DAYS_OFFSET    ), null, HDCED),
                offenderSentenceDetail(4L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 1), null, HDCED),
                offenderSentenceDetail(5L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 2), null, null));
    }

    private OffenderSentenceDetail offenderSentenceDetail(
            Long bookingId,
            LocalDate automaticReleaseDate,
            LocalDate conditionalReleaseDate,
            LocalDate homeDetentionCurfewEligibilityDate) {

        SentenceDetail detail = SentenceDetail
                .builder()
                .automaticReleaseDate(automaticReleaseDate)
                .conditionalReleaseDate(conditionalReleaseDate)
                .homeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();

        return OffenderSentenceDetail
                .builder()
                .bookingId(bookingId)
                .sentenceDetail(detail)
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

    private static OffenderCurfew offenderCurfew(long offenderCurfewId, long offenderBookId, String assessmentDate, String approvalStatus) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
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
