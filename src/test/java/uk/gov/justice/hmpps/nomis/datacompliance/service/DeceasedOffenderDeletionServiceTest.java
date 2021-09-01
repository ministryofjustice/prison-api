package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.SQLWarningException;
import uk.gov.justice.hmpps.nomis.datacompliance.config.DataComplianceProperties;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult.DeceasedOffender;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.*;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.DeceasedOffenderPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.sql.SQLWarning;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeceasedOffenderDeletionServiceTest {

    private static final String OFFENDER_NUMBER_1 = "A1234AA";
    private static final String OFFENDER_NUMBER_2 = "B4321BB";
    private static final Long OFFENDER_ID_1 = 2L;
    private static final Long OFFENDER_ID_2 = 9L;
    private static final Long BATCH_ID = 786L;

    @Mock
    private OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;

    @Mock
    private OffenderDeletionRepository offenderDeletionRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private MovementsService movementsService;

    @Mock
    private DeceasedOffenderPendingDeletionRepository deceasedOffenderPendingDeletionRepository;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());
    private final LocalDateTime now = LocalDateTime.now(clock);

    private DeceasedOffenderDeletionService service;


    @BeforeEach
    public void setUp() {

        service = new DeceasedOffenderDeletionService(
            new DataComplianceProperties(false, true),
            offenderAliasPendingDeletionRepository,
            offenderDeletionRepository,
            dataComplianceEventPusher,
            telemetryClient,
            movementsService,
            deceasedOffenderPendingDeletionRepository,
            clock);
    }

    @Test
    public void deleteDeceasedOffenders() {

        when(deceasedOffenderPendingDeletionRepository.findDeceasedOffendersDueForDeletion(now.toLocalDate(), Pageable.ofSize(2))).thenReturn(getOffendersPendingDeletionPage());

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
            .thenReturn(List.of(offenderAliasPendingDeletion(OFFENDER_ID_1, OFFENDER_NUMBER_1)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_2))
            .thenReturn(List.of(offenderAliasPendingDeletion(OFFENDER_ID_2, OFFENDER_NUMBER_2)));

        when(offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(OFFENDER_NUMBER_1)).thenReturn(Set.of(OFFENDER_ID_1));
        when(offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(OFFENDER_NUMBER_2)).thenReturn(Set.of(OFFENDER_ID_2));

        when(movementsService.getMovementsByOffenders(List.of(OFFENDER_NUMBER_1), List.of("DEC"), true, true)).thenReturn(List.of(offendersLastMovement(OFFENDER_NUMBER_1)));
        when(movementsService.getMovementsByOffenders(List.of(OFFENDER_NUMBER_2), List.of("DEC"), true, true)).thenReturn(List.of(offendersLastMovement(OFFENDER_NUMBER_2)));

        service.deleteDeceasedOffenders(BATCH_ID, Pageable.ofSize(2));

        InOrder orderVerifier = inOrder(offenderDeletionRepository);

        orderVerifier.verify(offenderDeletionRepository).setContext(AppModuleName.MERGE);
        orderVerifier.verify(offenderDeletionRepository).setContext(AppModuleName.PRISON_API);

        verify(dataComplianceEventPusher).send(expectedDeceasedOffenderDeletionResultEvent(getExpectedDeceasedOffenders(true)));
        verify(telemetryClient).trackEvent("DeceasedOffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER_1, "count", "1"), null);
        verify(telemetryClient).trackEvent("DeceasedOffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER_2, "count", "1"), null);
    }


    @Test
    public void deleteDeceasedOffendersNoMovementsFound() {

        when(deceasedOffenderPendingDeletionRepository.findDeceasedOffendersDueForDeletion(now.toLocalDate(), Pageable.ofSize(2))).thenReturn(getOffendersPendingDeletionPage());

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
            .thenReturn(List.of(offenderAliasPendingDeletion(OFFENDER_ID_1, OFFENDER_NUMBER_1)));

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_2))
            .thenReturn(List.of(offenderAliasPendingDeletion(OFFENDER_ID_2, OFFENDER_NUMBER_2)));

        when(offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(OFFENDER_NUMBER_1)).thenReturn(Set.of(OFFENDER_ID_1));
        when(offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(OFFENDER_NUMBER_2)).thenReturn(Set.of(OFFENDER_ID_2));

        when(movementsService.getMovementsByOffenders(List.of(OFFENDER_NUMBER_1), List.of("DEC"), true, true)).thenReturn(Collections.emptyList());
        when(movementsService.getMovementsByOffenders(List.of(OFFENDER_NUMBER_2), List.of("DEC"), true, true)).thenReturn(Collections.emptyList());

        service.deleteDeceasedOffenders(BATCH_ID, Pageable.ofSize(2));

        InOrder orderVerifier = inOrder(offenderDeletionRepository);

        orderVerifier.verify(offenderDeletionRepository).setContext(AppModuleName.MERGE);
        orderVerifier.verify(offenderDeletionRepository).setContext(AppModuleName.PRISON_API);

        verify(dataComplianceEventPusher).send(expectedDeceasedOffenderDeletionResultEvent(getExpectedDeceasedOffenders(false)));
        verify(telemetryClient).trackEvent("DeceasedOffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER_1, "count", "1"), null);
        verify(telemetryClient).trackEvent("DeceasedOffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER_2, "count", "1"), null);
    }

    @Test
    public void deleteDeceasedOffendersThrowsWhenDeletionIsNotEnabled() {

        service = new DeceasedOffenderDeletionService(
            new DataComplianceProperties(false, false),
            offenderAliasPendingDeletionRepository,
            offenderDeletionRepository,
            dataComplianceEventPusher,
            telemetryClient,
            movementsService,
            deceasedOffenderPendingDeletionRepository,
            clock);

        assertThatThrownBy(() -> service.deleteDeceasedOffenders(BATCH_ID, Pageable.unpaged()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Deceased deletion is not enabled!");
    }


    @Test
    void deleteDeceasedOffenderThrowsWhenUnableToUpdateContext() {

        doThrow(new SQLWarningException("Some Exception", new SQLWarning("SQL warning"))).when(offenderDeletionRepository).setContext(AppModuleName.MERGE);

        assertThatThrownBy(() -> service.deleteDeceasedOffenders(BATCH_ID, Pageable.unpaged()))
            .isInstanceOf(SQLWarningException.class);
    }

    @Test
    public void deleteDeceasedOffenderThrowsWhenOffenderAliasesNotFound() {

        when(deceasedOffenderPendingDeletionRepository.findDeceasedOffendersDueForDeletion(now.toLocalDate(), Pageable.ofSize(2))).thenReturn(getOffendersPendingDeletionPage());

        when(offenderAliasPendingDeletionRepository.findOffenderAliasPendingDeletionByOffenderNumber(OFFENDER_NUMBER_1))
            .thenReturn(emptyList());

        assertThatThrownBy(() -> service.deleteDeceasedOffenders(BATCH_ID, Pageable.ofSize(2)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Offender not found: 'A1234AA'");
    }

    private List<ExpectedDeceasedOffender> getExpectedDeceasedOffenders(boolean hasMovement) {
        return List.of(new ExpectedDeceasedOffender(OFFENDER_ID_1, OFFENDER_NUMBER_1, hasMovement),
            new ExpectedDeceasedOffender(OFFENDER_ID_2, OFFENDER_NUMBER_2, hasMovement));
    }

    private OffenderAliasPendingDeletion offenderAliasPendingDeletion(final long offenderId, final String offenderNumber) {
        return OffenderAliasPendingDeletion.builder()
            .firstName("John" + offenderId)
            .middleName("Middle" + offenderId)
            .lastName("Smith" + offenderId)
            .birthDate(LocalDate.of(2020, 1, (int) offenderId))
            .offenderId(offenderId)
            .rootOffenderId(offenderId)
            .offenderNumber(offenderNumber)
            .offenderBooking(OffenderBookingPendingDeletion.builder()
                .bookingId(offenderId)
                .offenderCharge(OffenderChargePendingDeletion.builder().offenceCode("offence" + offenderId).build())
                .offenderAlert(OffenderAlertPendingDeletion.builder().alertCode("alert" + offenderId).build())
                .build())
            .build();
    }

    private Movement offendersLastMovement(final String offenderNo) {
        return Movement.builder()
            .offenderNo(offenderNo)
            .fromAgency("LEI" + offenderNo)
            .fromAgencyDescription("lei prison")
            .toAgency("OUT")
            .toAgencyDescription("out of prison")
            .movementDate(now.toLocalDate())
            .movementTime(now.toLocalTime())
            .commentText("Some comment text")
            .movementType("DEC")
            .build();
    }

    private DeceasedOffenderDeletionResult expectedDeceasedOffenderDeletionResultEvent(final List<ExpectedDeceasedOffender> expectedDeceasedOffenders) {
        return new DeceasedOffenderDeletionResult(BATCH_ID, expectedDeceasedOffenders.stream().map(this::expectedDeceasedOffender).collect(Collectors.toList()));
    }

    private DeceasedOffender expectedDeceasedOffender(ExpectedDeceasedOffender expectedDeceasedOffender) {
        return DeceasedOffender.builder()
            .offenderIdDisplay(expectedDeceasedOffender.getOffenderNumber())
            .firstName("John" + expectedDeceasedOffender.getOffenderId())
            .middleName("Middle" + expectedDeceasedOffender.getOffenderId())
            .lastName("Smith" + expectedDeceasedOffender.getOffenderId())
            .birthDate(LocalDate.of(2020, 1, (int) expectedDeceasedOffender.getOffenderId()))
            .agencyLocationId(expectedDeceasedOffender.hasMovement ? "LEI" + expectedDeceasedOffender.getOffenderNumber() : null)
            .deceasedDate(expectedDeceasedOffender.hasMovement ? now.toLocalDate() : null)
            .deletionDateTime(now)
            .offenderAlias(OffenderAlias.builder()
                .offenderId(expectedDeceasedOffender.getOffenderId())
                .offenderBookIds(List.of(expectedDeceasedOffender.getOffenderId()))
                .build())
            .build();
    }


    private PageImpl<OffenderPendingDeletion> getOffendersPendingDeletionPage() {
        return new PageImpl<>(List.of(
            new OffenderPendingDeletion(OFFENDER_NUMBER_1),
            new OffenderPendingDeletion(OFFENDER_NUMBER_2)),
            PageRequest.of(0, 2), 2);
    }

    @Data
    @RequiredArgsConstructor
    static class ExpectedDeceasedOffender {

        private final long offenderId;
        private final String offenderNumber;
        private final boolean hasMovement;
    }

}






