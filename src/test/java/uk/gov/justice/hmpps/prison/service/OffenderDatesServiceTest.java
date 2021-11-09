package uk.gov.justice.hmpps.prison.service;

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
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private final Clock clock  = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private OffenderDatesService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderDatesService(offenderBookingRepository, staffUserAccountRepository, calcReasonTypeReferenceCodeRepository, clock);
    }

    @Test
    void updateOffenderDates_happy_path() {
        // Given
        final var bookingId = 1L;
        final var offenderBooking = OffenderBooking.builder().bookingId(bookingId).build();
        when(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking));
        when(calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk("OVERRIDE")))
            .thenReturn(Optional.of(new CalcReasonType("OVERRIDE", "Override")));
        final var staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build();
        when(staffUserAccountRepository.findById("staff"))
            .thenReturn(Optional.of(StaffUserAccount.builder().username("staff").staff(staff).build()));
        final var payload = RequestToUpdateOffenderDates.builder()
                .keyDates(createOffenderKeyDates(NOV_11_2021, NOV_11_2021, NOV_11_2021))
                .submissionUser("staff")
                .build();

        // When
        service.updateOffenderKeyDates(bookingId, payload);

        // Then
        final var expected = SentenceCalculation.builder()
            .offenderBooking(offenderBooking)
            .calcReasonType(new CalcReasonType("OVERRIDE", "Override"))
            .calculationDate(LocalDate.now(clock))
            .comments("Calculated externally")
            .staff(staff)
            .crdCalculatedDate(payload.getKeyDates().getConditionalReleaseDate())
            .ledCalculatedDate(payload.getKeyDates().getLicenceExpiryDate())
            .sedCalculatedDate(payload.getKeyDates().getSentenceExpiryDate())
            .build();

        assertThat(offenderBooking.getSentenceCalculations()).containsOnly(
            expected
        );
        assertEquals(Optional.of(expected), offenderBooking.getLatestCalculation());
    }

    @Test
    void updateOffenderDates_errors_for_unknown_booking() {
        // Given
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.updateOffenderKeyDates(-1L, RequestToUpdateOffenderDates.builder().build()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Resource with id [-1] not found.");
    }

    public static OffenderKeyDates createOffenderKeyDates(LocalDate conditionalReleaseDate, LocalDate licenceExpiryDate, LocalDate sentenceExpiryDate) {
        return OffenderKeyDates.builder()
            .conditionalReleaseDate(conditionalReleaseDate)
            .licenceExpiryDate(licenceExpiryDate)
            .sentenceExpiryDate(sentenceExpiryDate)
            .build();
    }
}
