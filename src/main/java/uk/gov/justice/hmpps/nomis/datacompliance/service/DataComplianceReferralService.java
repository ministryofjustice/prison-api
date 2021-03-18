package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.ProvisionalDeletionReferralResult;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAlertPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderChargePendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DataComplianceReferralService {

    public static final String RELEASED = "REL";

    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final MovementsService movementsService;
    private final Clock clock;

    public void referOffendersForDeletion(final Long batchId,
                                          final LocalDate from,
                                          final LocalDate to,
                                          final Pageable pageable) {

        final var offenderNumbers = getOffendersPendingDeletion(from, to, pageable);

        offenderNumbers.forEach(offenderNo -> dataComplianceEventPusher.send(
                generateOffenderPendingDeletionEvent(offenderNo, batchId)));

        dataComplianceEventPusher.send(OffenderPendingDeletionReferralComplete.builder()
                .batchId(batchId)
                .numberReferred((long) offenderNumbers.getNumberOfElements())
                .totalInWindow(offenderNumbers.getTotalElements())
                .build());
    }

    public void referAdHocOffenderDeletion(final String offenderNumber, final Long batchId) {

        final var offenderPendingDeletion =
                offenderPendingDeletionRepository.findOffenderPendingDeletion(offenderNumber, LocalDate.now(clock))
                        .map(this::transform);

        checkState(offenderPendingDeletion.isPresent(),
                "Unable to find offender that qualifies for deletion with number: '%s'", offenderNumber);

        dataComplianceEventPusher.send(generateOffenderPendingDeletionEvent(offenderPendingDeletion.get(), batchId));
    }

    public void referProvisionalDeletion(final String offenderNumber, final Long referralId) {

        final var offenderPendingDeletion =
            offenderPendingDeletionRepository.findOffenderPendingDeletion(offenderNumber, LocalDate.now(clock))
                .map(this::transform);

        final var provisionalDeletionReferralResult = offenderPendingDeletion.isPresent()
            ? generateProvisionalDeletionReferralResultEvent(offenderNumber, referralId) : ProvisionalDeletionReferralResult.changesIdentifiedResult(offenderNumber, referralId);

        dataComplianceEventPusher.send(provisionalDeletionReferralResult);
    }

    private OffenderPendingDeletion generateOffenderPendingDeletionEvent(final OffenderNumber offenderNumber,
                                                                         final Long batchId) {

        final var offenderAliases = offenderAliasPendingDeletionRepository
                .findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber.getOffenderNumber());

        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNumber.getOffenderNumber());

        final var rootOffenderAlias = offenderAliases.stream()
                .filter(alias -> Objects.equals(alias.getOffenderId(), alias.getRootOffenderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        format("Cannot find root offender alias for '%s'", offenderNumber.getOffenderNumber())));

        return transform(offenderNumber, rootOffenderAlias, offenderAliases, batchId, getLatestLocationId(rootOffenderAlias.getOffenderNumber()));
    }

    private ProvisionalDeletionReferralResult generateProvisionalDeletionReferralResultEvent(final String offenderNo, Long referralId){

        final var offenderAliases = offenderAliasPendingDeletionRepository
            .findOffenderAliasPendingDeletionByOffenderNumber(offenderNo);

        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNo);

        return transform(offenderNo, offenderAliases, referralId);
    }

    private ProvisionalDeletionReferralResult transform(final String offenderNumber, final List<OffenderAliasPendingDeletion> offenderAliases, Long referralId) {

        return ProvisionalDeletionReferralResult.builder()
            .referralId(referralId)
            .offenderIdDisplay(offenderNumber)
            .subsequentChangesIdentified(false)
            .agencyLocationId(getLatestLocationId(offenderNumber))
            .offenceCodes(getOffenceCodes(offenderAliases))
            .alertCodes(getAlertCodes(offenderAliases))
            .build();
    }


    private Page<OffenderNumber> getOffendersPendingDeletion(final LocalDate from,
                                                             final LocalDate to,
                                                             final Pageable pageable) {
        return offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(from, to, pageable)
                .map(this::transform);
    }

    private OffenderNumber transform(final uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion entity) {

        return OffenderNumber.builder()
                .offenderNumber(entity.getOffenderNumber())
                .build();
    }

    private OffenderPendingDeletion transform(final OffenderNumber offenderNumber,
                                              final OffenderAliasPendingDeletion rootOffenderAlias,
                                              final Collection<OffenderAliasPendingDeletion> offenderAliases,
                                              final Long batchId,
                                              final String agencyLocationId) {
        return OffenderPendingDeletion.builder()
                .offenderIdDisplay(offenderNumber.getOffenderNumber())
                .batchId(batchId)
                .firstName(rootOffenderAlias.getFirstName())
                .middleName(rootOffenderAlias.getMiddleName())
                .lastName(rootOffenderAlias.getLastName())
                .birthDate(rootOffenderAlias.getBirthDate())
                .agencyLocationId(agencyLocationId)
                .offenderAliases(offenderAliases.stream()
                        .map(this::transform)
                        .collect(toUnmodifiableList()))
                .build();
    }

    private OffenderAlias transform(final OffenderAliasPendingDeletion alias) {
        return OffenderAlias.builder()
                .offenderId(alias.getOffenderId())
                .bookings(alias.getOffenderBookings().stream()
                        .map(booking -> Booking.builder()
                                .offenderBookId(booking.getBookingId())
                                .offenceCodes(booking.getOffenderCharges().stream()
                                        .map(OffenderChargePendingDeletion::getOffenceCode)
                                        .collect(toSet()))
                                .alertCodes(booking.getOffenderAlerts().stream()
                                        .map(OffenderAlertPendingDeletion::getAlertCode)
                                        .collect(toSet()))
                                .build())
                        .collect(toUnmodifiableList()))
                .build();
    }

    private String getLatestLocationId(String offenderNumber) {
         return movementsService.getMovementsByOffenders(of(offenderNumber), of(RELEASED), true, true)
             .stream()
             .findFirst()
             .map(Movement::getFromAgency)
             .orElse(null);
    }

    private List<String> getOffenceCodes(final List<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
            .map(OffenderAliasPendingDeletion::getOffenderBookings)
            .flatMap(Collection::stream)
            .map(OffenderBookingPendingDeletion::getOffenderCharges)
            .flatMap(Collection::stream)
            .map(OffenderChargePendingDeletion::getOffenceCode)
            .collect(toList());
    }

    private List<String> getAlertCodes(final List<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
            .map(OffenderAliasPendingDeletion::getOffenderBookings)
            .flatMap(Collection::stream)
            .map(OffenderBookingPendingDeletion::getOffenderAlerts)
            .flatMap(Collection::stream)
            .map(OffenderAlertPendingDeletion::getAlertCode)
            .collect(toList());
    }

}
