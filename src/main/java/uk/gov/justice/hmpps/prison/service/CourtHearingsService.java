package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.api.model.CourtHearings;
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.COURT;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class CourtHearingsService {

    private final OffenderBookingRepository offenderBookingRepository;

    private final CourtEventRepository courtEventRepository;

    private final AgencyLocationRepository agencyLocationRepository;

    private final ReferenceCodeRepository<MovementReason> eventTypeRepository;

    private final ReferenceCodeRepository<EventStatus> eventStatusRepository;

    private final Clock clock;

    public CourtHearingsService(final OffenderBookingRepository offenderBookingRepository,
                                final CourtEventRepository courtEventRepository,
                                final AgencyLocationRepository agencyLocationRepository,
                                final ReferenceCodeRepository<MovementReason> eventTypeRepository,
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
    @VerifyBookingAccess(overrideRoles = "COURT_HEARING_MAINTAINER")
    @HasWriteScope
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER')")
    public CourtHearing scheduleHearing(final Long bookingId, final Long courtCaseId, final PrisonToCourtHearing hearing) {
        checkHearingIsInFuture(hearing.getCourtHearingDateTime());

        final var offenderBooking = activeOffenderBookingFor(bookingId);

        final var courtCase = getActiveCourtCaseFor(courtCaseId, offenderBooking);

        checkPrisonLocationSameAsOffenderBooking(hearing.getFromPrisonLocation(), offenderBooking);

        final var courtEvent = CourtEvent.builder()
                .courtLocation(getActiveCourtFor(hearing.getToCourtLocation()))
                .courtEventType(eventTypeRepository.findById(COURT).orElseThrow())
                .directionCode("OUT")
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
                .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(courtCase)
                .startTime(hearing.getCourtHearingDateTime())
                .commentText(hearing.getComments())
                .build();

        final var courtHearing = toCourtHearing(courtEventRepository.save(courtEvent));

        log.debug("created court hearing id '{}' for court case id '{}', booking id '{}', offender id '{} and noms id '{}, location '{}', datetime '{}'",
                courtHearing.getId(), courtCase.getId(), offenderBooking.getBookingId(), offenderBooking.getOffender().getId(), offenderBooking.getOffender().getNomsId(), courtHearing.getLocation().getAgencyId(), courtHearing.getDateTime());

        return courtHearing;
    }

    @Transactional
    @VerifyBookingAccess(overrideRoles = "COURT_HEARING_MAINTAINER")
    @HasWriteScope
    @PreAuthorize("hasRole('COURT_HEARING_MAINTAINER')")
    public CourtHearing scheduleHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        checkHearingIsInFuture(hearing.getCourtHearingDateTime());

        final var offenderBooking = activeOffenderBookingFor(bookingId);

        checkPrisonLocationSameAsOffenderBooking(hearing.getFromPrisonLocation(), offenderBooking);

        final var courtEvent = CourtEvent.builder()
                .courtLocation(getActiveCourtFor(hearing.getToCourtLocation()))
                .courtEventType(eventTypeRepository.findById(COURT).orElseThrow())
                .directionCode("OUT")
                .eventDate(hearing.getCourtHearingDateTime().toLocalDate())
                .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                .offenderBooking(offenderBooking)
                .startTime(hearing.getCourtHearingDateTime())
                .commentText(hearing.getComments())
                .build();

        final var courtHearing = toCourtHearing(courtEventRepository.save(courtEvent));

        log.debug("created court hearing id '{}' for  booking id '{}', offender id '{} and noms id '{}, location '{}', datetime '{}''",
                courtHearing.getId(), offenderBooking.getBookingId(), offenderBooking.getOffender().getId(), offenderBooking.getOffender().getNomsId(), courtHearing.getLocation().getAgencyId(), courtHearing.getDateTime());

        return courtHearing;
    }

    /**
     * Returns all court hearings for a given booking ID for the given date range.
     */
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "COURT_HEARING_MAINTAINER"})
    public CourtHearings getCourtHearingsFor(final Long bookingId, final LocalDate fromDate, final LocalDate toDate) {
        checkFromAndToDatesAreValid(fromDate, toDate);

        final var courtHearingsBuilder = CourtHearings.builder();

        courtEventRepository.findAll(CourtEventFilter.builder()
                .bookingId(bookingId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build())
                .stream()
                .sorted(comparing(CourtEvent::getEventDateTime))
                .forEach(ce ->
                        courtHearingsBuilder.hearing(
                                CourtHearing.builder()
                                        .id(ce.getId())
                                        .dateTime(ce.getEventDateTime())
                                        .location(AgencyTransformer.transform(ce.getCourtLocation()))
                                        .build())
                );

        return courtHearingsBuilder.build();
    }

    private void checkFromAndToDatesAreValid(final LocalDate from, final LocalDate to) {
        if (from == null || to == null) {
            return;
        }

        if (to.isBefore(from)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }
    }

    private void checkHearingIsInFuture(final LocalDateTime courtHearingDateTime) {
        checkArgument(courtHearingDateTime.isAfter(LocalDateTime.now(clock)), "Court hearing must be in the future.");
    }

    private OffenderBooking activeOffenderBookingFor(final Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        checkArgument(offenderBooking.isActive(), "Offender booking with id %s is not active.", bookingId);

        return offenderBooking;
    }

    private OffenderCourtCase getActiveCourtCaseFor(final Long caseId, final OffenderBooking offenderBooking) {
        final var courtCase = offenderBooking.getCourtCaseBy(caseId).orElseThrow(EntityNotFoundException.withMessage("Court case with id %d not found.", caseId));

        checkArgument(courtCase.isActive(), "Court case with id %s is not active.", caseId);

        return courtCase;
    }

    private void checkPrisonLocationSameAsOffenderBooking(final String prisonLocation, final OffenderBooking booking) {
        final var prison = agencyLocationRepository.findById(prisonLocation).orElseThrow(EntityNotFoundException.withMessage("Prison with id %s not found.", prisonLocation));

        checkArgument(booking.getLocation().equals(prison), "Prison location does not match the bookings location.");
    }

    private AgencyLocation getActiveCourtFor(final String courtLocation) {
        final var agency = agencyLocationRepository.findById(courtLocation).orElseThrow(EntityNotFoundException.withMessage("Court with id %s not found.", courtLocation));

        checkArgument(agency.getType().getCode().equalsIgnoreCase("CRT"), "Supplied court location wih id %s is not a valid court location.", courtLocation);
        checkArgument(agency.getActiveFlag().isActive(), "Supplied court location wih id %s is not active.", courtLocation);

        return agency;
    }

    private CourtHearing toCourtHearing(final CourtEvent event) {
        return CourtHearing.builder()
                .id(event.getId())
                .location(AgencyTransformer.transform(event.getCourtLocation()))
                .dateTime(event.getEventDateTime())
                .build();
    }
}
