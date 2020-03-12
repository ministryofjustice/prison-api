package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.core.HasWriteScope;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.ReferenceCodeRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class CourtHearingsService {

    private final OffenderBookingRepository offenderBookingRepository;

    private final CourtEventRepository courtEventRepository;

    private final AgencyLocationRepository agencyLocationRepository;

    private final ReferenceCodeRepository<EventType> eventTypeRepository;

    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;

    private final Clock clock;

    public CourtHearingsService(final OffenderBookingRepository offenderBookingRepository,
                                final CourtEventRepository courtEventRepository,
                                final AgencyLocationRepository agencyLocationRepository,
                                final ReferenceCodeRepository<EventType> eventTypeRepository,
                                final ReferenceCodeRepository<EventStatus> eventStatusRepository,
                                final Clock clock) {
        this.offenderBookingRepository = offenderBookingRepository;
        this.courtEventRepository = courtEventRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.eventStatusRepository = eventStatusRepository;
        this.clock = clock;
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public CourtHearing scheduleHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        checkHearingIsInFuture(hearing.getCourtHearingDateTime());

        final var offenderBooking = getActiveOffenderBookingFor(bookingId);

        final var courtCase = getActiveCourtCaseFor(hearing.getCourtCaseId(), offenderBooking);

        checkPrisonLocationSameAsOffenderBooking(hearing.getFromPrisonLocation(), offenderBooking);

        CourtEvent courtEvent = CourtEvent.builder()
                .courtLocation(getActiveCourtFor(hearing.getToCourtLocation()))
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
                .directionCode("OUT")
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
                .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED).orElseThrow())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(courtCase)
                .startTime(hearing.getCourtHearingDateTime())
                .commentText(hearing.getComments())
                .build();

        final var courtHearing = toCourtHearing(courtEventRepository.save(courtEvent));

        log.debug("created court hearing with id '{} for court case id {} and booking id {}", courtHearing.getId(), courtCase.getId(), offenderBooking.getBookingId());

        return courtHearing;
    }

    private void checkHearingIsInFuture(final LocalDateTime courtHearingDateTime) {
        checkArgument(courtHearingDateTime.isAfter(LocalDateTime.now(clock)), "Court hearing must be in the future.");
    }

    private OffenderBooking getActiveOffenderBookingFor(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        checkArgument(offenderBooking.isActive(),"Offender booking with id %s is not active.", bookingId);

        return offenderBooking;
    }

    private OffenderCourtCase getActiveCourtCaseFor(final Long caseId, final OffenderBooking offenderBooking) {
        final var courtCase = offenderBooking.getCourtCaseBy(caseId).orElseThrow(EntityNotFoundException.withId(caseId));

        checkArgument(courtCase.isActive(), "Court case with id %s is not active.", caseId);

        return courtCase;
    }

    private void checkPrisonLocationSameAsOffenderBooking(final String prisonLocation, final OffenderBooking booking) {
        final var agency = agencyLocationRepository.findById(prisonLocation).orElseThrow(EntityNotFoundException.withId(prisonLocation));

        checkArgument(booking.getLocation().equals(agency),"Prison location does not match the bookings location.");
    }

    private AgencyLocation getActiveCourtFor(final String courtLocation) {
        final var agency = agencyLocationRepository.findById(courtLocation).orElseThrow(EntityNotFoundException.withId(courtLocation));

        checkArgument(agency.getType().equalsIgnoreCase("CRT"),"Supplied court location wih id %s is not a valid court location.", courtLocation);
        checkArgument(agency.getActiveFlag().isActive(), "Supplied court location wih id %s is not active.", courtLocation);

        return agency;
    }

    private CourtHearing toCourtHearing(final CourtEvent event) {
        return CourtHearing.builder()
                .id(event.getId())
                .location(AgencyTransformer.transform(event.getCourtLocation()))
                .date(event.getEventDate())
                .time(event.getStartTime().toLocalTime())
                .build();
    }
}
