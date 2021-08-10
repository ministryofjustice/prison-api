package uk.gov.justice.hmpps.prison.service.curfews;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.BaseSentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseloadToAgencyMappingService;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.service.curfews.OffenderCurfewService.currentOffenderCurfews;

@ExtendWith(MockitoExtension.class)

public class OffenderCurfewServiceTest {

    private static final String AGENCY_ID = "LEI";
    private static final String USERNAME = "ERIC";
    private static final int CUTOFF_DAYS_OFFSET = 28;

    private static final LocalDate TODAY = LocalDate.of(2017, 6, 15);
    private static final LocalDate HDCED = LocalDate.of(2018, 12, 12);

    private OffenderCurfewService offenderCurfewService;

    @Mock
    private BookingService bookingService;

    @Mock
    private OffenderCurfewRepository offenderCurfewRepository;

    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    @BeforeEach
    public void configureService() {
        offenderCurfewService = new OffenderCurfewService(
                offenderCurfewRepository,
                caseloadToAgencyMappingService,
                bookingService,
                referenceDomainService);
    }

    @Test
    public void givenTwoOffenderCurfewWhenComparedThenOrderingIsCorrect() {

        assertThat(
                compare(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(2, 1, null)
                )).isEqualTo(-1);

        assertThat(
                compare(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(0);

        assertThat(
                compare(
                        offenderCurfew(2, 1, null),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(1);

        assertThat(
                compare(
                        offenderCurfew(1, 1, "2018-01-01"),
                        offenderCurfew(2, 1, "2018-01-01")
                )).isEqualTo(-1);

        assertThat(
                compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, "2018-01-01")
                )).isEqualTo(1);

        assertThat(
                compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, "2018-01-02")
                )).isEqualTo(-1);

        assertThat(
                compare(
                        offenderCurfew(2, 1, "2018-01-01"),
                        offenderCurfew(1, 1, null)
                )).isEqualTo(-1);
    }

    @Test
    public void oscByHdcedComparator() {
        final var d1 = LocalDate.of(2019, 1, 1);
        final var d2 = LocalDate.of(2019, 1, 2);

        assertOscComparison(offenderSentenceCalc(), offenderSentenceCalc(), 0);
        assertOscComparison(offenderSentenceCalc(), offenderSentenceCalc(d1), 1);
        assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(), -1);
        assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(d1), 0);
        assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(d2), -1);
        assertOscComparison(offenderSentenceCalc(d2), offenderSentenceCalc(d1), 1);
        assertOscComparison(offenderSentenceCalc(null), offenderSentenceCalc(), 0);
    }

    private static int compare(OffenderCurfew a, OffenderCurfew b) {
        return OffenderCurfewService.OFFENDER_CURFEW_COMPARATOR.compare(a, b);
    }

    private void assertOscComparison(OffenderSentenceCalc<? extends BaseSentenceCalcDates> a, OffenderSentenceCalc<? extends BaseSentenceCalcDates> b, int expected) {
        assertThat(OffenderCurfewService.OSC_BY_HDCED_COMPARATOR.compare(a, b)).isEqualTo(expected);
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
    public void givenNoOffendersInAgencyThenNoResults() {
        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        assertThat(offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME)).isEmpty();
    }

    @Test
    public void givenOffendersThenFilteredByHdcedPresent() {

        when(offenderCurfewRepository.offenderCurfews(Collections.singleton(AGENCY_ID))).thenReturn(
                List.of(
                        offenderCurfew(1, 1, null),
                        offenderCurfew(1, 2, null),
                        offenderCurfew(1, 3, null),
                        offenderCurfew(1, 4, null),
                        offenderCurfew(1, 5, null)));

        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME)).thenReturn(agencyIdsToAgencies(AGENCY_ID));
        when(bookingService.getOffenderSentenceCalculationsForAgency(Collections.singleton(AGENCY_ID))).thenReturn(offenderSentenceCalculations());

        final var eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceCalc::getBookingId)
                .collect(Collectors.toList())
        ).containsExactly(1L, 2L, 3L, 4L);
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

    private OffenderSentenceCalc<? extends BaseSentenceCalcDates> offenderSentenceCalc() {
        return OffenderSentenceCalc.builder().build();
    }

    private OffenderSentenceCalc<? extends BaseSentenceCalcDates> offenderSentenceCalc(LocalDate hdced) {

        return OffenderSentenceCalc
                .builder()
                .sentenceDetail(
                        BaseSentenceCalcDates
                                .builder()
                                .homeDetentionCurfewEligibilityDate(hdced)
                                .build())
                .build();
    }
}
