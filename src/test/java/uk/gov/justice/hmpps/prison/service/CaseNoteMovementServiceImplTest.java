package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerCaseNoteTypeAndSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSizeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseNoteMovementServiceImplTest {
    @Mock
    private CaseNoteRepository repository;

    @Mock
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;

    @Mock
    private BookingService bookingService;

    private CaseNoteService caseNoteService;

    @BeforeEach
    public void setUp() {
        caseNoteService = new CaseNoteService(repository, offenderCaseNoteRepository, new CaseNoteTransformer(staffUserAccountRepository, "yyyy/MM/dd HH:mm:ss"),
            bookingService, 10, offenderBookingRepository, staffUserAccountRepository, caseNoteTypeReferenceCodeRepository, caseNoteSubTypeReferenceCodeRepository);
    }

    @Test
    public void getCaseNoteUsageByBookingId() {
        final var usage = List.of(new PrisonerCaseNoteTypeAndSubType(-16L, "OBSERVE", "OBS_GEN", LocalDateTime.parse("2017-05-13T12:00")));
        when(offenderCaseNoteRepository.findCaseNoteTypesByBookingsAndDates(anyList(), anyString(), anyString(), any(), any())).thenReturn(usage);

        final var bookingIds = List.of(2L, 3L, 4L);
        assertThat(caseNoteService.getCaseNoteUsageByBookingId("TYPE", "SUBTYPE", bookingIds, null, null, 3)).isEqualTo(
            List.of(new CaseNoteUsageByBookingId(-16L, "OBSERVE", "OBS_GEN", 1L, LocalDateTime.parse("2017-05-13T12:00")))
        );

        final var tomorrow = LocalDate.now().plusDays(1);
        final var threeMonthsAgo = LocalDate.now().minusMonths(3);

        verify(offenderCaseNoteRepository).findCaseNoteTypesByBookingsAndDates(bookingIds, "TYPE", "SUBTYPE", threeMonthsAgo, tomorrow);
    }
}
