package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
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
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static java.lang.String.format;

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

    public CourtHearingsService(final OffenderBookingRepository offenderBookingRepository,
                                final CourtEventRepository courtEventRepository,
                                final AgencyLocationRepository agencyLocationRepository,
                                final ReferenceCodeRepository<EventType> eventTypeRepository,
                                final ReferenceCodeRepository<EventStatus> eventStatusRepository) {
        this.offenderBookingRepository = offenderBookingRepository;
        this.courtEventRepository = courtEventRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.eventStatusRepository = eventStatusRepository;
    }

    @Transactional
    public CourtHearing scheduleHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        final var offenderBooking = getActiveOffenderBookingFor(bookingId);

        final var courtCase = getActiveCourtCaseFor(hearing.getCourtCaseId(), offenderBooking);

        checkPrisonLocationSameAsOffenderBooking(hearing.getFromPrisonLocation(), offenderBooking);

        CourtEvent courtEvent = CourtEvent.builder()
                .courtLocation(getCourtFor(hearing.getToCourtLocation()))
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
                .directionCode("OUT")
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
                .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED).orElseThrow())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(courtCase)
                .startTime(hearing.getCourtHearingDateTime())
                .build();

        return toCourtHearing(courtEventRepository.save(courtEvent));
    }

    private OffenderBooking getActiveOffenderBookingFor(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        if (offenderBooking.isActive()) {
            return offenderBooking;
        }

        throw new IllegalArgumentException(format("Offender booking with id [%d] is not active.", bookingId));
    }

    private OffenderCourtCase getActiveCourtCaseFor(final Long caseId, final OffenderBooking offenderBooking) {
        final var courtCase = offenderBooking.getCourtCaseBy(caseId).orElseThrow(EntityNotFoundException.withId(caseId));

        if (courtCase.isActive()) {
            return courtCase;
        }

        throw new IllegalArgumentException(format("Court case with id [%d] is not active.", caseId));
    }

    private void checkPrisonLocationSameAsOffenderBooking(final String prisonLocation, final OffenderBooking booking) {
        var agency = agencyLocationRepository.findById(prisonLocation).orElseThrow(EntityNotFoundException.withId(prisonLocation));

        if (!booking.getLocation().equals(agency)) {
            throw new IllegalArgumentException("Prison location does not match the bookings location.");
        }
    }

    // TODO check if court is active.
    private AgencyLocation getCourtFor(final String courtLocation) {
        var agency = agencyLocationRepository.findById(courtLocation).orElseThrow(EntityNotFoundException.withId(courtLocation));

        if (agency.getType().equalsIgnoreCase("CRT")) {
            return agency;
        }

        throw new IllegalArgumentException(format("Supplied court location wih id %s is not a valid court location.", courtLocation));
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
