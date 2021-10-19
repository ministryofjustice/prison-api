package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParties;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AdjudicationsService {

    public static final String INCIDENT_ROLE_OFFENDER = "S";

    private final AdjudicationRepository adjudicationsRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingRepository bookingRepository;
    private final ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository;
    private final ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final AgencyInternalLocationRepository internalLocationRepository;
    private final AuthenticationFacade authenticationFacade;

    public AdjudicationsService(final AdjudicationRepository adjudicationsRepository,
                                final StaffUserAccountRepository staffUserAccountRepository,
                                final OffenderBookingRepository bookingRepository,
                                final ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository,
                                final ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository,
                                final AgencyLocationRepository agencyLocationRepository,
                                final AgencyInternalLocationRepository internalLocationRepository,
                                final AuthenticationFacade authenticationFacade) {
        this.adjudicationsRepository = adjudicationsRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.bookingRepository = bookingRepository;
        this.incidentTypeRepository = incidentTypeRepository;
        this.actionCodeRepository = actionCodeRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.internalLocationRepository = internalLocationRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @PreAuthorize("hasRole('SYSTEM_USER')")
    @VerifyBookingAccess
    @HasWriteScope
    @Transactional
    public AdjudicationDetail createAdjudication(@NotNull final Long bookingId, @NotNull @Valid final NewAdjudication adjudication) {
        final var reporterName = authenticationFacade.getCurrentUsername();
        final var reporter = staffUserAccountRepository.findById(reporterName);

        final var offenderBookingEntry = bookingRepository.findById(bookingId);
        final var currentDateTime = LocalDateTime.now();
        final var incidentDateTime = adjudication.getIncidentTime();
        final var incidentType = incidentTypeRepository.findById(AdjudicationIncidentType.GOVERNORS_REPORT);
        final var actionCode = actionCodeRepository.findById(AdjudicationActionCode.PLACED_ON_REPORT);

        final var incidentInternalLocationDetails = internalLocationRepository.findOneByLocationId(adjudication.getIncidentLocationId());
        final var agencyDetails = agencyLocationRepository.findById(incidentInternalLocationDetails.get().getAgencyId());

        final var adjudicationToCreate = Adjudication.builder()
            .incidentDate(incidentDateTime.toLocalDate())
            .incidentTime(incidentDateTime)
            .reportDate(currentDateTime.toLocalDate())
            .reportTime(currentDateTime)
            .agencyLocation(agencyDetails.get())
            .internalLocation(incidentInternalLocationDetails.get())
            .incidentDetails(adjudication.getStatement())
            .incidentStatus("ACTIVE")
            .incidentType(incidentType.get())
            .lockFlag("N")
            .staffReporterId(reporter.get().getStaff())
            .build();
        final var incidentNumber = adjudicationsRepository.getNextIncidentId();
        final var offenderAdjudicationEntry = AdjudicationParties.builder()
            .id(new AdjudicationParties.PK(adjudicationToCreate, 1L))
            .incidentId(incidentNumber)
            .incidentRole(INCIDENT_ROLE_OFFENDER)
            .partyAddedDate(currentDateTime.toLocalDate())
            .actionCode(actionCode.get())
            .offenderBooking(offenderBookingEntry.get())
            .build();

        adjudicationToCreate.setParties(List.of(offenderAdjudicationEntry));

        return transformToDto(adjudicationsRepository.save(adjudicationToCreate), reporter.get());

        /*
        assertReporterId();
        assertDateAndTimeNotNull();
        assertLocationId();
        assertStatementLessThan4000();
        assertAllBookingIdsInCaseload(appointments.getAppointments(), agencyId);

        final var defaults = appointments.getAppointmentDefaults();

        final var agencyId = findLocationInUserLocations(defaults.getLocationId())
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Location does not exist or is not in your caseload."))
            .getAgencyId();

        final var createdAppointments = appointmentsWithRepeats.stream().map(a -> {
                final var appointmentId = bookingRepository.createAppointment(a, defaults, agencyId);

                return CreatedAppointmentDetails.builder()
                    .appointmentEventId(appointmentId)
                    .bookingId(a.getBookingId())
                    .startTime(a.getStartTime())
                    .endTime(a.getEndTime())
                    .appointmentType(defaults.getAppointmentType())
                    .locationId(defaults.getLocationId())
                    .build();
            }
        ).collect(java.util.stream.Collectors.toList());

        trackAppointmentsCreated(createdAppointments.size(), defaults);

        return createdAppointments;
        */
    }

    private AdjudicationDetail transformToDto(Adjudication adjudication, StaffUserAccount reporterAccount) {
        return AdjudicationDetail.builder()
            // TODO - need logic to extract this
            .adjudicationNumber(adjudication.getParties().get(0).getIncidentId())
            .incidentTime(adjudication.getIncidentTime())
            .agencyId(adjudication.getAgencyLocation().getId())
            .internalLocationId(adjudication.getInternalLocation().getLocationId())
            .incidentDetails(adjudication.getIncidentDetails())
            // TODO Check
            .reportNumber(adjudication.getAgencyIncidentId())
            .reportType(adjudication.getIncidentType().getCode())
            .reporterFirstName(reporterAccount.getStaff().getFirstName())
            .reporterLastName(reporterAccount.getStaff().getLastName())
            .reportTime(adjudication.getReportTime())
            .build();
    }
}
