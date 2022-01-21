package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationOffenceTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class AdjudicationsService {
    private final AdjudicationRepository adjudicationsRepository;
    private final AdjudicationOffenceTypeRepository adjudicationsOffenceTypeRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingRepository bookingRepository;
    private final ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository;
    private final ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final AgencyInternalLocationRepository internalLocationRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TelemetryClient telemetryClient;
    private final Clock clock;
    @Value("${batch.max.size:1000}")
    private final int batchSize;

    public AdjudicationsService(
        final AdjudicationRepository adjudicationsRepository,
        final AdjudicationOffenceTypeRepository adjudicationsOffenceTypeRepository,
        final StaffUserAccountRepository staffUserAccountRepository,
        final OffenderBookingRepository bookingRepository,
        final ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository,
        final ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository,
        final AgencyLocationRepository agencyLocationRepository,
        final AgencyInternalLocationRepository internalLocationRepository,
        final AuthenticationFacade authenticationFacade,
        final TelemetryClient telemetryClient,
        final Clock clock,
        @Value("${batch.max.size:1000}") final int batchSize) {
        this.adjudicationsRepository = adjudicationsRepository;
        this.adjudicationsOffenceTypeRepository = adjudicationsOffenceTypeRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.bookingRepository = bookingRepository;
        this.incidentTypeRepository = incidentTypeRepository;
        this.actionCodeRepository = actionCodeRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.internalLocationRepository = internalLocationRepository;
        this.authenticationFacade = authenticationFacade;
        this.telemetryClient = telemetryClient;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Transactional
    @VerifyOffenderAccess
    public AdjudicationDetail createAdjudication(@NotNull final String offenderNo, @NotNull @Valid final NewAdjudication adjudication) {
        final var reporterName = authenticationFacade.getCurrentUsername();
        final var currentDateTime = LocalDateTime.now(clock);
        final var incidentDateTime = adjudication.getIncidentTime();

        var offenceCodes = List.< AdjudicationOffenceType >of();
        if (adjudication.getOffenceCodes() != null) {
            offenceCodes = adjudicationsOffenceTypeRepository.findByOffenceCodeIn(adjudication.getOffenceCodes());
            if (offenceCodes.size() != adjudication.getOffenceCodes().size()) {
                throw new RuntimeException("Offence code not found");
            }
        }

        final var reporter = staffUserAccountRepository.findById(reporterName)
            .orElseThrow(() -> new RuntimeException(format("User not found %s", reporterName)));

        final var offenderBookingEntry = bookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
            .orElseThrow(() -> new RuntimeException(format("Could not find a current booking for Offender No %s", offenderNo)));
        final var incidentType = incidentTypeRepository.findById(AdjudicationIncidentType.GOVERNORS_REPORT)
            .orElseThrow(() -> new RuntimeException("Incident type not available"));
        final var actionCode = actionCodeRepository.findById(AdjudicationActionCode.PLACED_ON_REPORT)
            .orElseThrow(() -> new RuntimeException("Action code not available"));

        final var incidentInternalLocationDetails = internalLocationRepository.findOneByLocationId(adjudication.getIncidentLocationId())
            .orElseThrow(EntityNotFoundException.withMessage(format("Location with id %d does not exist or is not in your caseload", adjudication.getIncidentLocationId())));

        final var agencyId = adjudication.getAgencyId();
        final var agencyDetails = agencyLocationRepository.findById(agencyId)
            .orElseThrow(EntityNotFoundException.withMessage(format("Agency with id %s does not exist", agencyId)));

        final var adjudicationToCreate = Adjudication.builder()
            .incidentDate(incidentDateTime.toLocalDate())
            .incidentTime(incidentDateTime)
            .reportDate(currentDateTime.toLocalDate())
            .reportTime(currentDateTime)
            .agencyLocation(agencyDetails)
            .internalLocation(incidentInternalLocationDetails)
            .incidentDetails(adjudication.getStatement())
            .incidentStatus(Adjudication.INCIDENT_STATUS_ACTIVE)
            .incidentType(incidentType)
            .lockFlag(Adjudication.LOCK_FLAG_UNLOCKED)
            .staffReporter(reporter.getStaff())
            .build();
        final var adjudicationNumber = adjudicationsRepository.getNextAdjudicationNumber();
        final var offenderAdjudicationEntry = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudicationToCreate, 1L))
            .adjudicationNumber(adjudicationNumber)
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .partyAddedDate(currentDateTime.toLocalDate())
            .actionCode(actionCode)
            .offenderBooking(offenderBookingEntry)
            .build();
        final var offenceEntries = generateOffenceCharges(offenderAdjudicationEntry, offenceCodes);

        offenderAdjudicationEntry.setCharges(offenceEntries);
        adjudicationToCreate.setParties(List.of(offenderAdjudicationEntry));

        final var createdAdjudication = adjudicationsRepository.save(adjudicationToCreate);

        trackAdjudicationCreated(createdAdjudication);

        return transformToDto(createdAdjudication);
    }

    @Transactional
    public AdjudicationDetail updateAdjudication(@NotNull Long adjudicationNumber, @NotNull @Valid UpdateAdjudication adjudication) {
        final var adjudicationToUpdate = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Adjudication with number %s does not exist", adjudicationNumber)));

        final var incidentInternalLocationDetails = internalLocationRepository.findOneByLocationId(adjudication.getIncidentLocationId())
            .orElseThrow(EntityNotFoundException.withMessage(format("Location with id %d does not exist or is not in your caseload", adjudication.getIncidentLocationId())));

        adjudicationToUpdate.setIncidentDate(adjudication.getIncidentTime().toLocalDate());
        adjudicationToUpdate.setIncidentTime(adjudication.getIncidentTime());
        adjudicationToUpdate.setInternalLocation(incidentInternalLocationDetails);
        adjudicationToUpdate.setIncidentDetails(adjudication.getStatement());

        if (adjudication.getOffenceCodes() != null) {
            final var adjudicationOffenderPartyToUpdate = adjudicationToUpdate.getOffenderParty()
                .orElseThrow(() -> new RuntimeException("No offender associated with this adjudication"));

            final var offenceCodes = adjudicationsOffenceTypeRepository.findByOffenceCodeIn(adjudication.getOffenceCodes());
            final var offenceEntries = generateOffenceCharges(adjudicationOffenderPartyToUpdate, offenceCodes);
            adjudicationOffenderPartyToUpdate.getCharges().clear();
            adjudicationOffenderPartyToUpdate.getCharges().addAll(offenceEntries);
        }

        final var updatedAdjudication = adjudicationsRepository.save(adjudicationToUpdate);

        trackAdjudicationUpdated(adjudicationNumber, updatedAdjudication);

        return transformToDto(updatedAdjudication);
    }

    public AdjudicationDetail getAdjudication(@NotNull final Long adjudicationNumber) {
        final var requestedAdjudication = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Adjudication not found with the number %d", adjudicationNumber)));
        return transformToDto(requestedAdjudication);
    }

    public List<AdjudicationDetail> getAdjudications(final List<Long> adjudicationNumbers) {
        return Lists.partition(adjudicationNumbers, batchSize).stream().flatMap(
                numbers -> adjudicationsRepository.findByParties_AdjudicationNumberIn(numbers).stream()
            ).map(this::transformToDto)
            .toList();
    }

    private List<AdjudicationCharge> generateOffenceCharges(AdjudicationParty adjudicationPartyToUpdate, List<AdjudicationOffenceType> offenceCodes) {
        final var existingCharges = adjudicationPartyToUpdate.getCharges();
        final var existingChargesBySequenceNumber = existingCharges.stream().collect(Collectors.toMap(AdjudicationCharge::getSequenceNumber, Function.identity()));
        final var requiredAdjudicationCharges = new ArrayList<AdjudicationCharge>();
        for (int i = 0; i < offenceCodes.size(); i++) {
            final var offenceCode = offenceCodes.get(i);
            final long sequenceNumber = i + 1;
            final var existingChargeToAdd = existingChargesBySequenceNumber.get(sequenceNumber);
            if (existingChargeToAdd != null) {
                existingChargeToAdd.setOffenceType(offenceCode);
                requiredAdjudicationCharges.add(existingChargeToAdd);
            } else {
                requiredAdjudicationCharges.add(AdjudicationCharge.builder()
                    .id(new PK(adjudicationPartyToUpdate, sequenceNumber))
                    .offenceType(offenceCode)
                    .build());
            }
        }
        return requiredAdjudicationCharges;
    }

    private void trackAdjudicationCreated(final Adjudication createdAdjudication) {
        final Map<String, String> logMap = new HashMap<>();
        logMap.put("reporterUsername", authenticationFacade.getCurrentUsername());
        logMap.put("offenderNo", createdAdjudication.getOffenderParty()
            .map(o -> o.getOffenderBooking().getOffender().getNomsId()).orElse(""));
        trackAdjudicationDetails("AdjudicationCreated", createdAdjudication, logMap);
    }

    private void trackAdjudicationUpdated(final Long adjudicationNumber, final Adjudication updatedAdjudication) {
        final Map<String, String> logMap = new HashMap<>();
        logMap.put("adjudicationNumber", "" + adjudicationNumber);
        trackAdjudicationDetails("AdjudicationUpdated", updatedAdjudication, logMap);
    }

    private void trackAdjudicationDetails(final String eventName, final Adjudication adjudication, final Map<String, String> propertyMap) {
        propertyMap.put("incidentTime", adjudication.getIncidentTime().toString());
        propertyMap.put("incidentLocation", adjudication.getInternalLocation().getDescription());
        propertyMap.put("statementSize", "" + adjudication.getIncidentDetails().length());

        telemetryClient.trackEvent(eventName, propertyMap, null);
    }

    private AdjudicationDetail transformToDto(final Adjudication adjudication) {
        final var offenderPartyDetails = adjudication.getOffenderParty();
        final var bookingId = offenderPartyDetails.map(p -> p.getOffenderBooking().getBookingId()).orElse(null);
        final var offenderNo = offenderPartyDetails.map(p -> p.getOffenderBooking().getOffender().getNomsId()).orElse(null);
        return AdjudicationDetail.builder()
            .adjudicationNumber(offenderPartyDetails.map(AdjudicationParty::getAdjudicationNumber).orElse(null))
            .reporterStaffId(adjudication.getStaffReporter().getStaffId())
            .bookingId(bookingId)
            .offenderNo(offenderNo)
            .agencyId(adjudication.getAgencyLocation().getId())
            .incidentTime(adjudication.getIncidentTime())
            .incidentLocationId(adjudication.getInternalLocation().getLocationId())
            .statement(adjudication.getIncidentDetails())
            .offenceCodes(transformToOffenceCodes(offenderPartyDetails))
            .createdByUserId(adjudication.getCreatedByUserId())
            .build();
    }

    private List<String> transformToOffenceCodes(Optional<AdjudicationParty> offenderPartyDetails) {
        if (!offenderPartyDetails.isPresent()) {
            return null;
        }
        final var adjudicationCharges = offenderPartyDetails.get().getCharges();
        return adjudicationCharges.stream().map(c -> c.getOffenceType().getOffenceCode()).toList();
    }
}
