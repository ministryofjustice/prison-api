package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.LatestTusedData;
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.repository.SentenceCalculationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderDatesServiceTest {

    private static final LocalDate NOV_11_2021 = LocalDate.of(2021, 11, 8);
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private SentenceCalculationRepository sentenceCalculationRepository;
    @Mock
    private TelemetryClient telemetryClient;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private OffenderDatesService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderDatesService(sentenceCalculationRepository, offenderBookingRepository, staffUserAccountRepository, telemetryClient, clock);
    }

    @Test
    void updateOffenderDates_happy_path() {
        // Given
        final Long bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var submissionUser = "staff";
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        final var staffUserAccount = StaffUserAccount.builder().username(submissionUser).staff(staff).build();
        when(staffUserAccountRepository.findById(submissionUser)).thenReturn(Optional.of(staffUserAccount));
        final var calculationUuid = UUID.randomUUID();
        final var calculationDateTime = LocalDateTime.of(2021, 11, 17, 11, 0);
        final var payload = RequestToUpdateOffenderDates.builder()
            .keyDates(createOffenderKeyDates())
            .submissionUser(submissionUser)
            .calculationDateTime(calculationDateTime)
            .calculationUuid(calculationUuid)
            .build();
        final var keyDates = payload.getKeyDates();

        // When
        service.updateOffenderKeyDates(bookingId, payload);

        // Then
        final var expected = SentenceCalculation.builder()
            .offenderBooking(offenderBooking)
            .reasonCode("UPDATE")
            .calculationDate(calculationDateTime)
            .comments("The information shown was calculated using the Calculate Release Dates service. The calculation ID is: " + calculationUuid)
            .staff(staff)
            .recordedDateTime(calculationDateTime)
            .recordedUser(staffUserAccount)
            .hdcedCalculatedDate(keyDates.getHomeDetentionCurfewEligibilityDate())
            .etdCalculatedDate(keyDates.getEarlyTermDate())
            .mtdCalculatedDate(keyDates.getMidTermDate())
            .ltdCalculatedDate(keyDates.getLateTermDate())
            .dprrdCalculatedDate(keyDates.getDtoPostRecallReleaseDate())
            .ardCalculatedDate(keyDates.getAutomaticReleaseDate())
            .crdCalculatedDate(keyDates.getConditionalReleaseDate())
            .pedCalculatedDate(keyDates.getParoleEligibilityDate())
            .npdCalculatedDate(keyDates.getNonParoleDate())
            .ledCalculatedDate(keyDates.getLicenceExpiryDate())
            .prrdCalculatedDate(keyDates.getPostRecallReleaseDate())
            .sedCalculatedDate(keyDates.getSentenceExpiryDate())
            .tusedCalculatedDate(keyDates.getTopupSupervisionExpiryDate())
            .ersedOverridedDate(keyDates.getEarlyRemovalSchemeEligibilityDate())
            .effectiveSentenceEndDate(keyDates.getEffectiveSentenceEndDate())
            .effectiveSentenceLength(keyDates.getSentenceLength())
            .judiciallyImposedSentenceLength(keyDates.getSentenceLength())
            .build();

        assertThat(offenderBooking.getSentenceCalculations()).containsOnly(
            expected
        );
        assertThat(offenderBooking.getLatestCalculation()).isEqualTo(Optional.of(expected));
        verify(telemetryClient).trackEvent("OffenderKeyDatesUpdated",
            ImmutableMap.of(
                "bookingId", bookingId.toString(),
                "calculationUuid", calculationUuid.toString(),
                "submissionUser", submissionUser
            ), null);
    }

    @Test
    void updateOffenderDates_happy_path_default_calculation_date() {
        // Given
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        final var staffUserAccount = StaffUserAccount.builder().username("staff").staff(staff).build();
        when(staffUserAccountRepository.findById("staff")).thenReturn(Optional.of(staffUserAccount));
        final var calculationUuid = UUID.randomUUID();
        final var payload = RequestToUpdateOffenderDates.builder()
            .keyDates(createOffenderKeyDates())
            .submissionUser("staff")
            .calculationUuid(calculationUuid)
            .build();

        // When
        service.updateOffenderKeyDates(bookingId, payload);

        // Then
        final var defaultedCalculationDate = LocalDateTime.now(clock);
        final var latestCalculation = offenderBooking.getLatestCalculation().get();
        assertThat(latestCalculation.getCalculationDate()).isEqualTo(defaultedCalculationDate);
        assertThat(latestCalculation.getRecordedDateTime()).isEqualTo(defaultedCalculationDate);
    }

    @Test
    void updateOffendDates_no_dates_to_store() {
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        final var staffUserAccount = StaffUserAccount.builder().username("staff").staff(staff).build();
        when(staffUserAccountRepository.findById("staff")).thenReturn(Optional.of(staffUserAccount));
        final var calculationUuid = UUID.randomUUID();
        final var payload = RequestToUpdateOffenderDates.builder()
            .keyDates(new OffenderKeyDates())
            .noDates(true)
            .submissionUser("staff")
            .calculationUuid(calculationUuid)
            .build();

        // When
        service.updateOffenderKeyDates(bookingId, payload);

        // Then
        final var latestCalculation = offenderBooking.getLatestCalculation().get();
        assertThat(latestCalculation).hasAllNullFieldsOrPropertiesExcept(
            "offenderBooking",
            "hdcEligible",
            "calculationDate",
            "comments",
            "staff",
            "reasonCode",
            "recordedDateTime",
            "recordedUser",
            "effectiveSentenceLength");
    }

    @Test
    void updateOffenderDates_exception_for_unknown_booking() {
        // Given
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateOffenderKeyDates(-1L, RequestToUpdateOffenderDates.builder().build()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Resource with id [-1] not found.");
    }

    @Test
    void updateOffenderDates_exception_for_unknown_staff() {
        // Given
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var staff = "staff";
        when(staffUserAccountRepository.findById(staff)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateOffenderKeyDates(bookingId, RequestToUpdateOffenderDates.builder().submissionUser(staff).build()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Resource with id [staff] not found.");
    }

    @Test
    void getOffenderKeyDates_all_data_by_offender_sent_calc_id() {
        // Given
        final var offenderSentCalcId = 1L;

        final var sentenceCalculation =
            SentenceCalculation.builder()
                .id(1L)
                .hdcedCalculatedDate(LocalDate.of(2021, 11, 1))
                .etdCalculatedDate(LocalDate.of(2021, 11, 2))
                .mtdCalculatedDate(LocalDate.of(2021, 11, 3))
                .ltdCalculatedDate(LocalDate.of(2021, 11, 4))
                .dprrdCalculatedDate(LocalDate.of(2021, 11, 5))
                .ardCalculatedDate(LocalDate.of(2021, 11, 6))
                .crdCalculatedDate(LocalDate.of(2021, 11, 7))
                .pedCalculatedDate(LocalDate.of(2021, 11, 8))
                .npdCalculatedDate(LocalDate.of(2021, 11, 9))
                .ledCalculatedDate(LocalDate.of(2021, 11, 10))
                .prrdCalculatedDate(LocalDate.of(2021, 11, 11))
                .sedCalculatedDate(LocalDate.of(2021, 11, 12))
                .tusedCalculatedDate(LocalDate.of(2021, 11, 13))
                .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
                .effectiveSentenceLength("11/00/11")
                .ersedOverridedDate(LocalDate.of(2021, 11, 15))
                .hdcadOverridedDate(LocalDate.of(2021, 11, 16))
                .tariffOverridedDate(LocalDate.of(2021, 11, 17))
                .tersedOverridedDate(LocalDate.of(2021, 11, 18))
                .apdOverridedDate(LocalDate.of(2021, 11, 19))
                .rotlOverridedDate(LocalDate.of(2021, 11, 20))
                .judiciallyImposedSentenceLength("11/00/00")
                .comments("Comments")
                .reasonCode("NEW")
                .calculationDate(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
                .build();
        when(sentenceCalculationRepository.findById(offenderSentCalcId)).thenReturn(Optional.of(sentenceCalculation));

        // When
        final var result = service.getOffenderKeyDatesByOffenderSentCalcId(offenderSentCalcId);

        // Then
        assertOffenderCalculatedKeyDates(result);
    }

    @Test
    void getOffenderKeyDates_all_data_available() {
        // Given
        final var bookingId = 1L;

        final var offenderBooking = OffenderBooking.builder()
            .bookingId(bookingId)
            .sentenceCalculations(List.of(
                SentenceCalculation.builder()
                    .id(1L)
                    .hdcedCalculatedDate(LocalDate.of(2021, 11, 1))
                    .etdCalculatedDate(LocalDate.of(2021, 11, 2))
                    .mtdCalculatedDate(LocalDate.of(2021, 11, 3))
                    .ltdCalculatedDate(LocalDate.of(2021, 11, 4))
                    .dprrdCalculatedDate(LocalDate.of(2021, 11, 5))
                    .ardCalculatedDate(LocalDate.of(2021, 11, 6))
                    .crdCalculatedDate(LocalDate.of(2021, 11, 7))
                    .pedCalculatedDate(LocalDate.of(2021, 11, 8))
                    .npdCalculatedDate(LocalDate.of(2021, 11, 9))
                    .ledCalculatedDate(LocalDate.of(2021, 11, 10))
                    .prrdCalculatedDate(LocalDate.of(2021, 11, 11))
                    .sedCalculatedDate(LocalDate.of(2021, 11, 12))
                    .tusedCalculatedDate(LocalDate.of(2021, 11, 13))
                    .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
                    .effectiveSentenceLength("11/00/11")
                    .ersedOverridedDate(LocalDate.of(2021, 11, 15))
                    .hdcadOverridedDate(LocalDate.of(2021, 11, 16))
                    .tariffOverridedDate(LocalDate.of(2021, 11, 17))
                    .tersedOverridedDate(LocalDate.of(2021, 11, 18))
                    .apdOverridedDate(LocalDate.of(2021, 11, 19))
                    .rotlOverridedDate(LocalDate.of(2021, 11, 20))
                    .judiciallyImposedSentenceLength("11/00/00")
                    .comments("Comments")
                    .reasonCode("NEW")
                    .calculationDate(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
                    .build()
            ))
            .build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));

        // When
        final var result = service.getOffenderKeyDates(bookingId);

        // Then
        assertOffenderCalculatedKeyDates(result);
    }

    @Test
    void updateOffenderDates_with_passed_in_comment_and_reason() {
        // Given
        final Long bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var submissionUser = "staff";
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        final var staffUserAccount = StaffUserAccount.builder().username(submissionUser).staff(staff).build();
        when(staffUserAccountRepository.findById(submissionUser)).thenReturn(Optional.of(staffUserAccount));
        final var calculationUuid = UUID.randomUUID();
        final var calculationDateTime = LocalDateTime.of(2021, 11, 17, 11, 0);
        final var payload = RequestToUpdateOffenderDates.builder()
            .keyDates(createOffenderKeyDates())
            .submissionUser(submissionUser)
            .calculationDateTime(calculationDateTime)
            .calculationUuid(calculationUuid)
            .comment("Passed in comment should override default")
            .reason("TRANSFER")
            .build();
        final var keyDates = payload.getKeyDates();

        // When
        service.updateOffenderKeyDates(bookingId, payload);

        // Then
        final var expected = SentenceCalculation.builder()
            .offenderBooking(offenderBooking)
            .reasonCode("TRANSFER")
            .calculationDate(calculationDateTime)
            .comments("Passed in comment should override default")
            .staff(staff)
            .recordedDateTime(calculationDateTime)
            .recordedUser(staffUserAccount)
            .hdcedCalculatedDate(keyDates.getHomeDetentionCurfewEligibilityDate())
            .etdCalculatedDate(keyDates.getEarlyTermDate())
            .mtdCalculatedDate(keyDates.getMidTermDate())
            .ltdCalculatedDate(keyDates.getLateTermDate())
            .dprrdCalculatedDate(keyDates.getDtoPostRecallReleaseDate())
            .ardCalculatedDate(keyDates.getAutomaticReleaseDate())
            .crdCalculatedDate(keyDates.getConditionalReleaseDate())
            .pedCalculatedDate(keyDates.getParoleEligibilityDate())
            .npdCalculatedDate(keyDates.getNonParoleDate())
            .ledCalculatedDate(keyDates.getLicenceExpiryDate())
            .prrdCalculatedDate(keyDates.getPostRecallReleaseDate())
            .sedCalculatedDate(keyDates.getSentenceExpiryDate())
            .tusedCalculatedDate(keyDates.getTopupSupervisionExpiryDate())
            .ersedOverridedDate(keyDates.getEarlyRemovalSchemeEligibilityDate())
            .effectiveSentenceEndDate(keyDates.getEffectiveSentenceEndDate())
            .effectiveSentenceLength(keyDates.getSentenceLength())
            .judiciallyImposedSentenceLength(keyDates.getSentenceLength())
            .build();

        assertThat(offenderBooking.getSentenceCalculations()).containsOnly(expected);
        assertThat(offenderBooking.getLatestCalculation()).isEqualTo(Optional.of(expected));
    }

    private static void assertOffenderCalculatedKeyDates(OffenderCalculatedKeyDates result) {
        // Then
        assertThat(result).isEqualTo(OffenderCalculatedKeyDates.offenderCalculatedKeyDates()
            .homeDetentionCurfewEligibilityDate(LocalDate.of(2021, 11, 1))
            .earlyTermDate(LocalDate.of(2021, 11, 2))
            .midTermDate(LocalDate.of(2021, 11, 3))
            .lateTermDate(LocalDate.of(2021, 11, 4))
            .dtoPostRecallReleaseDate(LocalDate.of(2021, 11, 5))
            .automaticReleaseDate(LocalDate.of(2021, 11, 6))
            .conditionalReleaseDate(LocalDate.of(2021, 11, 7))
            .paroleEligibilityDate(LocalDate.of(2021, 11, 8))
            .nonParoleDate(LocalDate.of(2021, 11, 9))
            .licenceExpiryDate(LocalDate.of(2021, 11, 10))
            .postRecallReleaseDate(LocalDate.of(2021, 11, 11))
            .sentenceExpiryDate(LocalDate.of(2021, 11, 12))
            .topupSupervisionExpiryDate(LocalDate.of(2021, 11, 13))
            .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
            .sentenceLength("11/00/11")
            .earlyRemovalSchemeEligibilityDate(LocalDate.of(2021, 11, 15))
            .homeDetentionCurfewApprovedDate(LocalDate.of(2021, 11, 16))
            .tariffDate(LocalDate.of(2021, 11, 17))
            .tariffExpiredRemovalSchemeEligibilityDate(LocalDate.of(2021, 11, 18))
            .approvedParoleDate(LocalDate.of(2021, 11, 19))
            .releaseOnTemporaryLicenceDate(LocalDate.of(2021, 11, 20))
            .judiciallyImposedSentenceLength("11/00/00")
            .comment("Comments")
            .reasonCode("NEW")
            .calculatedAt(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
            .build());
    }

    @Test
    void findLatestTusedDataIsReturnedCorrectly() {
        LatestTusedData latestTusedData = new LatestTusedData(LocalDate.of(2023, 1, 3), null, null, "A1234AA");
        when(offenderBookingRepository.findLatestTusedDataFromNomsId(anyString())).thenReturn(Optional.of(latestTusedData));
        LatestTusedData returned = service.getLatestTusedDataFromNomsId("A1234AA");
        assertThat(returned.getOffenderNo()).isEqualTo("A1234AA");
        assertThat(returned.getLatestTused()).isEqualTo(LocalDate.of(2023, 1, 3));
    }

    @Test
    void findLatestTusedDataThrowsExpectedException() {
        when(offenderBookingRepository.findLatestTusedDataFromNomsId(anyString())).thenReturn(Optional.empty());
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> service.getLatestTusedDataFromNomsId("A1234AA"));
        assertThat(exception.getMessage()).isEqualTo("Resource with id [A1234AA] not found.");
    }

    public static OffenderKeyDates createOffenderKeyDates() {
        return createOffenderKeyDates(
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021, "11/00/00");
    }

    public static OffenderKeyDates createOffenderKeyDates(LocalDate homeDetentionCurfewEligibilityDate,
                                                          LocalDate earlyTermDate,
                                                          LocalDate midTermDate,
                                                          LocalDate lateTermDate,
                                                          LocalDate dtoPostRecallReleaseDate,
                                                          LocalDate automaticReleaseDate,
                                                          LocalDate conditionalReleaseDate,
                                                          LocalDate paroleEligibilityDate,
                                                          LocalDate nonParoleDate,
                                                          LocalDate licenceExpiryDate,
                                                          LocalDate postRecallReleaseDate,
                                                          LocalDate sentenceExpiryDate,
                                                          LocalDate topupSupervisionExpiryDate,
                                                          LocalDate earlyRemovalSchemeEligibilityDate,
                                                          LocalDate effectiveSentenceEndDate,
                                                          String sentenceLength) {
        return OffenderKeyDates.builder()
            .homeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate)
            .earlyTermDate(earlyTermDate)
            .midTermDate(midTermDate)
            .lateTermDate(lateTermDate)
            .dtoPostRecallReleaseDate(dtoPostRecallReleaseDate)
            .automaticReleaseDate(automaticReleaseDate)
            .conditionalReleaseDate(conditionalReleaseDate)
            .paroleEligibilityDate(paroleEligibilityDate)
            .nonParoleDate(nonParoleDate)
            .licenceExpiryDate(licenceExpiryDate)
            .postRecallReleaseDate(postRecallReleaseDate)
            .sentenceExpiryDate(sentenceExpiryDate)
            .topupSupervisionExpiryDate(topupSupervisionExpiryDate)
            .earlyRemovalSchemeEligibilityDate(earlyRemovalSchemeEligibilityDate)
            .effectiveSentenceEndDate(effectiveSentenceEndDate)
            .sentenceLength(sentenceLength)
            .build();
    }

}
