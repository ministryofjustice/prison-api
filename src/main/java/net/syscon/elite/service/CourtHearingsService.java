package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
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

    public CourtHearingsService(final OffenderBookingRepository offenderBookingRepository,
                                final CourtEventRepository courtEventRepository) {
        this.offenderBookingRepository = offenderBookingRepository;
        this.courtEventRepository = courtEventRepository;
    }

    @Transactional
    public CourtHearing scheduleHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        // TODO throw entity not found exception
        var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow();

        // TODO throw entity not found exception
        var courtCase = offenderBooking.getCourtCaseBy(hearing.getCourtCaseId()).orElseThrow();

        CourtEvent courtEvent = CourtEvent.builder()
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
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
