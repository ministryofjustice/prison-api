package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.service.transformers.AdjudicationsTransformer;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class AdjudicationsPartyService {
    private final AdjudicationRepository adjudicationRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingRepository bookingRepository;
    private final Clock clock;
    private final EntityManager entityManager;

    public AdjudicationsPartyService(
        final StaffUserAccountRepository staffUserAccountRepository,
        final OffenderBookingRepository bookingRepository,
        final AdjudicationRepository adjudicationRepository,
        final Clock clock,
        final EntityManager entityManager) {
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.adjudicationRepository = adjudicationRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
        this.entityManager = entityManager;
    }

    @Transactional
    public AdjudicationDetail updateAdjudicationParties(
        @NotNull long adjudicationNumber,
        @NotNull Collection<Long> victimStaffIds,
        @NotNull Collection<String> victimOffenderIds,
        @NotNull Collection<String> connectedOffenderIds)
    {
        final var adjudication = adjudicationRepository.findByParties_AdjudicationNumber(adjudicationNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("Adjudication with number %s does not exist", adjudicationNumber)));
        final var victimStaff = victimStaffIds != null ?
            victimStaffIds.stream()
                .map(id -> staffUserAccountRepository.findByStaff_StaffId(id)
                    .orElseThrow(() -> new RuntimeException(format("User not found %s", id))).getStaff()
                ).collect(Collectors.toList()) : List.<Staff>of();
        final var victimOffenderBookings = victimOffenderIds != null ?
            victimOffenderIds.stream()
                .map(id -> bookingRepository.findByOffenderNomsIdAndBookingSequence(id, 1)
                    .orElseThrow(() -> new RuntimeException(format("Could not find a current booking for Offender No %s", id)))
                ).collect(Collectors.toList()) : List.<OffenderBooking>of();
        final var connectedOffenderBookings = connectedOffenderIds != null ?
            connectedOffenderIds.stream().map(id -> bookingRepository.findByOffenderNomsIdAndBookingSequence(id, 1)
                .orElseThrow(() -> new RuntimeException(format("Could not find a current booking for Offender No %s", id)))
            ).collect(Collectors.toList()) : List.<OffenderBooking>of();

        var offenderParty = adjudication.getOffenderParty().get();
        var remainingAdjudicationParties = ancillaryAdjudicationPartiesToRemain(victimStaff, victimOffenderBookings, connectedOffenderBookings, adjudication);
        adjudication.getParties().clear();
        adjudication.getParties().add(offenderParty);
        // There is a database trigger AGENCY_INCIDENT_PARTIES_T1 intended to maintain the uniqueness of offender bookings
        // We therefore have to remove all the adjudication parties that are not required and flush, which will not violate
        // the trigger. After that we add the new adjudication parties one at a time as Oracle states that it can not
        // run the trigger correctly if we add more than one at a time in a batch.
        adjudication.getParties().addAll(remainingAdjudicationParties);
        // Flush now we have removed the unnecessary adjudication parties.
        entityManager.flush();

        var newAdjudicationParties = newAncillaryAdjudicationParties(victimStaff, victimOffenderBookings, connectedOffenderBookings, adjudication);
        newAdjudicationParties.forEach(newAdjudicationParty -> {
            adjudication.getParties().add(newAdjudicationParty);
            // If we do not run these inserts one at a time Oracle errors as it is not able to correctly run the trigger
            // AGENCY_INCIDENT_PARTIES_T1
            entityManager.flush();
        });
        adjudicationRepository.save(adjudication);
        return AdjudicationsTransformer.transformToDto(adjudication);
    }

    private Set<AdjudicationParty> newAncillaryAdjudicationParties(
        Collection<Staff> requiredVictimStaff,
        Collection<OffenderBooking> requiredVictimOffenderBookings,
        Collection<OffenderBooking> requiredConnectedOffenderBookings,
        Adjudication adjudication
    ) {
        var currentDateTime = LocalDateTime.now(clock);
        AtomicReference<Long> sequence = new AtomicReference<>(adjudication.getMaxSequence());

        var newVictimStaff =
            idsToAdd(requiredVictimStaff, adjudication.getVictimsStaff(), Staff::getStaffId).stream()
                .map(staffId ->
                    newVictimStaffAdjudicationParty(adjudication, sequence, currentDateTime, staffWithId(requiredVictimStaff, staffId)));

        var newVictimOffenders =
            idsToAdd(requiredVictimOffenderBookings, adjudication.getVictimsOffenderBookings(), OffenderBooking::getBookingId).stream()
                .map(offenderBookingId ->
                    newVictimOffenderAdjudicationParty(adjudication, sequence, currentDateTime, offenderBookingWithId(requiredVictimOffenderBookings, offenderBookingId)));

        var newConnectedOffenders =
            idsToAdd(requiredConnectedOffenderBookings, adjudication.getConnectedOffenderBookings(), OffenderBooking::getBookingId).stream()
                .map(offenderBookingId ->
                    newConnectedOffenderAdjudicationParty(adjudication, sequence, currentDateTime, offenderBookingWithId(requiredConnectedOffenderBookings, offenderBookingId)));

        return Stream.of(newVictimStaff, newVictimOffenders, newConnectedOffenders)
            .flatMap(Function.identity())
            .collect(Collectors.toSet());
    }


    private Set<AdjudicationParty> ancillaryAdjudicationPartiesToRemain(
        Collection<Staff> requiredVictimStaff,
        Collection<OffenderBooking> requiredVictimOffenderBookings,
        Collection<OffenderBooking> requiredConnectedOffenderBookings,
        Adjudication adjudication
    ) {
        var remainingVictimsStaffParties = new HashSet<>(adjudication.getVictimsStaffParties());
        var remainingVictimsOffenderParties = new HashSet<>(adjudication.getVictimsOffenderParties());
        var remainingConnectedOffendersParties = new HashSet<>(adjudication.getConnectedOffenderParties());

        idsToRemove(requiredVictimStaff, adjudication.getVictimsStaff(), Staff::getStaffId)
            .forEach(staffId -> remove(remainingVictimsStaffParties, AdjudicationParty::staffId, staffId));

        idsToRemove(requiredVictimOffenderBookings, adjudication.getVictimsOffenderBookings(), OffenderBooking::getBookingId)
            .forEach(offenderBookingId -> remove(remainingVictimsOffenderParties, AdjudicationParty::offenderBookingId, offenderBookingId));

        idsToRemove(requiredConnectedOffenderBookings, adjudication.getConnectedOffenderBookings(), OffenderBooking::getBookingId)
            .forEach(offenderBookingId -> remove(remainingConnectedOffendersParties, AdjudicationParty::offenderBookingId, offenderBookingId));

        return List.of(remainingVictimsStaffParties, remainingVictimsOffenderParties, remainingConnectedOffendersParties).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    private Staff staffWithId(Collection<Staff> staff, Long id) {
        return staff.stream().filter(s -> id.equals(s.getStaffId())).findFirst().get();
    }

    private OffenderBooking offenderBookingWithId(Collection<OffenderBooking> offenderBookings, Long id) {
        return offenderBookings.stream().filter(o -> id.equals(o.getBookingId())).findFirst().get();
    }

    private AdjudicationParty.AdjudicationPartyBuilder newAdjudicationParty(Adjudication adjudication, AtomicReference<Long> sequence, LocalDateTime currentDateTime) {
        sequence.set(sequence.get() + 1);
        return AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, sequence.get()))
            .partyAddedDate(currentDateTime.toLocalDate());
    }

    private AdjudicationParty newConnectedOffenderAdjudicationParty(Adjudication adjudication, AtomicReference<Long> sequence, LocalDateTime currentDateTime, OffenderBooking offenderBooking) {
        return newAdjudicationParty(adjudication, sequence, currentDateTime)
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .offenderBooking(offenderBooking).build();
    }

    private AdjudicationParty newVictimStaffAdjudicationParty(Adjudication adjudication, AtomicReference<Long> sequence, LocalDateTime currentDateTime, Staff victimStaff) {
        return newAdjudicationParty(adjudication, sequence, currentDateTime)
            .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
            .staff(victimStaff).build();
    }

    private AdjudicationParty newVictimOffenderAdjudicationParty(Adjudication adjudication, AtomicReference<Long> sequence, LocalDateTime currentDateTime, OffenderBooking offenderBooking) {
        return newAdjudicationParty(adjudication, sequence, currentDateTime)
            .incidentRole(Adjudication.INCIDENT_ROLE_VICTIM)
            .offenderBooking(offenderBooking).build();
    }

    public static <T> Set<Long> idsToAdd(Collection<T> required, Collection<T> current, Function<T, Long> toId) {
        var toAdd = new HashSet<>(required.stream().map(toId).toList());
        toAdd.removeAll(current.stream().map(toId).toList());
        return toAdd;
    }

    public static <T> Set<Long> idsToRemove(Collection<T> required, Collection<T> current, Function<T, Long> toId) {
        var toRemove = new HashSet<>(current.stream().map(toId).toList());
        toRemove.removeAll(required.stream().map(toId).toList());
        return toRemove;
    }

    public static <T> void remove(Collection<T> all, Function<T, Long> toId, Long id) {
        var toRemove = all.stream().filter(t -> id.equals(toId.apply(t))).toList();
        all.removeAll(toRemove);
    }
}
