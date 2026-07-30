package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.util.Optional;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.COMPLETED;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class PrisonToPrisonMoveSchedulingService {

    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;

    private final OffenderIndividualScheduleRepository scheduleRepository;

    public PrisonToPrisonMoveSchedulingService(final ReferenceCodeRepository<EventStatus> eventStatusRepository,
                                               final OffenderIndividualScheduleRepository scheduleRepository) {
        this.eventStatusRepository = eventStatusRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Optional<OffenderIndividualSchedule> completeScheduledChildHearingEvent(@Nullable Long bookingId, long parentEventId) {
        return scheduleRepository.findOneByOffenderBookingBookingIdAndParentEventId(bookingId, parentEventId)
            .map(scheduleEvent -> {
                scheduleEvent.setEventStatus(eventStatusRepository.findById(COMPLETED).orElseThrow());
                return scheduleEvent;
            });
    }
}
