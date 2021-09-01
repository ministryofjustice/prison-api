package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.config.DataComplianceProperties;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult.DeceasedOffender;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderBookingPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.DeceasedOffenderPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.List.of;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeceasedOffenderDeletionService {

    public static final String DECEASED = "DEC";

    private final DataComplianceProperties properties;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final TelemetryClient telemetryClient;
    private final MovementsService movementsService;
    private final DeceasedOffenderPendingDeletionRepository deceasedOffenderPendingDeletionRepository;
    private final Clock clock;


    public void deleteDeceasedOffenders(final Long batchId, final Pageable pageable) {

        checkState(properties.isDeceasedDeletionEnabled(), "Deceased deletion is not enabled!");

        var resultBuilder = DeceasedOffenderDeletionResult.builder().batchId(batchId);
        var telemetryLog = new ArrayList<Map>();

        offenderDeletionRepository.setContext(AppModuleName.MERGE);

        deceasedOffenderPendingDeletionRepository.findDeceasedOffendersDueForDeletion(LocalDate.now(clock), pageable).stream()
            .forEach(offenderPendingDeletion -> {


                final var offenderNumber = offenderPendingDeletion.getOffenderNumber();
                final var offenderAliases = getOffenderAliases(offenderNumber);
                final var rootOffenderAlias = toRootOffender(offenderNumber, offenderAliases);

                final var offenderIds = offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNumber);

                resultBuilder.deceasedOffender(transform(offenderNumber, rootOffenderAlias, offenderAliases, getDeceasedMovement(offenderNumber)));
                telemetryLog.add(Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())));

            });

        offenderDeletionRepository.setContext(AppModuleName.PRISON_API);

        dataComplianceEventPusher.send(resultBuilder.build());
        telemetryLog.forEach(map -> telemetryClient.trackEvent("DeceasedOffenderDelete", map, null));
    }

    private OffenderAliasPendingDeletion toRootOffender(final String offenderNumber, final List<OffenderAliasPendingDeletion> offenderAliases) {
        return offenderAliases.stream()
            .filter(alias -> Objects.equals(alias.getOffenderId(), alias.getRootOffenderId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(format("Cannot find root offender alias for '%s'", offenderNumber)));
    }

    private List<OffenderAliasPendingDeletion> getOffenderAliases(final String offenderNumber) {
        final var offenderAliases = offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber);
        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNumber);
        return offenderAliases;
    }

    private DeceasedOffender transform(final String offenderNumber,
                                       final OffenderAliasPendingDeletion rootOffenderAlias,
                                       final Collection<OffenderAliasPendingDeletion> offenderAliases,
                                       final Movement movement) {
        final var offenderBuilder = DeceasedOffender.builder();
        offenderBuilder
            .offenderIdDisplay(offenderNumber)
            .firstName(rootOffenderAlias.getFirstName())
            .middleName(rootOffenderAlias.getMiddleName())
            .lastName(rootOffenderAlias.getLastName())
            .birthDate(rootOffenderAlias.getBirthDate())
            .deletionDateTime(LocalDateTime.now(clock))
            .offenderAliases(offenderAliases.stream()
                .map(this::transform)
                .collect(toUnmodifiableList()));

        if (movement != null) {
            offenderBuilder
                .agencyLocationId(movement.getFromAgency())
                .deceasedDate(movement.getMovementDate());
        }

        return offenderBuilder.build();
    }

    private DeceasedOffenderDeletionResult.OffenderAlias transform(final OffenderAliasPendingDeletion alias) {
        return OffenderAlias.builder()
            .offenderId(alias.getOffenderId())
            .offenderBookIds(alias.getOffenderBookings().stream()
                .map(OffenderBookingPendingDeletion::getBookingId)
                .collect(toUnmodifiableList()))
            .build();
    }

    private Movement getDeceasedMovement(final String offenderNumber) {
        return movementsService.getMovementsByOffenders(of(offenderNumber), of(DECEASED), true, true)
            .stream()
            .findFirst()
            .orElse(null);
    }


}
