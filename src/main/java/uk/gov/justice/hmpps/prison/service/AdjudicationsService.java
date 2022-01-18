package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import kotlin.ranges.LongRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;


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


    private List offenceCodesFrom(NewAdjudication adjudication) {
        var offenceCodes = List.< AdjudicationOffenceType >of();
        if (adjudication.getOffenceCodes() != null) {
            offenceCodes = adjudicationsOffenceTypeRepository.findByOffenceCodeIn(adjudication.getOffenceCodes());
            if (offenceCodes.size() != adjudication.getOffenceCodes().size()) {
                throw new RuntimeException("Offence code not found");
            }
        }
        return offenceCodes;
    }

    @Transactional
    @VerifyOffenderAccess
    public AdjudicationDetail createAdjudication(@NotNull final String offenderNo, @NotNull @Valid final NewAdjudication adjudication) {
        final var reporterName = authenticationFacade.getCurrentUsername();
        final var currentDateTime = LocalDateTime.now(clock);
        final var incidentDateTime = adjudication.getIncidentTime();

        final var offenceCodes = offenceCodesFrom(adjudication);

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

        // TODO comment back in when the model looks better.



        // final var createdAdjudication = adjudicationsRepository.save(adjudicationToCreate);

        // trackAdjudicationCreated(createdAdjudication);

        // return transformToDto(createdAdjudication);
        return transformToDto(adjudicationToCreate);
    }

    private List<AdjudicationParty> updateAncillaryAdjudicationParties(
        List<Staff> victimStaff,
        List<OffenderBooking> victimOffenderBookingIds,
        List<OffenderBooking> connectedOffenderBookingIds,
        Adjudication adjudication
    ) {
        final var currentDateTime = LocalDateTime.now(clock);
        AtomicReference<Long> maxSequence = new AtomicReference<>(adjudication.getMaxSequence());
        var definitiveVictimsStaff = new ArrayList<>(adjudication.getVictimsStaff());
        var definitiveVictimsOffender = new ArrayList<>(adjudication.getVictimsOffenders());
        var definitiveConnectedOffenders = new ArrayList<>(adjudication.getConnectedOffenders());

        victimStaffIdsToRemoveWithCount(victimStaff, adjudication.getVictimsStaff())
            .forEach((staffId, toRemoveCount) ->
                removeLastN(definitiveVictimsStaff, p -> staffId.equals(p.getStaffId().getStaffId()), toRemoveCount));

        victimStaffIdsToAddWithCount(victimStaff, adjudication.getVictimsStaff())
            .forEach((staffId, toAddCount) -> LongStream.range(0, toAddCount)
                    .forEach(i -> {
                        maxSequence.set(maxSequence.get() + 1);
                        definitiveVictimsStaff.add(AdjudicationParty.builder()
                            .id(new AdjudicationParty.PK(adjudication, maxSequence.get()))
                            .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
                            .partyAddedDate(currentDateTime.toLocalDate())
                            .staffId(victimStaff.stream().filter(s -> staffId.equals(s.getStaffId())).findFirst().get())
                            .build());
                    }));

        victimOffenderBookingIdsToRemoveWithCount(victimOffenderBookingIds, adjudication.getVictimsOffenders())
            .forEach((offenderBookingId, toRemoveCount) ->
                removeLastN(definitiveVictimsOffender, p -> offenderBookingId.equals(p.getOffenderBooking().getBookingId()), toRemoveCount));

        victimOffenderBookingIdsToAddWithCount(victimOffenderBookingIds, adjudication.getVictimsOffenders())
            .forEach((offenderBookingId, toAddCount) -> LongStream.range(0, toAddCount)
                .forEach(i -> {
                    maxSequence.set(maxSequence.get() + 1);
                    definitiveVictimsOffender.add(AdjudicationParty.builder()
                        .id(new AdjudicationParty.PK(adjudication, maxSequence.get()))
                        .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
                        .partyAddedDate(currentDateTime.toLocalDate())
                        .offenderBooking(victimOffenderBookingIds.stream().filter(o -> offenderBookingId.equals(o.getBookingId())).findFirst().get())
                        .build());
                }));

        return List.of(definitiveVictimsStaff, definitiveVictimsOffender).stream().flatMap(Collection::stream).toList();
    }

    private <T> List<T> lastN(List<T> all, Predicate<T> predicate, Long n){
        var reversed = new ArrayList<>(all);
        Collections.reverse(reversed);
        return reversed.stream().filter(predicate).limit(n).toList();
    }

    private <T> void removeLastN(List<T> all, Predicate<T> predicate, Long n) {
        var toRemove = lastN(all, predicate, n);
        all.removeAll(toRemove);
    }

    private Map<Long, Long> victimOffenderBookingIdsToRemoveWithCount(List<OffenderBooking> offenderBookingsNow, List<AdjudicationParty> currentAdjudicationParties) {
        return idsToRemoveWithCount(
            offenderBookingsNow.stream().map(OffenderBooking::getBookingId).toList(),
            currentAdjudicationParties.stream().map(a -> a.getOffenderBooking().getBookingId()).toList());
    }

    private Map<Long, Long> victimOffenderBookingIdsToAddWithCount(List<OffenderBooking> offenderBookingsNow, List<AdjudicationParty> currentAdjudicationParties) {
        return idsToAddWithCount(
            offenderBookingsNow.stream().map(OffenderBooking::getBookingId).toList(),
            currentAdjudicationParties.stream().map(a -> a.getOffenderBooking().getBookingId()).toList());
    }

    private Map<Long, Long> victimStaffIdsToAddWithCount(List<Staff> staffVictimsNow, List<AdjudicationParty> currentAdjudicationParties) {
        return idsToAddWithCount(
            staffVictimsNow.stream().map(Staff::getStaffId).toList(),
            currentAdjudicationParties.stream().map(a -> a.getStaffId().getStaffId()).toList());
    }

    private Map<Long, Long> victimStaffIdsToRemoveWithCount(List<Staff> staffVictimsNow, List<AdjudicationParty> currentAdjudicationParties) {
        return idsToRemoveWithCount(
            staffVictimsNow.stream().map(Staff::getStaffId).toList(),
            currentAdjudicationParties.stream().map(a -> a.getStaffId().getStaffId()).toList());
    }

    private Map<Long, Long> idsToAddWithCount(List<Long> desired, List<Long> current) {
        return addAndRemoveWithCount(desired, current).entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    private Map<Long, Long> idsToRemoveWithCount(List<Long> desired, List<Long> current) {
        return addAndRemoveWithCount(desired, current).entrySet().stream()
            .filter(e -> e.getValue() < 0)
            .collect(Collectors.toMap(e -> e.getKey(), e -> -e.getValue()));
    }

    private Map<Long, Long> addAndRemoveWithCount(List<Long> desired, List<Long> current){
        var currentIdsWithCount = current.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        var desiredIdsWithCount = desired.stream().collect(Collectors.groupingBy(staffId -> staffId, Collectors.counting()));
        var unionIds = List.of(desired, current).stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());
        var toAddAndRemoveWithCount = unionIds.stream().map(id -> {
            var currentCount = currentIdsWithCount.containsKey(id) ? currentIdsWithCount.get(id) : 0;
            var desiredCount = desiredIdsWithCount.containsKey(id) ? desiredIdsWithCount.get(id) : 0;
            return Pair.of(id, desiredCount - currentCount);
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return toAddAndRemoveWithCount;
    }

    @Transactional
    public AdjudicationDetail updateAdjudication(@NotNull Long adjudicationNumber, @NotNull @Valid UpdateAdjudication adjudication) {

        final var victimsStaff = adjudication.getVictimStaffIds().stream().map(id ->
                staffUserAccountRepository.findById(id).orElseThrow(() -> new RuntimeException(format("User not found %s", id))).getStaff()
        ).collect(Collectors.toList());
        final var victimsOffendersBooking = adjudication.getVictimOffenderIds().stream().map(id -> bookingRepository.findByOffenderNomsIdAndBookingSequence(id, 1)
            .orElseThrow(() -> new RuntimeException(format("Could not find a current booking for Offender No %s", id)))
        ).collect(Collectors.toList());
        final var connectedOffendersBooking = adjudication.getConnectedOffenders().stream().map(id -> bookingRepository.findByOffenderNomsIdAndBookingSequence(id, 1)
            .orElseThrow(() -> new RuntimeException(format("Could not find a current booking for Offender No %s", id)))
        ).collect(Collectors.toList());
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

        var qq = updateAncillaryAdjudicationParties(victimsStaff, victimsOffendersBooking, connectedOffendersBooking, adjudicationToUpdate);
        var originalAdjudicationParty = adjudicationToUpdate.getOffenderParty();
        adjudicationToUpdate.getParties().clear();
        adjudicationToUpdate.getParties().add(originalAdjudicationParty.get());
        adjudicationToUpdate.getParties().addAll(qq);



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
            .collect(Collectors.toList());
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
        return adjudicationCharges.stream().map(c -> c.getOffenceType().getOffenceCode()).collect(Collectors.toList());
    }
}
