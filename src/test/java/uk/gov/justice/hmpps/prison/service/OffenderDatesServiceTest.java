package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CalcReasonType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ReferenceCodeRepository<CalcReasonType> calcReasonTypeReferenceCodeRepository;
    @Mock
    private TelemetryClient telemetryClient;

    private final Clock clock  = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private OffenderDatesService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderDatesService(offenderBookingRepository, staffUserAccountRepository, calcReasonTypeReferenceCodeRepository, telemetryClient, clock);
    }

    @Test
    void updateOffenderDates_happy_path() {
        // Given
        final Long bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        when(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("UPDATE")))
            .thenReturn(Optional.of(new CalcReasonType("UPDATE", "Modify Sentence")));
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
            .calcReasonType(new CalcReasonType("UPDATE", "Modify Sentence"))
            .calculationDate(calculationDateTime)
            .comments("CRD calculation ID: " + calculationUuid)
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
            .effectiveSentenceEndDate(keyDates.getEffectiveSentenceEndDate())
            .effectiveSentenceLength(keyDates.getSentenceLength())
            .judiciallyImposedSentenceLength(keyDates.getSentenceLength())
            .build();

        assertThat(offenderBooking.getSentenceCalculations()).containsOnly(
            expected
        );
        assertEquals(Optional.of(expected), offenderBooking.getLatestCalculation());
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
        when(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("UPDATE")))
            .thenReturn(Optional.of(new CalcReasonType("UPDATE", "Modify Sentence")));
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
        assertEquals(defaultedCalculationDate, latestCalculation.getCalculationDate());
        assertEquals(defaultedCalculationDate, latestCalculation.getRecordedDateTime());
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

    // this shouldn't happen because UPDATE record exists in the NOMIS DB see the following SQL:
    //    SELECT * FROM reference_codes WHERE DOMAIN = 'CALC_REASON' AND CODE = 'UPDATE')
    // but thought was good to cover the case
    @Test
    void updateOffenderDates_exception_for_unknown_calc_reason() {
        // Given
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        final var payload = RequestToUpdateOffenderDates.builder()
            .keyDates(createOffenderKeyDates())
            .submissionUser("staff")
            .build();
        when(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("UPDATE")))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateOffenderKeyDates(bookingId, payload))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Resource with id [UPDATE] not found.");
    }

    @Test
    void updateOffenderDates_exception_for_unknown_staff() {
        // Given
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        when(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("UPDATE")))
            .thenReturn(Optional.of(new CalcReasonType("UPDATE", "Modify Sentence")));
        final var staff = "staff";
        when(staffUserAccountRepository.findById(staff)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateOffenderKeyDates(bookingId, RequestToUpdateOffenderDates.builder().submissionUser(staff).build()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Resource with id [staff] not found.");
    }

    public static OffenderKeyDates createOffenderKeyDates() {
        return createOffenderKeyDates(
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, NOV_11_2021,
            NOV_11_2021, NOV_11_2021, "11/00/00");
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
            .effectiveSentenceEndDate(effectiveSentenceEndDate)
            .sentenceLength(sentenceLength)
            .build();
    }
}
