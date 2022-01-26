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
import uk.gov.justice.hmpps.prison.repository.jpa.model.*;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.*;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
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
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingRepository bookingRepository;
    private final Clock clock;
    private final EntityManager entityManager;

    public AdjudicationsPartyService(
        final StaffUserAccountRepository staffUserAccountRepository,
        final OffenderBookingRepository bookingRepository,
        final Clock clock,
        final EntityManager entityManager) {
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
        this.entityManager = entityManager;
    }

    @Transactional
    public Adjudication updateAncillaryAdjudicationParties(
        @NotNull Adjudication adjudication,
        @NotNull AdjudicationParty offenderParty,
        @NotNull List<Long> victimStaffIds,
        @NotNull List<String> victimOffenderIds,
        @NotNull List<String> connectedOffenderIds)
    {
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
        return adjudication;
    }

    private List<AdjudicationParty> newAncillaryAdjudicationParties(
        List<Staff> requiredVictimStaff,
        List<OffenderBooking> requiredVictimOffenderBookings,
        List<OffenderBooking> requiredConnectedOffenderBookings,
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
            .flatMap(Function.identity()).toList();
    }


    private List<AdjudicationParty> ancillaryAdjudicationPartiesToRemain(
        List<Staff> requiredVictimStaff,
        List<OffenderBooking> requiredVictimOffenderBookings,
        List<OffenderBooking> requiredConnectedOffenderBookings,
        Adjudication adjudication
    ) {
        var remainingVictimsStaffParties = new ArrayList<>(adjudication.getVictimsStaffParties());
        var remainingVictimsOffenderParties = new ArrayList<>(adjudication.getVictimsOffenderParties());
        var remainingConnectedOffendersParties = new ArrayList<>(adjudication.getConnectedOffenderParties());

        idsToRemove(requiredVictimStaff, adjudication.getVictimsStaff(), Staff::getStaffId)
            .forEach(staffId -> remove(remainingVictimsStaffParties, AdjudicationParty::staffId, staffId));

        idsToRemove(requiredVictimOffenderBookings, adjudication.getVictimsOffenderBookings(), OffenderBooking::getBookingId)
            .forEach(offenderBookingId -> remove(remainingVictimsOffenderParties, AdjudicationParty::offenderBookingId, offenderBookingId));

        idsToRemove(requiredConnectedOffenderBookings, adjudication.getConnectedOffenderBookings(), OffenderBooking::getBookingId)
            .forEach(offenderBookingId -> remove(remainingConnectedOffendersParties, AdjudicationParty::offenderBookingId, offenderBookingId));

        return List.of(remainingVictimsStaffParties, remainingVictimsOffenderParties, remainingConnectedOffendersParties).stream()
            .flatMap(Collection::stream).toList();
    }

    private Staff staffWithId(List<Staff> staff, Long id) {
        return staff.stream().filter(s -> id.equals(s.getStaffId())).findFirst().get();
    }

    private OffenderBooking offenderBookingWithId(List<OffenderBooking> offenderBookings, Long id) {
        return offenderBookings.stream().filter(o -> id.equals(o.getBookingId())).findFirst().get();
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

    private AdjudicationParty.AdjudicationPartyBuilder newAdjudicationParty(Adjudication adjudication, AtomicReference<Long> sequence, LocalDateTime currentDateTime) {
        sequence.set(sequence.get() + 1);
        return AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudication, sequence.get()))
            .partyAddedDate(currentDateTime.toLocalDate());
    }

    private <T> Set<Long> idsToAdd(List<T> required, List<T> current, Function<T, Long> toId) {
        return idsToAdd(required.stream().map(toId).toList(), current.stream().map(toId).toList());
    }

    private <T> Set<Long> idsToRemove(List<T> required, List<T> current, Function<T, Long> toId) {
        return idsToRemove(required.stream().map(toId).toList(), current.stream().map(toId).toList());
    }

    private Set<Long> idsToAdd(List<Long> desired, List<Long> current) {
        var toAdd = new HashSet<>(desired);
        toAdd.removeAll(current);
        return toAdd;
    }

    private Set<Long> idsToRemove(List<Long> desired, List<Long> current) {
        var toRemove = new HashSet<>(current);
        toRemove.removeAll(desired);
        return toRemove;
    }

    private <T> void remove(List<T> all, Function<T, Long> toId, Long id) {
        var toRemove = all.stream().filter(t -> id.equals(toId.apply(t))).toList();
        all.removeAll(toRemove);
    }
}
