package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationCreationRequestData;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.OicHearingRequest;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResponse;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultDto;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultRequest;
import uk.gov.justice.hmpps.prison.api.model.OicSanctionRequest;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Sanction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing.OicHearingStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.OicSanctionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.Status;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationOffenceTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingResultRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicSanctionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.transformers.AdjudicationsTransformer;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final AdjudicationsPartyService adjudicationsPartyService;
    private final AgencyInternalLocationRepository internalLocationRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TelemetryClient telemetryClient;
    private final Clock clock;
    private final EntityManager entityManager;

    private final OicHearingRepository oicHearingRepository;
    private final OicHearingResultRepository oicHearingResultRepository;
    private final OicSanctionRepository oicSanctionRepository;
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
        final EntityManager entityManager,
        @Value("${batch.max.size:1000}") final int batchSize,
        final AdjudicationsPartyService adjudicationsPartyService,
        final OicHearingRepository oicHearingRepository,
        final OicHearingResultRepository oicHearingResultRepository,
        final OicSanctionRepository oicSanctionRepository) {
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
        this.entityManager = entityManager;
        this.batchSize = batchSize;
        this.adjudicationsPartyService = adjudicationsPartyService;
        this.oicHearingRepository = oicHearingRepository;
        this.oicHearingResultRepository = oicHearingResultRepository;
        this.oicSanctionRepository = oicSanctionRepository;
    }

    private List<AdjudicationOffenceType> offenceCodesFrom(List<String> suppliedOffenceCodes) {
        var offenceCodes = List.< AdjudicationOffenceType >of();
        if (suppliedOffenceCodes != null) {
            offenceCodes = adjudicationsOffenceTypeRepository.findByOffenceCodeIn(suppliedOffenceCodes);
            if (offenceCodes.size() != (new HashSet<>(suppliedOffenceCodes)).size()) {
                throw new RuntimeException("Offence code not found");
            }
        }
        return offenceCodes;
    }

    @Transactional
    @VerifyOffenderAccess
    public AdjudicationCreationRequestData generateAdjudicationCreationData(@NotNull final String offenderNo) {
        final var offenderBookingEntry = bookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
            .orElseThrow(() -> EntityNotFoundException.withMessage(format("Could not find a current booking for Offender No %s", offenderNo)));
        final var adjudicationNumber = adjudicationsRepository.getNextAdjudicationNumber();
        return AdjudicationCreationRequestData.builder()
            .bookingId(offenderBookingEntry.getBookingId())
            .adjudicationNumber(adjudicationNumber)
            .build();
    }

    @Transactional
    @VerifyOffenderAccess
    public AdjudicationDetail createAdjudication(@NotNull final String offenderNo,
                                                 @NotNull @Valid final NewAdjudication adjudication) {
        final var currentDateTime = adjudication.getReportedDateTime();
        final var incidentDateTime = adjudication.getIncidentTime();

        final var offenceCodes = offenceCodesFrom(adjudication.getOffenceCodes());

        final var reporterName = adjudication.getReporterName();
        final var reporter = staffUserAccountRepository.findById(reporterName)
            .orElseThrow(() -> new RuntimeException(format("User not found %s", reporterName)));

        final var offenderBookingEntry = bookingRepository.findByBookingId(adjudication.getBookingId())
                .orElseThrow(() -> new RuntimeException(format("Could not find the booking with id %d", adjudication.getBookingId())));
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

        // Check it doesn't already exist
        // Note that we can't check whether this is a valid adjudication number as they are allocated in batches
        final var adjudicationNumber = adjudication.getAdjudicationNumber();
        final var existingAdjudicationParty = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber);
        if (existingAdjudicationParty.isPresent()) {
            throw EntityNotFoundException.withMessage(format("Adjudication with number %d already exists", adjudicationNumber));
        }

        final var offenderAdjudicationEntry = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudicationToCreate, 1L))
            .adjudicationNumber(adjudicationNumber)
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .partyAddedDate(currentDateTime.toLocalDate())
            .actionCode(actionCode)
            .offenderBooking(offenderBookingEntry)
            .build();

        adjudicationToCreate.getParties().add(offenderAdjudicationEntry);
        adjudicationsRepository.save(adjudicationToCreate);

        addOffenceCharges(offenderAdjudicationEntry, offenceCodes);

        adjudicationsPartyService.updateAdjudicationParties(
            adjudicationNumber,
            adjudication.getVictimStaffUsernames(),
            adjudication.getVictimOffenderIds(),
            adjudication.getConnectedOffenderIds());

        final var updatedAdjudication = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber).get();

        trackAdjudicationCreated(updatedAdjudication, reporterName);

        return AdjudicationsTransformer.transformToDto(updatedAdjudication);
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

            final var offenceCodes = offenceCodesFrom(adjudication.getOffenceCodes());
            addOffenceCharges(adjudicationOffenderPartyToUpdate, offenceCodes);
        }
        adjudicationsRepository.save(adjudicationToUpdate);

        adjudicationsPartyService.updateAdjudicationParties(
            adjudicationNumber,
            adjudication.getVictimStaffUsernames(),
            adjudication.getVictimOffenderIds(),
            adjudication.getConnectedOffenderIds()
        );

        final var updatedAdjudication = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber).get();

        trackAdjudicationUpdated(adjudicationNumber, updatedAdjudication);

        return AdjudicationsTransformer.transformToDto(updatedAdjudication);
    }

    public AdjudicationDetail getAdjudication(@NotNull final Long adjudicationNumber) {
        final var requestedAdjudication = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Adjudication not found with the number %d", adjudicationNumber)));
        return AdjudicationsTransformer.transformToDto(requestedAdjudication);
    }

    public List<AdjudicationDetail> getAdjudications(final List<Long> adjudicationNumbers) {
        return Lists.partition(adjudicationNumbers, batchSize).stream().flatMap(
                numbers -> adjudicationsRepository.findByParties_AdjudicationNumberIn(numbers).stream()
            ).map(AdjudicationsTransformer::transformToDto)
            .toList();
    }

    @Transactional
    @VerifyOffenderAccess
    public OicHearingResponse createOicHearing(final Long adjudicationNumber, final OicHearingRequest oicHearingRequest) {
        adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Could not find adjudication number %d", adjudicationNumber)));

        oicHearingLocationValidation(oicHearingRequest.getHearingLocationId());

        final var hearingDate = oicHearingRequest.getDateTimeOfHearing().toLocalDate();
        final var hearingTime = oicHearingRequest.getDateTimeOfHearing();

        final var oicHearing = OicHearing.builder()
            .eventStatus(OicHearingStatus.SCH)
            .oicHearingType(oicHearingRequest.getOicHearingType())
            .adjudicationNumber(adjudicationNumber)
            .hearingDate(hearingDate)
            .hearingTime(hearingTime)
            .scheduleDate(hearingDate)
            .scheduleTime(hearingTime)
            .internalLocationId(oicHearingRequest.getHearingLocationId()).build();

        final var savedOicHearing = oicHearingRepository.save(oicHearing);

        return OicHearingResponse.builder()
            .oicHearingId(savedOicHearing.getOicHearingId())
            .dateTimeOfHearing(savedOicHearing.getHearingTime())
            .hearingLocationId(savedOicHearing.getInternalLocationId())
            .build();
    }

    @Transactional
    @VerifyOffenderAccess
    public void amendOicHearing(final Long adjudicationNumber, final long oicHearingId, final OicHearingRequest oicHearingRequest) {
        final var hearingToAmend = getWithValidationChecks(adjudicationNumber, oicHearingId).getLeft();
        oicHearingLocationValidation(oicHearingRequest.getHearingLocationId());

        final var hearingDate = oicHearingRequest.getDateTimeOfHearing().toLocalDate();
        final var hearingTime = oicHearingRequest.getDateTimeOfHearing();

        hearingToAmend.setHearingDate(hearingDate);
        hearingToAmend.setHearingTime(hearingTime);
        hearingToAmend.setOicHearingType(oicHearingRequest.getOicHearingType());
        hearingToAmend.setInternalLocationId(oicHearingRequest.getHearingLocationId());

        oicHearingRepository.save(hearingToAmend);
    }

    @Transactional
    @VerifyOffenderAccess
    public void deleteOicHearing(final Long adjudicationNumber, final long oicHearingId) {
        final var hearingToDelete = getWithValidationChecks(adjudicationNumber, oicHearingId).getLeft();
        oicHearingRepository.delete(hearingToDelete);
    }

    @Transactional
    @VerifyOffenderAccess
    public OicHearingResultDto createOicHearingResult(
        final Long adjudicationNumber,
        final Long oicHearingId,
        final OicHearingResultRequest oicHearingResultRequest) {

        final var pair = getWithValidationChecks(adjudicationNumber, oicHearingId);
        final var oicHearing = pair.getLeft();
        final var adjudication = pair.getRight();

        if (oicHearingResultRepository.findById(new OicHearingResult.PK(oicHearingId, 1L)).isPresent()) {
            throw new ValidationException(format("Hearing result for hearing id %d already exist for adjudication number %d", oicHearingId, adjudicationNumber));
        }

        final var staff = staffUserAccountRepository.findByUsername(oicHearingResultRequest.getAdjudicator())
            .orElseThrow(() -> new EntityNotFoundException(format("Adjudicator not found for username %s", oicHearingResultRequest.getAdjudicator())));

        oicHearing.setAdjudicator(staff.getStaff());
        oicHearingRepository.save(oicHearing);

        Long oicOffenceId;
        try {
            oicOffenceId = adjudication.getOffenderParty().get().getCharges().get(0).getOffenceType().getOffenceId();
        } catch (Exception e) {
            throw EntityNotFoundException.withMessage(format("OicOffenceId not found for adjudicationNumber %d and oicHearingId %d", adjudicationNumber, oicHearingId), e.getMessage());
        }

        final var oicHearingResult = oicHearingResultRepository.save(OicHearingResult.builder()
            .oicHearingId(oicHearingId)
            .chargeSeq(1L)
            .resultSeq(1L)
            .pleaFindingCode(oicHearingResultRequest.getPleaFindingCode())
            .findingCode(oicHearingResultRequest.getFindingCode())
            .agencyIncidentId(adjudication.getAgencyIncidentId())
            .oicOffenceId(oicOffenceId)
            .build());


        return OicHearingResultDto.builder()
            .findingCode(oicHearingResult.getFindingCode())
            .pleaFindingCode(oicHearingResult.getPleaFindingCode())
            .build();
    }

    @Transactional
    @VerifyOffenderAccess
    public OicHearingResultDto amendOicHearingResult(
        final Long adjudicationNumber,
        final Long oicHearingId,
        final OicHearingResultRequest oicHearingResultRequest) {

        final var oicHearing = getWithValidationChecks(adjudicationNumber, oicHearingId).getLeft();

        final var oicHearingResult = oicHearingResultRepository.findById(new OicHearingResult.PK(oicHearingId, 1L))
            .orElseThrow(new EntityNotFoundException(format("No hearing result found for hearing id %d and adjudication number %d", oicHearingId, adjudicationNumber)));

        final var staff = staffUserAccountRepository.findByUsername(oicHearingResultRequest.getAdjudicator())
            .orElseThrow(() -> new EntityNotFoundException(format("Adjudicator not found for username %s", oicHearingResultRequest.getAdjudicator())));

        oicHearing.setAdjudicator(staff.getStaff());
        oicHearingRepository.save(oicHearing);

        oicHearingResult.setPleaFindingCode(oicHearingResultRequest.getPleaFindingCode());
        oicHearingResult.setFindingCode(oicHearingResultRequest.getFindingCode());
        oicHearingResultRepository.save(oicHearingResult);

        return OicHearingResultDto.builder()
            .findingCode(oicHearingResult.getFindingCode())
            .pleaFindingCode(oicHearingResult.getPleaFindingCode())
            .build();
    }

    @Transactional
    @VerifyOffenderAccess
    public void deleteOicHearingResult(
        final Long adjudicationNumber,
        final Long oicHearingId) {

        final var oicHearing = getWithValidationChecks(adjudicationNumber, oicHearingId).getLeft();

        final var oicHearingResult = oicHearingResultRepository.findById(new OicHearingResult.PK(oicHearingId, 1L))
            .orElseThrow(new EntityNotFoundException(format("No hearing result found for hearing id %d and adjudication number %d", oicHearingId, adjudicationNumber)));

        oicHearing.setAdjudicator(null);
        oicHearingRepository.save(oicHearing);
        oicHearingResultRepository.delete(oicHearingResult);
    }

    @Transactional
    @VerifyOffenderAccess
    public Sanction createOicSanction(
        final Long adjudicationNumber,
        final Long oicHearingId,
        final OicSanctionRequest oicSanctionRequest) {

        final var pair = getWithValidationChecks(adjudicationNumber, oicHearingId);

        List<OicSanction> oicSanctions = oicSanctionRepository.findAllByOicHearingId(oicHearingId);

        final var oicSanction = oicSanctionRepository.save(OicSanction.builder()
            .offenderBookId(-1L)                                        //?
            .sanctionSeq(oicSanctions.size() + 1L)
            .oicSanctionCode(OicSanctionCode.valueOf(oicSanctionRequest.getSanctionType()))
            .compensationAmount(oicSanctionRequest.getCompensationAmount())
            .sanctionMonths(oicSanctionRequest.getSanctionMonths())
            .sanctionDays(oicSanctionRequest.getSanctionDays())
            .commentText(oicSanctionRequest.getComment())
            .effectiveDate(oicSanctionRequest.getEffectiveDate())
//                .appealingDate()
//                .consecutiveOffenderBookId()
            .consecutiveSanctionSeq(oicSanctionRequest.getConsecutiveSanctionSeq())
            .oicHearingId(oicHearingId)
            .status(Status.valueOf(oicSanctionRequest.getStatus()))
//                .offenderAdjustId()
            .resultSeq(1L)
//                .statusDate()
            .oicIncidentId(adjudicationNumber)
//                .lidsSanctionNumber()
            .build());

        return Sanction.builder()
            .build();
    }

    private void oicHearingLocationValidation(final Long hearingLocationId){
        internalLocationRepository.findOneByLocationId(hearingLocationId)
            .orElseThrow(() -> new ValidationException(format("Invalid hearing location id %d", hearingLocationId)));
    }

    private Pair<OicHearing, Adjudication> getWithValidationChecks(final Long adjudicationNumber, final long hearingId){
        final var adjudication = adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Could not find adjudication number %d", adjudicationNumber)));

        final var hearing = oicHearingRepository.findById(hearingId)
            .orElseThrow(EntityNotFoundException.withMessage(format("Could not find oic hearingId %d for adjudication number %d", hearingId, adjudicationNumber)));

        if(!Objects.equals(hearing.getAdjudicationNumber(), adjudicationNumber))
            throw new ValidationException(format("oic hearingId %d is not linked to adjudication number %d", hearingId, adjudicationNumber));

        return Pair.of(hearing, adjudication);
    }

    private void addOffenceCharges(AdjudicationParty adjudicationPartyToUpdate, List<AdjudicationOffenceType> offenceCodes) {
        final var existingCharges = adjudicationPartyToUpdate.getCharges();
        final var existingChargesBySequenceNumber = existingCharges.stream().collect(Collectors.toMap(AdjudicationCharge::getSequenceNumber, Function.identity()));
        adjudicationPartyToUpdate.getCharges().clear();
        // If we do not run these inserts one at a time Oracle errors as it is not able to correctly run the trigger
        // AGENCY_INCIDENT_CHARGES_T1
        entityManager.flush();
        for (int i = 0; i < offenceCodes.size(); i++) {
            final var offenceCode = offenceCodes.get(i);
            final long sequenceNumber = i + 1;
            final var existingChargeToAdd = existingChargesBySequenceNumber.get(sequenceNumber);
            if (existingChargeToAdd != null) {
                existingChargeToAdd.setOffenceType(offenceCode);
                adjudicationPartyToUpdate.getCharges().add(existingChargeToAdd);
            } else {
                adjudicationPartyToUpdate.getCharges().add(AdjudicationCharge.builder()
                    .id(new PK(adjudicationPartyToUpdate, sequenceNumber))
                    .offenceType(offenceCode)
                    .oicChargeId(String.format("%d/%d", adjudicationPartyToUpdate.getAdjudicationNumber(), i+1))
                    .build());
            }
            // If we do not run these inserts one at a time Oracle errors as it is not able to correctly run the trigger
            // AGENCY_INCIDENT_CHARGES_T1
            entityManager.flush();
        }
    }

    private void trackAdjudicationCreated(final Adjudication createdAdjudication, String reporterName) {
        final Map<String, String> logMap = new HashMap<>();
        logMap.put("reporterUsername", reporterName);
        logMap.put("offenderNo", createdAdjudication.getOffenderParty()
            .map(AdjudicationParty::getOffenderBooking)
            .map(OffenderBooking::getOffender)
            .map(Offender::getNomsId)
            .orElse(""));
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
}
