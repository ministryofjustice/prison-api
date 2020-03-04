package net.syscon.elite.service;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtHearingsServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final Long COURT_EVENT_ID = 99L;

    private static final PrisonToCourtHearing COURT_HEARING = PrisonToCourtHearing.builder()
            .courtCaseId(-2L)
            .fromPrisonLocation("PRISON")
            .toCourtLocation("COURT")
            .courtHearingDateTime(LocalDateTime.now())
            .build();

    private static final AgencyLocation COURT_LOCATION = AgencyLocation.builder()
            .activeFlag(ActiveFlag.Y)
            .description("Agency Description")
            .id("AGENCY_ID")
            .type("AGENCY_TYPE")
            .build();

    private static final CourtEvent.CourtEventBuilder COURT_EVENT_BUILDER = CourtEvent.builder()
            .courtLocation(COURT_LOCATION)
            .eventDate(LocalDate.EPOCH)
            .startTime(LocalDate.EPOCH.atStartOfDay());

    private static final CourtEvent UN_PERSISTED_COURT_EVENT = COURT_EVENT_BUILDER.build();

    private static final CourtEvent PERSISTED_COURT_EVENT = COURT_EVENT_BUILDER.id(COURT_EVENT_ID).build();

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private CourtEventRepository courtEventRepository;

    @Mock
    private OffenderBooking offenderBooking;

    @Mock
    private OffenderCourtCase offenderCourtCase;

    private CourtHearingsService courtHearingsService;

    @BeforeEach
    void setup() {
        courtHearingsService = new CourtHearingsService(offenderBookingRepository, courtEventRepository);
    }

    @Test
    void scheduling_of_court_hearing() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(offenderBooking));

        when(offenderBooking.getCourtCaseBy(COURT_HEARING.getCourtCaseId())).thenReturn(Optional.of(offenderCourtCase));

        when(courtEventRepository.save(UN_PERSISTED_COURT_EVENT)).thenReturn(PERSISTED_COURT_EVENT);

        CourtHearing courtHearing = courtHearingsService.scheduleHearing(OFFENDER_BOOKING_ID, COURT_HEARING);

        assertThat(courtHearing).isEqualTo(CourtHearing.builder()
                .id(COURT_EVENT_ID)
                .date(LocalDate.EPOCH)
                .location(AgencyTransformer.transform(COURT_LOCATION))
                .time(LocalDate.EPOCH.atStartOfDay().toLocalTime())
                .build());
    }

    // TODO add test for non-matching booking

    // TODO add test for non-matching court case
}
