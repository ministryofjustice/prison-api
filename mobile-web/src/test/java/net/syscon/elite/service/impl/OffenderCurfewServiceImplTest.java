package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderCurfewService;
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
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

public class OffenderCurfewServiceImplTest {

    private static final String AGENCY_ID = "LEI";
    private static final String USERNAME = "ERIC";
    private static final int CUTOFF_DAYS_OFFSET = 28;

    private static final String DUMMY_AGENCY_QUERY = "dummyQuery";

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

    @Before
    public void configureService() {
        offenderCurfewService = new OffenderCurfewServiceImpl(offenderCurfewRepository, bookingService, clock);
    }

    /**
     * Demonstrates that 'clock' and TODAY are aligned...
     */
    @Test
    public void dayRepresentedByFixedClockAndTODAY_shouldBeEquivalent() {
        assertThat(LocalDate.now(clock)).isEqualTo(TODAY);
    }

    @Test
    public void givenNoOffendersInAgency_thenNoResults() {

        assertThat(offenderCurfewService.getHomeDetentionCurfewCandidates(AGENCY_ID, USERNAME)).isEmpty();
    }

    @Test
    public void givenOffenders_whenEveryOffenderHasANOMISApprovalStatus_thenOnlyCutoffDatesFilterResults() {

        when(bookingService.getOffenderSentencesSummary(AGENCY_ID, USERNAME, Collections.emptyList() ))
                .thenReturn(offenderSentenceDetails());

        when(bookingService.buildAgencyQuery(AGENCY_ID, USERNAME)).thenReturn(DUMMY_AGENCY_QUERY);

        when(offenderCurfewRepository.offendersWithoutCurfewApprovalStatus(DUMMY_AGENCY_QUERY)).thenReturn(Collections.emptyList());

        final List<OffenderSentenceDetail> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(AGENCY_ID, USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceDetail::getBookingId)
                .collect(Collectors.toList())
        ).containsOnly(3L, 4L, 13L, 14L);

    }

    @Test
    public void givenOffenders_whenNoOffenderHasANOMISApprovalStatus_thenAllOffendersAreCandidates() {

        when(bookingService.getOffenderSentencesSummary(AGENCY_ID, USERNAME, Collections.emptyList() ))
                .thenReturn(offenderSentenceDetails());

        when(bookingService.buildAgencyQuery(AGENCY_ID, USERNAME)).thenReturn(DUMMY_AGENCY_QUERY);

        when(offenderCurfewRepository.offendersWithoutCurfewApprovalStatus(DUMMY_AGENCY_QUERY))
                .thenReturn(LongStream.rangeClosed(1L, 25L).boxed().collect(Collectors.toList()));

        final List<OffenderSentenceDetail> eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(AGENCY_ID, USERNAME);

        assertThat(eligibleOffenders
                .stream()
                .map(OffenderSentenceDetail::getBookingId)
                .collect(Collectors.toList())
        ).containsOnly(
                 1L,  2L,  3L,  4L,
                     12L, 13L, 14L,
                25L);

    }

    private List<OffenderSentenceDetail> offenderSentenceDetails() {

        return Arrays.asList(
                offenderSentenceDetail(1L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 2), null, HDCED),
                offenderSentenceDetail(2L, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 1), null, HDCED),
                offenderSentenceDetail(3L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 0), null, HDCED),
                offenderSentenceDetail(4L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 1), null, HDCED),
                offenderSentenceDetail(5L, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 2), null, null),

                offenderSentenceDetail(11L, null, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 2), null),
                offenderSentenceDetail(12L, null, TODAY.plusDays(CUTOFF_DAYS_OFFSET - 1), HDCED),
                offenderSentenceDetail(13L, null, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 0), HDCED),
                offenderSentenceDetail(14L, null, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 1), HDCED),
                offenderSentenceDetail(15L, null, TODAY.plusDays(CUTOFF_DAYS_OFFSET + 2), null),

                offenderSentenceDetail(25L, null, null, HDCED)
        );
    }

    private OffenderSentenceDetail offenderSentenceDetail(
            Long bookingId,
            LocalDate automaticReleaseDate,
            LocalDate cofirmedReleaseDate,
            LocalDate homeDetentionCurfewEligibilityDate) {
        SentenceDetail detail = SentenceDetail
                .builder()
                .automaticReleaseDate(automaticReleaseDate)
                .confirmedReleaseDate(cofirmedReleaseDate)
                .homeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate)
                .build();

        return OffenderSentenceDetail
                .builder()
                .bookingId(bookingId)
                .sentenceDetail(detail)
                .build();
    }

}
