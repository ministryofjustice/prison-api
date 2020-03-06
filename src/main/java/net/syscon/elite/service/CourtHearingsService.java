package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.ReferenceCodeRepository;
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
        // TODO throw entity not found exception
        var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow();

        // TODO throw entity not found exception (check is active)
        var courtCase = offenderBooking.getCourtCaseBy(hearing.getCourtCaseId()).orElseThrow();

        // TODO throw entity not found exception (check is prison?)
        var locationOfPrison = agencyLocationRepository.findById(hearing.getFromPrisonLocation()).orElseThrow();

        // TODO throw entity not found exception (check is court)
        var locationOfCourt = agencyLocationRepository.findById(hearing.getToCourtLocation()).orElseThrow();

        // TODO sort out reference codes!!!
        CourtEvent courtEvent = CourtEvent.builder()
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
                .courtLocation(locationOfCourt)
                .directionCode("OUT")
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
                .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED).orElseThrow())
                .offenderCourtCase(courtCase)
                .offenderBooking(offenderBooking)
                .startTime(hearing.getCourtHearingDateTime())
                .build();

        return toCourtHearing(courtEventRepository.save(courtEvent));
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
