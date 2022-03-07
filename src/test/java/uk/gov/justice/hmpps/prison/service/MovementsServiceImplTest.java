package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.repository.MovementsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRespository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovementsServiceImplTest {
    private static final String TEST_OFFENDER_NO = "AA1234A";
    @Mock
    private MovementsRepository movementsRepository;
    @Mock
    private ExternalMovementRepository externalMovementRepository;
    @Mock
    private CourtEventRepository courtEventRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private AgencyLocationRepository agencyLocationRepository;
    @Mock
    private ReferenceCodeRepository<MovementType> movementTypeRepository;
    @Mock
    private ReferenceCodeRepository<MovementReason> movementReasonRepository;
    @Mock
    private MovementTypeAndReasonRespository movementTypeAndReasonRespository;

    private MovementsService movementsService;

    @BeforeEach
    public void init() {
        movementsService = new MovementsService(
            movementsRepository,
            externalMovementRepository,
            courtEventRepository,
            agencyLocationRepository,
            movementTypeRepository,
            movementReasonRepository,
            offenderBookingRepository,
            movementTypeAndReasonRespository,
            1);
    }

    @Test
    public void testGetRecentMovements_ByOffenders() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, true, false)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true, false);
        assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn");
        assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds");

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true, false);
    }

    @Test
    public void testGetMovements_ByOffenders() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, false, false)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, false, false);
        assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn");
        assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds");

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, false, false);
    }

    @Test
    public void testGetMovements_ByOffendersNullDescriptions() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, true, false)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true, false);

        assertThat(processedMovements).hasSize(1);
        assertThat(processedMovements.get(0).getFromAgencyDescription()).isEmpty();

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true, false);
    }

    @Test
    public void testGetEnrouteOffenderMovements() {
        final List<OffenderMovement> oms = ImmutableList.of(OffenderMovement.builder()
            .offenderNo(TEST_OFFENDER_NO)
            .bookingId(123L).firstName("JAMES")
            .lastName("SMITH")
            .fromAgencyDescription("LEEDS")
            .toAgencyDescription("MOORLANDS")
            .build());

        when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12))).thenReturn(oms);

        final var enrouteOffenderMovements = movementsService.getEnrouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12));
        assertThat(enrouteOffenderMovements).extracting("fromAgencyDescription").contains("Leeds");
        assertThat(enrouteOffenderMovements).extracting("toAgencyDescription").contains("Moorlands");
        assertThat(enrouteOffenderMovements).extracting("lastName").contains("SMITH");
        assertThat(enrouteOffenderMovements).extracting("bookingId").contains(123L);

        verify(movementsRepository).getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12));
    }


    @Test
    public void testGetEnrouteOffender_NoDateFilter() {
        /* call service with no specified date */
        movementsService.getEnrouteOffenderMovements("LEI", null);

        verify(movementsRepository).getEnrouteMovementsOffenderMovementList("LEI", null);
    }

    @Test
    public void testGetEnrouteOffenderMovements_Count() {
        when(movementsRepository.getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12))).thenReturn(5);

        final var count = movementsService.getEnrouteOffenderCount("LEI", LocalDate.of(2015, 9, 12));
        assertThat(count).isEqualTo(5);

        verify(movementsRepository).getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12));
    }

    @Test
    public void testGetOffenders_OutToday() {
        final var timeOut = LocalTime.now();
        final List<OffenderMovement> singleOffender = ImmutableList.of(
            OffenderMovement.builder()
                .offenderNo("1234")
                .directionCode("OUT")
                .dateOfBirth(LocalDate.now())
                .movementDate(LocalDate.now())
                .fromAgency("LEI")
                .firstName("JOHN")
                .lastName("DOE")
                .movementReasonDescription("NORMAL TRANSFER")
                .movementTime(timeOut)
                .build());

        when(movementsRepository.getOffendersOut("LEI", LocalDate.now(), null)).thenReturn(singleOffender);

        assertThat(movementsService.getOffendersOut("LEI", LocalDate.now(), null)).containsExactly(
            OffenderOutTodayDto.builder()
                .offenderNo("1234")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.now())
                .timeOut(timeOut)
                .reasonDescription("Normal Transfer")
                .build());
    }

    @Test
    public void testGetOffenders_OutTodayByMovementType() {
        final var timeOut = LocalTime.now();

        final List<OffenderMovement> singleOffender = ImmutableList.of(
            OffenderMovement.builder()
                .offenderNo("1234")
                .directionCode("OUT")
                .dateOfBirth(LocalDate.now())
                .movementDate(LocalDate.now())
                .fromAgency("LEI")
                .firstName("JOHN")
                .lastName("DOE")
                .movementReasonDescription("NORMAL TRANSFER")
                .movementTime(timeOut)
                .build());

        when(movementsRepository.getOffendersOut("LEI", LocalDate.now(), "REL")).thenReturn(singleOffender);

        assertThat(movementsService.getOffendersOut("LEI", LocalDate.now(), "rel")).containsExactly(
            OffenderOutTodayDto.builder()
                .offenderNo("1234")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.now())
                .timeOut(timeOut)
                .reasonDescription("Normal Transfer")
                .build());
    }

    @Test
    public void testMapping_ToProperCase() {
        final var agency = "LEI";

        when(movementsRepository.getOffendersInReception(agency))
            .thenReturn(
                Collections.singletonList(OffenderInReception.builder()
                    .firstName("FIRST")
                    .lastName("LASTNAME")
                    .dateOfBirth(LocalDate.of(1950, 10, 10))
                    .offenderNo("1234A")
                    .build()
                )
            );

        final var offenders = movementsService.getOffendersInReception(agency);

        assertThat(offenders)
            .containsExactly(OffenderInReception.builder()
                .firstName("First")
                .lastName("Lastname")
                .dateOfBirth(LocalDate.of(1950, 10, 10))
                .offenderNo("1234A")
                .build());
    }

    @Test
    public void testMappingToProperCase_CurrentlyOut() {

        when(movementsRepository.getOffendersCurrentlyOut(1L))
            .thenReturn(
                Collections.singletonList(OffenderOut.builder()
                    .firstName("FIRST")
                    .lastName("LASTNAME")
                    .dateOfBirth(LocalDate.of(1950, 10, 10))
                    .offenderNo("1234A")
                    .location("x-1-1")
                    .build()
                )
            );

        final var offenders = movementsService.getOffendersCurrentlyOut(1L);

        assertThat(offenders)
            .containsExactly(OffenderOut.builder()
                .firstName("First")
                .lastName("Lastname")
                .dateOfBirth(LocalDate.of(1950, 10, 10))
                .offenderNo("1234A")
                .location("x-1-1")
                .build());
    }

    @Test
    public void testThatCallsToRecentMovements_AreBatched() {
        final var offenders = List.of("offender1", "offender2");
        final var movement1 = Movement.builder()
            .offenderNo("offender1")
            .fromAgencyDescription("Lei")
            .toAgencyDescription("York")
            .toCity("York")
            .fromCity("Leeds")
            .movementType("TRN")
            .movementReason("COURT")
            .build();
        final var movement2 = Movement.builder()
            .offenderNo("offender2")
            .fromAgencyDescription("Hli")
            .toAgencyDescription("York")
            .toCity("York")
            .fromCity("Hull")
            .movementType("TRN")
            .movementReason("COURT")
            .build();

        when(movementsRepository.getMovementsByOffenders(List.of("offender1"), Collections.emptyList(), true, false))
            .thenReturn(List.of(movement1));

        when(movementsRepository.getMovementsByOffenders(List.of("offender2"), Collections.emptyList(), true, false))
            .thenReturn(List.of(movement2));

        final var movements = movementsService.getMovementsByOffenders(offenders, Collections.emptyList(), true, false);

        assertThat(movements).containsSequence(List.of(movement1, movement2));

        verify(movementsRepository).getMovementsByOffenders(List.of("offender1"), Collections.emptyList(), true, false);
        verify(movementsRepository).getMovementsByOffenders(List.of("offender2"), Collections.emptyList(), true, false);
    }

    @Test
    public void getOffenderIn_verifyRetrieval() {

        when(externalMovementRepository.findMovements(any(), anyBoolean(), any(), any(), any(), any()))
            .thenReturn(
                new PageImpl<>(Collections.singletonList(ExternalMovement.builder()
                    .movementTime(LocalDateTime.of(2020, 1, 30, 12, 30))
                    .offenderBooking(OffenderBooking.builder()
                        .bookingId(-1L)
                        .assignedLivingUnit(AgencyInternalLocation.builder().description("INSIDE").build())
                        .offender(Offender.builder()
                            .nomsId("A1234AA")
                            .firstName("BOB")
                            .middleName("JOHN")
                            .lastName("SMITH")
                            .birthDate(LocalDate.of(2001, 1, 2))
                            .build())
                        .build())
                    .active(true)
                    .fromCity(new City("CIT-1", "City 1"))
                    .toCity(new City("CIT-2", "City 2"))
                    .toAgency(AgencyLocation.builder()
                        .id("LEI")
                        .description("LEEDS")
                        .build())
                    .fromAgency(AgencyLocation.builder()
                        .id("MDI")
                        .description("MOORLAND")
                        .build()
                    ).build())));

        final var offenders = movementsService.getOffendersIn(
            "LEI",
            LocalDateTime.of(2020, 1, 2, 1, 2),
            LocalDateTime.of(2020, 2, 2, 1, 2),
            PageRequest.of(1, 2), false);

        assertThat(offenders).containsExactly(OffenderIn.builder()
            .offenderNo("A1234AA")
            .firstName("Bob")
            .middleName("John")
            .lastName("Smith")
            .bookingId(-1L)
            .dateOfBirth(LocalDate.of(2001, 1, 2))
            .movementDateTime(LocalDateTime.of(2020, 1, 30, 12, 30))
            .movementTime(LocalTime.of(12, 30))
            .fromCity("City 1")
            .toCity("City 2")
            .fromAgencyId("MDI")
            .fromAgencyDescription("MOORLAND")
            .toAgencyId("LEI")
            .toAgencyDescription("LEEDS")
            .location("INSIDE")
            .build());


        verify(externalMovementRepository).findMovements(
            "LEI",
            true,
            MovementDirection.IN,
            LocalDateTime.of(2020, 1, 2, 1, 2),
            LocalDateTime.of(2020, 2, 2, 1, 2),
            PageRequest.of(1, 2));
    }


    @Nested
    class CreateExternalMovementTests {
        final LocalDateTime NOW = LocalDateTime.parse("2021-01-01T21:00");

        final AgencyLocation PRISON = AgencyLocation.builder().id("MDI").description("Moorland (HMP & YOI)").build();
        final AgencyLocation HOSPITAL = AgencyLocation.builder().id("HAZLWD").description("Hazelwood House").build();
        final MovementType MOVEMENT_TYPE_RELEASE = new MovementType("REL", "Released");
        final MovementReason MOVEMENT_REASON_CR = new MovementReason("CR", "Conditional release");
        final MovementTypeAndReason VALID_MOVEMENT_TYPE_AND_REASON = MovementTypeAndReason.builder().type("REL").reasonCode("CR").build();

        final OffenderBooking OFFENDER_BOOKING = OffenderBooking.builder()
            .bookingId(1L)
            .active(false)
            .offender(Offender.builder()
                .nomsId("A12345")
                .firstName("Bob")
                .middleName("Good")
                .lastName("Doe")
                .birthDate(LocalDate.of(1980, 10, 10))
                .build())
            .build();

        final CreateExternalMovement CREATE_MOVEMENT = CreateExternalMovement.builder()
            .bookingId(1L)
            .movementTime(NOW)
            .toAgencyId("HAZLWD")
            .fromAgencyId("MDI")
            .directionCode(MovementDirection.OUT)
            .movementType("REL")
            .movementReason("CR")
            .build();

        @Test
        public void testBookingNotFound() {
            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("booking not found using 1");
        }

        @Test
        public void testMovementTypeNotFound() {
            when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));

            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("movementType not found using: REL");
        }

        @Test
        public void testMovementReasonNotFound() {
            when(movementTypeRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
            when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));

            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("movementReason not found using: CR");
        }

        @Test
        public void testInvalidMovementReasonForType() {
            when(movementTypeRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
            when(movementReasonRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_REASON_CR));
            when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));

            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("Invalid movement reason for supplied movement type");
        }

        @Test
        public void testFromAgencyNotFound() {
            when(movementTypeRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
            when(movementReasonRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_REASON_CR));
            when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));
            when(movementTypeAndReasonRespository.findMovementTypeAndReasonByTypeIs(any())).thenReturn(List.of(VALID_MOVEMENT_TYPE_AND_REASON));

            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("fromAgency not found using: MDI");
        }

        @Test
        public void testToAgencyNotFound() {
            when(agencyLocationRepository.findById("MDI")).thenReturn(Optional.of(AgencyLocation.builder().build()));
            when(movementTypeRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
            when(movementReasonRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_REASON_CR));
            when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));
            when(movementTypeAndReasonRespository.findMovementTypeAndReasonByTypeIs(any())).thenReturn(List.of(VALID_MOVEMENT_TYPE_AND_REASON));

            Throwable exception = assertThrows(EntityNotFoundException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

            assertThat(exception.getMessage()).isEqualTo("toAgency not found using: HAZLWD");
        }

        @Nested
        class RulesAroundMovements {
            @Test
            public void testStopReleaseMovementsForOffenders_currentlyInside() {
                when(movementTypeRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
                when(movementReasonRepository.findById(any())).thenReturn(Optional.of(MOVEMENT_REASON_CR));
                when(movementTypeAndReasonRespository.findMovementTypeAndReasonByTypeIs(any())).thenReturn(List.of(VALID_MOVEMENT_TYPE_AND_REASON));

                when(offenderBookingRepository.findById(anyLong()))
                    .thenReturn(Optional.of(OFFENDER_BOOKING
                        .toBuilder()
                        .active(true)
                        .bookingStatus("ACTIVE IN")
                        .build()));

                Throwable exception = assertThrows(IllegalStateException.class, () -> movementsService.createExternalMovement(1L, CREATE_MOVEMENT));

                assertThat(exception.getMessage()).isEqualTo("You can only create an external movement for inactive offenders");
            }
        }

        @Nested
        class ParametersAndMapping {
            @BeforeEach
            public void beforeEach() {
                when(agencyLocationRepository.findById("MDI")).thenReturn(Optional.of(PRISON));
                when(agencyLocationRepository.findById("HAZLWD")).thenReturn(Optional.of(HOSPITAL));
                when(movementTypeRepository.findById(MovementType.pk("REL"))).thenReturn(Optional.of(MOVEMENT_TYPE_RELEASE));
                when(movementReasonRepository.findById(MovementReason.pk("CR"))).thenReturn(Optional.of(MOVEMENT_REASON_CR));
                when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OFFENDER_BOOKING));
                when(movementTypeAndReasonRespository.findMovementTypeAndReasonByTypeIs(any())).thenReturn(List.of(VALID_MOVEMENT_TYPE_AND_REASON));
            }

            @Test
            public void testMapping() {
                final var movement = movementsService.createExternalMovement(1L, CREATE_MOVEMENT);

                assertThat(movement)
                    .extracting(
                        "offenderNo",
                        "bookingId",
                        "dateOfBirth",
                        "firstName",
                        "lastName",
                        "middleName",
                        "fromAgency",
                        "fromAgencyDescription",
                        "toAgency",
                        "toAgencyDescription",
                        "movementType",
                        "movementTypeDescription",
                        "movementReason",
                        "movementReasonDescription",
                        "directionCode",
                        "movementTime",
                        "movementDate")
                    .contains("A12345", 1L,
                        LocalDate.of(1980, 10, 10), "Bob", "Doe", "Good",
                        PRISON.getId(), PRISON.getDescription(),
                        HOSPITAL.getId(), HOSPITAL.getDescription(),
                        MOVEMENT_TYPE_RELEASE.getCode(),
                        MOVEMENT_TYPE_RELEASE.getDescription(),
                        MOVEMENT_REASON_CR.getCode(),
                        MOVEMENT_REASON_CR.getDescription(),
                        MovementDirection.OUT.toString(),
                        NOW.toLocalTime(),
                        NOW.toLocalDate());

            }
        }
    }

    @Nested
    public class GetTransferMovementsForAgencies {

        @Test
        public void testScheduledEventsAreReturnedCorrectly() {

            final var from = LocalDateTime.parse("2019-05-01T11:00:00");
            final var to = LocalDateTime.parse("2019-05-01T17:00:00");
            final var agencyList = List.of("LEI", "MDI");
            final var now = LocalDate.now().atStartOfDay();

            final var listOfMovements = List.of(
                MovementSummary.builder().offenderNo("1111").movementType("TRN").movementTime(now).fromAgency("LEI").fromAgencyDescription("Leeds (HMP)").toAgency("MDI").toAgencyDescription("MOORLAND (HMP)").movementReason("Court").build(),
                MovementSummary.builder().offenderNo("2222").movementType("TRN").movementTime(now).fromAgency("MDI").fromAgencyDescription("Moorland (HMP)").toAgency("LEI").toAgencyDescription("Leeds (HMP)").movementReason("Transfer").build(),
                MovementSummary.builder().offenderNo("4333").movementType("TRN").movementTime(now).fromAgency("MD").fromAgencyDescription("MIDLANDS (HMP)").toAgency("HOW").toAgencyDescription("Howden").movementReason("Transfer").build()
            );

            final var listOfCourtEvents = List.of(
                CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(now).fromAgency("LEI").fromAgencyDescription("LEEDS (HMP)").toAgency("MDI").toAgencyDescription("MOORLAND (HMP)").build()
            );

            final var listOfReleaseEvents = List.of(
                ReleaseEvent.builder().offenderNo("6666").movementTypeCode("REL").createDateTime(now).fromAgency("LEI").fromAgencyDescription("LEEDS (HMP)").build()
            );


            when(movementsRepository.getCompletedMovementsForAgencies(agencyList, from, to)).thenReturn(listOfMovements);
            when(movementsRepository.getCourtEvents(agencyList, from, to)).thenReturn(listOfCourtEvents);
            when(movementsRepository.getOffenderReleases(agencyList, from, to)).thenReturn(listOfReleaseEvents);

            final var courtEvents = true;
            final var releaseEvents = true;
            final var transferEvents = false;
            final var movements = true;

            final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);

            assertThat(transferSummary).isNotNull();

            assertThat(transferSummary.getCourtEvents())
                .extracting("offenderNo", "eventType", "startTime", "fromAgency", "fromAgencyDescription", "toAgency", "toAgencyDescription")
                .contains(Tuple.tuple("5555", "CRT", now, "LEI", "Leeds (HMP)", "MDI", "Moorland (HMP)"));


            assertThat(transferSummary.getReleaseEvents())
                .extracting("offenderNo", "movementTypeCode", "createDateTime", "fromAgency", "fromAgencyDescription")
                .contains(Tuple.tuple("6666", "REL", now, "LEI", "Leeds (HMP)"));

            assertThat(transferSummary.getMovements())
                .extracting("offenderNo", "movementType", "movementTime", "fromAgency", "fromAgencyDescription", "toAgency", "toAgencyDescription", "movementReason")
                .containsExactlyInAnyOrder(
                    Tuple.tuple("1111", "TRN", now, "LEI", "Leeds (HMP)", "MDI", "Moorland (HMP)", "Court"),
                    Tuple.tuple("2222", "TRN", now, "MDI", "Moorland (HMP)", "LEI", "Leeds (HMP)", "Transfer"),
                    Tuple.tuple("4333", "TRN", now, "MD", "Midlands (HMP)", "HOW", "Howden", "Transfer")
                );

            verify(movementsRepository).getCompletedMovementsForAgencies(agencyList, from, to);
            verify(movementsRepository).getCourtEvents(agencyList, from, to);
            verify(movementsRepository).getOffenderReleases(agencyList, from, to);

            verifyNoMoreInteractions(movementsRepository);
        }

        @Test
        public void testAgencyEventsCombinationQuery() {

            final var from = LocalDateTime.parse("2019-05-01T11:00:00");
            final var to = LocalDateTime.parse("2019-05-01T17:00:00");
            final var agencyList = List.of("LEI", "MDI");

            final var listOfCourtEvents = List.of(
                CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(LocalDateTime.now()).build()
            );

            final var listOfTransferEvents = List.of(
                TransferEvent.builder().offenderNo("7777").fromAgency("MDI").startTime(from).endTime(to).eventClass("EXT_MOV").eventStatus("SCH").createDateTime(LocalDateTime.now()).build()
            );


            final var courtEvents = true;
            final var releaseEvents = false;
            final var transferEvents = true;
            final var movements = false;

            when(movementsRepository.getCourtEvents(agencyList, from, to)).thenReturn(listOfCourtEvents);
            when(movementsRepository.getIndividualSchedules(agencyList, from.toLocalDate())).thenReturn(listOfTransferEvents);

            final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);

            assertThat(transferSummary).isNotNull();

            assertThat(transferSummary.getCourtEvents()).containsAll(listOfCourtEvents);
            assertThat(transferSummary.getReleaseEvents()).isNullOrEmpty();
            assertThat(transferSummary.getTransferEvents()).containsAll(listOfTransferEvents);
            assertThat(transferSummary.getMovements()).isNullOrEmpty();

            verify(movementsRepository).getCourtEvents(agencyList, from, to);
            verify(movementsRepository).getIndividualSchedules(agencyList, from.toLocalDate());

            verifyNoMoreInteractions(movementsRepository);
        }

        @Test
        public void testAgencyEventsNoQueryParameters() {

            // Valid date range
            final var from = LocalDateTime.parse("2019-05-01T11:00:00");
            final var to = LocalDateTime.parse("2019-05-01T17:00:00");
            final var agencyList = List.of("LEI", "MDI");

            // All false - no data is being requested
            final var courtEvents = false;
            final var releaseEvents = false;
            final var transferEvents = false;
            final var movements = false;

            assertThatThrownBy(() ->
                movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements)
            ).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("At least one query parameter must be true [courtEvents|releaseEvents|transferEvents|movements]");

            verifyNoMoreInteractions(movementsRepository);
        }

        @Nested
        public class ParameterChecks {
            @Test
            public void invalidDateRange() {
                // From time is AFTER the to time
                final var from = LocalDateTime.parse("2019-05-01T17:00:00");
                final var to = LocalDateTime.parse("2019-05-01T11:00:00");
                final var agencyList = List.of("LEI", "MDI");

                final var courtEvents = true;
                final var releaseEvents = true;
                final var transferEvents = true;
                final var movements = true;

                assertThatThrownBy(() ->
                    movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements)
                ).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("The supplied fromDateTime parameter is after the toDateTime value");

                verifyNoMoreInteractions(movementsRepository);
            }

            @Test
            public void dateRangeIsGreaterThan24Hours() {
                final var from = LocalDateTime.now();
                final var to = LocalDateTime.now().plusDays(1);

                final var agencyList = List.of("LEI");
                final var courtEvents = true;
                final var releaseEvents = true;
                final var transferEvents = true;
                final var movements = true;

                assertThatThrownBy(() ->
                    movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements)
                ).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("400 The supplied time period is more than 24 hours - limit to 24 hours maximum");

                verifyNoMoreInteractions(movementsRepository);
            }

            @Test
            public void noAgencyCodes() {
                // No agency identifiers provided
                final var from = LocalDateTime.parse("2019-05-01T11:00:00");
                final var to = LocalDateTime.parse("2019-05-01T17:00:00");
                final var agencyList = Collections.<String>emptyList();

                final var courtEvents = true;
                final var releaseEvents = true;
                final var transferEvents = true;
                final var movements = true;

                assertThatThrownBy(() ->
                    movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements)
                ).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("No agency location identifiers were supplied");

                verifyNoMoreInteractions(movementsRepository);
            }

            @Test
            public void atLeastOneIsTrue() {
                final var from = LocalDateTime.parse("2019-05-01T11:00:00");
                final var to = LocalDateTime.parse("2019-05-01T17:00:00");
                final var agencyList = List.of("LEI");

                assertThatThrownBy(() ->
                    movementsService.getTransferMovementsForAgencies(agencyList, from, to, false, false, false, false)
                ).isInstanceOf(HttpClientErrorException.class)
                    .hasMessageContaining("400 At least one query parameter must be true [courtEvents|releaseEvents|transferEvents|movements]");

                verifyNoMoreInteractions(movementsRepository);
            }
        }

        @Nested
        public class GetTransfers {
            @Test
            @DisplayName("makes two calls to get transfers by date, one call for each date when the fromDateTime and toDateTime span across different days")
            public void makesTwoCallsToTheRepository() {
                final var todayAtMidnight = LocalDate.now().atTime(LocalTime.MIDNIGHT);
                final var tomorrowMorning = LocalDate.now().plusDays(1).atStartOfDay();

                final var agencyList = List.of("LEI");
                movementsService.getTransferMovementsForAgencies(
                    agencyList,
                    todayAtMidnight,
                    tomorrowMorning,
                    false, false, true, false
                );

                verify(movementsRepository).getIndividualSchedules(agencyList, todayAtMidnight.toLocalDate());
                verify(movementsRepository).getIndividualSchedules(agencyList, tomorrowMorning.toLocalDate());
            }

            @Test
            public void makesASingleCallToTheRepository() {
                final var agencyList = List.of("LEI");
                movementsService.getTransferMovementsForAgencies(
                    agencyList,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    false, false, true, false
                );

                verify(movementsRepository, times(1)).getIndividualSchedules(any(), any());
                verify(movementsRepository).getIndividualSchedules(agencyList, LocalDate.now());
            }

            @Test
            public void returnSchedulesThatAreTransfersAnd_notDeleted() {
                final var startDateTime = LocalDateTime.now();
                final var endDateTime = LocalDateTime.now();

                when(movementsRepository.getIndividualSchedules(any(), any())).thenReturn(List.of(
                    makeTransfer("A12345", "SCH", "LEI", "MDI", startDateTime, endDateTime),
                    makeTransfer("A12346", "DEL", "MDI", "LEI", startDateTime, endDateTime),
                    makeInternalMovement("A12347")
                ));

                final var transfers = movementsService.getTransferMovementsForAgencies(
                    List.of("LEI", "MDI"),
                    startDateTime,
                    endDateTime,
                    false, false, true, false
                );

                assertThat(transfers.getTransferEvents()).hasSize(1);
                assertThat(transfers.getTransferEvents())
                    .extracting("offenderNo", "fromAgency", "toAgency")
                    .contains(Tuple.tuple("A12345", "LEI", "MDI"));
            }

            @Test
            public void returnScheduledTransfer_forTheTimePeriod() {
                final var startDateTime = LocalDateTime.now();
                final var endDateTime = LocalDateTime.now().plusMinutes(5);

                when(movementsRepository.getIndividualSchedules(any(), any())).thenReturn(List.of(
                    makeTransfer("A12345", "SCH", "LEI", "MDI", startDateTime, endDateTime),
                    makeTransfer("A12346", "SCH", "WFI", "WX", startDateTime.plusMinutes(6), endDateTime.plusHours(8)),
                    makeInternalMovement("A12347")
                ));

                final var transfers = movementsService.getTransferMovementsForAgencies(
                    List.of("WX", "LEI"),
                    startDateTime,
                    endDateTime,
                    false, false, true, false
                );

                assertThat(transfers.getTransferEvents()).hasSize(1);
                assertThat(transfers.getTransferEvents())
                    .extracting("offenderNo", "fromAgency", "toAgency")
                    .contains(Tuple.tuple("A12345", "LEI", "MDI"));
            }

            @Test
            public void returnScheduledTransfers_agencyDescriptionsFormattedCorrectly() {
                final var startDateTime = LocalDateTime.now();
                final var endDateTime = LocalDateTime.now();

                when(movementsRepository.getIndividualSchedules(any(), any())).thenReturn(List.of(
                    makeTransfer("A12345", "SCH", "LEI", "MDI", startDateTime, endDateTime),
                    makeInternalMovement("A12347")
                ));

                final var transfers = movementsService.getTransferMovementsForAgencies(
                    List.of("WX", "LEI"),
                    startDateTime,
                    endDateTime,
                    false, false, true, false
                );

                assertThat(transfers.getTransferEvents()).hasSize(1);
                assertThat(transfers.getTransferEvents())
                    .extracting("offenderNo", "fromAgency", "fromAgencyDescription", "toAgency", "toAgencyDescription")
                    .contains(Tuple.tuple("A12345", "LEI", "Leeds (HMP)", "MDI", "Moorland (HMP & YOI)"));
            }

            private TransferEvent makeTransfer(final String offenderNo, final String eventStatus, final String fromAgencyId,
                                               final String toAgencyId, final LocalDateTime startDateTime, final LocalDateTime endDateTime) {

                final var agencyDescriptionMap = Map.of("LEI", "LEEDS (HMP)", "MDI", "MOORLAND (HMP & YOI)");

                return TransferEvent.builder()
                    .offenderNo(offenderNo)
                    .eventType("TRN")
                    .eventSubType("NOTR")
                    .eventClass("EXT_MOV")
                    .toAgency(toAgencyId)
                    .toAgencyDescription(agencyDescriptionMap.get(toAgencyId))
                    .fromAgency(fromAgencyId)
                    .fromAgencyDescription(agencyDescriptionMap.get(fromAgencyId))
                    .eventStatus(eventStatus)
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .build();
            }

            private TransferEvent makeInternalMovement(final String offenderNo) {
                return TransferEvent.builder()
                    .offenderNo(offenderNo)
                    .eventType("MOV")
                    .eventClass("INT_MOV")
                    .eventStatus("SCH")
                    .build();
            }
        }
    }

}
