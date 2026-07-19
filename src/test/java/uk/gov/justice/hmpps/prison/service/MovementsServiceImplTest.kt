package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.hmpps.prison.api.model.CourtEvent
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement
import uk.gov.justice.hmpps.prison.api.model.Movement
import uk.gov.justice.hmpps.prison.api.model.OffenderIn
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception
import uk.gov.justice.hmpps.prison.api.model.OffenderLatestArrivalDate
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement
import uk.gov.justice.hmpps.prison.api.model.OffenderOut
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent
import uk.gov.justice.hmpps.prison.api.model.TransferEvent
import uk.gov.justice.hmpps.prison.repository.MovementsRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class MovementsServiceImplTest {
  private val movementsRepository: MovementsRepository = mock()
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val courtEventRepository: CourtEventRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val movementTypeRepository: ReferenceCodeRepository<MovementType?> = mock()
  private val movementTypeAndReasonRepository: MovementTypeAndReasonRepository = mock()
  private val entityManager: EntityManager = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val movementsService = MovementsService(
    movementsRepository,
    externalMovementRepository,
    courtEventRepository,
    agencyLocationRepository,
    movementTypeRepository,
    offenderBookingRepository,
    movementTypeAndReasonRepository,
    entityManager,
    1,
    telemetryClient,
  )

  @Test
  fun testGetRecentMovements_ByOffenders() {
    val movements = listOf(
      Movement.builder().offenderNo(
        TEST_OFFENDER_NO,
      ).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build(),
    )
    val offenderNoList = listOf(TEST_OFFENDER_NO)

    whenever(
      movementsRepository.getMovementsByOffenders(
        offenderNoList,
        null,
        true,
        false,
      ),
    ).thenReturn(movements)

    val processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true, false)
    assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn")
    assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds")

    verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true, false)
  }

  @Test
  fun testGetMovements_ByOffenders() {
    val movements = listOf(
      Movement.builder().offenderNo(
        TEST_OFFENDER_NO,
      ).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build(),
    )
    val offenderNoList = listOf(TEST_OFFENDER_NO)

    whenever(
      movementsRepository.getMovementsByOffenders(
        offenderNoList,
        null,
        false,
        false,
      ),
    ).thenReturn(movements)

    val processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, false, false)
    assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn")
    assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds")

    verify(movementsRepository)
      .getMovementsByOffenders(offenderNoList, null, false, false)
  }

  @Test
  fun testGetMovements_ByOffendersNullDescriptions() {
    val movements = listOf(
      Movement.builder().offenderNo(
        TEST_OFFENDER_NO,
      ).build(),
    )
    val offenderNoList = listOf(TEST_OFFENDER_NO)

    whenever(
      movementsRepository.getMovementsByOffenders(
        offenderNoList,
        null,
        true,
        false,
      ),
    ).thenReturn(movements)

    val processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true, false)

    assertThat(processedMovements).hasSize(1)
    assertThat(processedMovements.first().fromAgencyDescription).isEmpty()

    verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true, false)
  }

  @Test
  fun testGetEnRouteOffenderMovements() {
    val oms = listOf(
      OffenderMovement.builder()
        .offenderNo(TEST_OFFENDER_NO)
        .bookingId(123L).firstName("JAMES")
        .lastName("SMITH")
        .fromAgencyDescription("LEEDS")
        .toAgencyDescription("MOORLANDS")
        .build(),
    )

    whenever(
      movementsRepository.getEnrouteMovementsOffenderMovementList(
        "LEI",
        LocalDate.of(2015, 9, 12),
      ),
    ).thenReturn(oms)

    val enrouteOffenderMovements = movementsService.getEnRouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12))
    assertThat(enrouteOffenderMovements).extracting("fromAgencyDescription")
      .contains("Leeds")
    assertThat(enrouteOffenderMovements).extracting("toAgencyDescription")
      .contains("Moorlands")
    assertThat(enrouteOffenderMovements).extracting("lastName").contains("SMITH")
    assertThat(enrouteOffenderMovements).extracting("bookingId").contains(123L)

    verify(movementsRepository)
      .getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12))
  }

  @Test
  fun testGetEnrouteOffender_NoDateFilter() {
    /* call service with no specified date */
    movementsService.getEnRouteOffenderMovements("LEI", null)

    verify(movementsRepository).getEnrouteMovementsOffenderMovementList("LEI", null)
  }

  @Test
  fun testGetOffenders_OutToday() {
    val timeOut = LocalTime.now()
    val singleOffender = listOf(
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
        .build(),
    )

    whenever(movementsRepository.getOffendersOut("LEI", LocalDate.now(), null))
      .thenReturn(singleOffender)

    assertThat(movementsService.getOffendersOut("LEI", LocalDate.now(), null))
      .containsExactly(
        OffenderOutTodayDto.builder()
          .offenderNo("1234")
          .firstName("John")
          .lastName("Doe")
          .dateOfBirth(LocalDate.now())
          .timeOut(timeOut)
          .reasonDescription("Normal Transfer")
          .build(),
      )
  }

  @Test
  fun testGetOffenders_OutTodayByMovementType() {
    val timeOut = LocalTime.now()

    val singleOffender = listOf(
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
        .build(),
    )

    whenever(
      movementsRepository.getOffendersOut(
        "LEI",
        LocalDate.now(),
        "REL",
      ),
    ).thenReturn(singleOffender)

    assertThat(movementsService.getOffendersOut("LEI", LocalDate.now(), "rel"))
      .containsExactly(
        OffenderOutTodayDto.builder()
          .offenderNo("1234")
          .firstName("John")
          .lastName("Doe")
          .dateOfBirth(LocalDate.now())
          .timeOut(timeOut)
          .reasonDescription("Normal Transfer")
          .build(),
      )
  }

  @Test
  fun testMapping_ToProperCase() {
    val agency = "LEI"

    whenever(movementsRepository.getOffendersInReception(agency))
      .thenReturn(
        mutableListOf(
          OffenderInReception.builder()
            .firstName("FIRST")
            .lastName("LASTNAME")
            .dateOfBirth(LocalDate.of(1950, 10, 10))
            .offenderNo("1234A")
            .build(),
        ),
      )

    val offenders = movementsService.getOffendersInReception(agency)

    assertThat(offenders)
      .containsExactly(
        OffenderInReception.builder()
          .firstName("First")
          .lastName("Lastname")
          .dateOfBirth(LocalDate.of(1950, 10, 10))
          .offenderNo("1234A")
          .build(),
      )
  }

  @Test
  fun testMappingToProperCase_CurrentlyOut() {
    whenever(movementsRepository.getOffendersCurrentlyOut(1L))
      .thenReturn(
        mutableListOf(
          OffenderOut.builder()
            .firstName("FIRST")
            .lastName("LASTNAME")
            .dateOfBirth(LocalDate.of(1950, 10, 10))
            .offenderNo("1234A")
            .location("x-1-1")
            .build(),
        ),
      )

    val offenders = movementsService.getOffendersCurrentlyOut(1L)

    assertThat(offenders)
      .containsExactly(
        OffenderOut.builder()
          .firstName("First")
          .lastName("Lastname")
          .dateOfBirth(LocalDate.of(1950, 10, 10))
          .offenderNo("1234A")
          .location("x-1-1")
          .build(),
      )
  }

  @Test
  fun testThatCallsToRecentMovements_AreBatched() {
    val offenders = listOf("offender1", "offender2")
    val movement1 = Movement.builder()
      .offenderNo("offender1")
      .fromAgencyDescription("Lei")
      .toAgencyDescription("York")
      .toCity("York")
      .fromCity("Leeds")
      .movementType("TRN")
      .movementReason("COURT")
      .build()
    val movement2 = Movement.builder()
      .offenderNo("offender2")
      .fromAgencyDescription("Hli")
      .toAgencyDescription("York")
      .toCity("York")
      .fromCity("Hull")
      .movementType("TRN")
      .movementReason("COURT")
      .build()

    whenever(
      movementsRepository.getMovementsByOffenders(
        listOf("offender1"),
        listOf(),
        true,
        false,
      ),
    )
      .thenReturn(listOf(movement1))

    whenever(
      movementsRepository.getMovementsByOffenders(
        listOf("offender2"),
        listOf(),
        true,
        false,
      ),
    )
      .thenReturn(listOf(movement2))

    val movements = movementsService.getMovementsByOffenders(offenders, listOf(), true, false)

    assertThat(movements).containsSequence(listOf(movement1, movement2))

    verify(movementsRepository)
      .getMovementsByOffenders(listOf("offender1"), listOf(), true, false)
    verify(movementsRepository)
      .getMovementsByOffenders(listOf("offender2"), listOf(), true, false)
  }

  @Test
  fun getOffenderIn_verifyRetrieval() {
    whenever(
      externalMovementRepository.findMovements(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
      ),
    )
      .thenReturn(
        PageImpl(
          mutableListOf<ExternalMovement>(
            ExternalMovement.builder()
              .movementReason(
                MovementTypeAndReason(
                  MovementType("TAP", "Transfer"),
                  MovementReason.TRANSFER_VIA_TAP.code,
                  "MOVE_RSN",
                ),
              )
              .movementDate(LocalDate.of(2020, 1, 30))
              .movementTime(LocalDateTime.of(2020, 1, 30, 12, 30))
              .offenderBooking(
                OffenderBooking.builder()
                  .bookingId(-1L)
                  .assignedLivingUnit(AgencyInternalLocation.builder().description("INSIDE").build())
                  .offender(
                    Offender.builder()
                      .nomsId("A1234AA")
                      .firstName("BOB")
                      .middleName("JOHN")
                      .lastName("SMITH")
                      .birthDate(LocalDate.of(2001, 1, 2))
                      .build(),
                  )
                  .build(),
              )
              .active(true)
              .fromCity(City("CIT-1", "City 1"))
              .toCity(City("CIT-2", "City 2"))
              .toAgency(
                AgencyLocation.builder()
                  .id("LEI")
                  .description("LEEDS")
                  .build(),
              )
              .fromAgency(
                AgencyLocation.builder()
                  .id("MDI")
                  .description("MOORLAND")
                  .build(),
              ).build(),
          ),
        ),
      )

    val offenders = movementsService.getOffendersIn(
      "LEI",
      LocalDateTime.of(2020, 1, 2, 1, 2),
      LocalDateTime.of(2020, 2, 2, 1, 2),
      PageRequest.of(1, 2),
      false,
    )

    assertThat(offenders).containsExactly(
      OffenderIn(
        "A1234AA",
        -1L,
        LocalDate.of(2001, 1, 2),
        "Bob",
        "John",
        "Smith",
        "MDI",
        "MOORLAND",
        "LEI",
        "LEEDS",
        "City 1",
        "City 2",
        LocalTime.of(12, 30),
        LocalDateTime.of(2020, 1, 30, 12, 30),
        "INSIDE",
        "TAP",
        "MOVE_RSN",
        null,
        MovementReason.TRANSFER_VIA_TAP.code,
      ),
    )

    verify(externalMovementRepository).findMovements(
      "LEI",
      true,
      MovementDirection.IN,
      LocalDateTime.of(2020, 1, 2, 1, 2),
      LocalDateTime.of(2020, 2, 2, 1, 2),
      PageRequest.of(1, 2),
    )
  }

  private companion object {
    private const val TEST_OFFENDER_NO = "AA1234A"
    private val NOW: LocalDateTime = LocalDateTime.parse("2021-01-01T21:00")

    private val PRISON: AgencyLocation = AgencyLocation.builder().id("MDI").description("Moorland (HMP & YOI)").build()
    private val HOSPITAL: AgencyLocation = AgencyLocation.builder().id("HAZLWD").description("Hazelwood House").build()
    private val MOVEMENT_TYPE_RELEASE: MovementType = MovementType("REL", "Released")
    private val MOVEMENT_REASON_CR: MovementReason = MovementReason("CR", "Conditional release")
    private val VALID_MOVEMENT_TYPE_AND_REASON: MovementTypeAndReason =
      MovementTypeAndReason(MOVEMENT_TYPE_RELEASE, "CR", "Conditional release")
    private val OFFENDER_BOOKING: OffenderBooking = OffenderBooking.builder()
      .bookingId(1L)
      .active(false)
      .offender(
        Offender.builder()
          .nomsId("A12345")
          .firstName("Bob")
          .middleName("Good")
          .lastName("Doe")
          .birthDate(LocalDate.of(1980, 10, 10))
          .build(),
      )
      .build()

    private val CREATE_MOVEMENT: CreateExternalMovement = CreateExternalMovement.builder()
      .bookingId(1L)
      .movementTime(NOW)
      .toAgencyId("HAZLWD")
      .fromAgencyId("MDI")
      .directionCode(MovementDirection.OUT)
      .movementType("REL")
      .movementReason("CR")
      .build()
  }

  @Nested
  internal inner class CreateExternalMovementTests {

    @Test
    fun testBookingNotFound() {
      val exception: Throwable = assertThrows(
        EntityNotFoundException::class.java,
      ) { movementsService.createExternalMovement(1L, CREATE_MOVEMENT) }

      assertThat(exception.message).isEqualTo("booking not found using 1")
    }

    @Test
    fun testMovementReasonNotFound() {
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(
          Optional.of(OFFENDER_BOOKING),
        )

      val exception: Throwable = assertThrows(
        EntityNotFoundException::class.java,
      ) { movementsService.createExternalMovement(1L, CREATE_MOVEMENT) }

      assertThat(exception.message).isEqualTo("movementReason not found using type REL and reason CR")
    }

    @Test
    fun testFromAgencyNotFound() {
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(
          Optional.of(OFFENDER_BOOKING),
        )
      whenever(movementTypeAndReasonRepository.findById(any()))
        .thenReturn(
          Optional.of(VALID_MOVEMENT_TYPE_AND_REASON),
        )
      whenever(
        movementTypeAndReasonRepository.findMovementTypeAndReasonById_Type(
          any(),
        ),
      ).thenReturn(
        listOf(VALID_MOVEMENT_TYPE_AND_REASON),
      )

      val exception: Throwable = assertThrows(
        EntityNotFoundException::class.java,
      ) { movementsService.createExternalMovement(1L, CREATE_MOVEMENT) }

      assertThat(exception.message).isEqualTo("fromAgency not found using: MDI")
    }

    @Test
    fun testToAgencyNotFound() {
      whenever(agencyLocationRepository.findById("MDI"))
        .thenReturn(Optional.of(AgencyLocation.builder().build()))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(
          Optional.of(OFFENDER_BOOKING),
        )
      whenever(movementTypeAndReasonRepository.findById(any()))
        .thenReturn(
          Optional.of(VALID_MOVEMENT_TYPE_AND_REASON),
        )
      whenever(
        movementTypeAndReasonRepository.findMovementTypeAndReasonById_Type(
          any(),
        ),
      ).thenReturn(
        listOf(VALID_MOVEMENT_TYPE_AND_REASON),
      )

      val exception: Throwable = assertThrows(
        EntityNotFoundException::class.java,
      ) { movementsService.createExternalMovement(1L, CREATE_MOVEMENT) }

      assertThat(exception.message).isEqualTo("toAgency not found using: HAZLWD")
    }

    @Nested
    internal inner class RulesAroundMovements {
      @Test
      fun testStopReleaseMovementsForOffenders_currentlyInside() {
        whenever(movementTypeAndReasonRepository.findById(any()))
          .thenReturn(
            Optional.of(VALID_MOVEMENT_TYPE_AND_REASON),
          )
        whenever(
          movementTypeAndReasonRepository.findMovementTypeAndReasonById_Type(
            any(),
          ),
        ).thenReturn(
          listOf(VALID_MOVEMENT_TYPE_AND_REASON),
        )

        whenever(offenderBookingRepository.findById(anyLong()))
          .thenReturn(
            Optional.of(
              OFFENDER_BOOKING
                .toBuilder()
                .active(true)
                .bookingStatus("ACTIVE IN")
                .build(),
            ),
          )

        val exception: Throwable = assertThrows(
          IllegalStateException::class.java,
        ) { movementsService.createExternalMovement(1L, CREATE_MOVEMENT) }

        assertThat(exception.message)
          .isEqualTo("You can only create an external movement for inactive offenders")
      }
    }

    @Nested
    internal inner class ParametersAndMapping {
      @BeforeEach
      fun beforeEach() {
        whenever(agencyLocationRepository.findById("MDI")).thenReturn(
          Optional.of(PRISON),
        )
        whenever(agencyLocationRepository.findById("HAZLWD")).thenReturn(
          Optional.of(HOSPITAL),
        )
        whenever(offenderBookingRepository.findById(anyLong()))
          .thenReturn(
            Optional.of(OFFENDER_BOOKING),
          )
        whenever(movementTypeAndReasonRepository.findById(any()))
          .thenReturn(
            Optional.of(VALID_MOVEMENT_TYPE_AND_REASON),
          )
        whenever(
          movementTypeAndReasonRepository.findMovementTypeAndReasonById_Type(
            any(),
          ),
        ).thenReturn(
          listOf(VALID_MOVEMENT_TYPE_AND_REASON),
        )
      }

      @Test
      fun testMapping() {
        val movement = movementsService.createExternalMovement(1L, CREATE_MOVEMENT)

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
            "movementDate",
          )
          .contains(
            "A12345", 1L,
            LocalDate.of(1980, 10, 10), "Bob", "Doe", "Good",
            PRISON.id, PRISON.description,
            HOSPITAL.id, HOSPITAL.description,
            MOVEMENT_TYPE_RELEASE.code,
            MOVEMENT_TYPE_RELEASE.description,
            MOVEMENT_REASON_CR.code,
            MOVEMENT_REASON_CR.description,
            MovementDirection.OUT.toString(),
            NOW.toLocalTime(),
            NOW.toLocalDate(),
          )
      }

      @Test
      fun testSetPreviousMovementsToInactive() {
        val previousMovement = ExternalMovement.builder()
          .movementReason(
            MovementTypeAndReason(
              MovementType("TAP", "Transfer"),
              MovementReason.TRANSFER_VIA_TAP.code,
              "desc",
            ),
          )
          .movementDate(LocalDate.of(2020, 1, 30))
          .movementTime(LocalDateTime.of(2020, 1, 30, 12, 30))
          .active(true)
          .fromCity(City("CIT-1", "City 1"))
          .toCity(City("CIT-2", "City 2"))
          .toAgency(AgencyLocation.builder().id("LEI").description("LEEDS").build())
          .fromAgency(
            AgencyLocation.builder().id("MDI").description("MOORLAND").build(),
          ).build()
        OFFENDER_BOOKING.addExternalMovement(previousMovement)
        assertThat(previousMovement.isActive).isTrue()

        movementsService.createExternalMovement(1L, CREATE_MOVEMENT)
        assertThat(previousMovement.isActive).isFalse()
        verify(entityManager).flush()
      }
    }
  }

  @Nested
  inner class GetTransferMovementsForAgencies {
    @Test
    fun testScheduledEventsAreReturnedCorrectly() {
      val from = LocalDateTime.parse("2019-05-01T11:00:00")
      val agency = "LEI"
      val now = LocalDate.now().atStartOfDay()

      val listOfCourtEvents = listOf<CourtEvent?>(
        CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(now).fromAgency("LEI")
          .fromAgencyDescription("LEEDS (HMP)").toAgency("MDI").toAgencyDescription("MOORLAND (HMP)").build(),
      )

      val listOfReleaseEvents = listOf<ReleaseEvent?>(
        ReleaseEvent.builder().offenderNo("6666").movementTypeCode("REL").createDateTime(now).fromAgency("LEI")
          .fromAgencyDescription("LEEDS (HMP)").build(),
      )

      whenever(movementsRepository.getCourtEvents(agency, from.toLocalDate()))
        .thenReturn(listOfCourtEvents)
      whenever(movementsRepository.getOffenderReleases(agency, from.toLocalDate()))
        .thenReturn(listOfReleaseEvents)

      val transferSummary = movementsService.getTransferMovementsForAgencies(
        agency,
        from,
      )

      assertThat(transferSummary).isNotNull()

      assertThat(transferSummary.courtEvents)
        .extracting(
          "offenderNo",
          "eventType",
          "startTime",
          "fromAgency",
          "fromAgencyDescription",
          "toAgency",
          "toAgencyDescription",
        )
        .contains(Tuple.tuple("5555", "CRT", now, "LEI", "Leeds (HMP)", "MDI", "Moorland (HMP)"))

      assertThat(transferSummary.releaseEvents)
        .extracting("offenderNo", "movementTypeCode", "createDateTime", "fromAgency", "fromAgencyDescription")
        .contains(Tuple.tuple("6666", "REL", now, "LEI", "Leeds (HMP)"))

      assertThat(transferSummary.movements).isEmpty()

      verify(movementsRepository).getCourtEvents(agency, from.toLocalDate())
      verify(movementsRepository).getOffenderReleases(agency, from.toLocalDate())
      verify(movementsRepository).getIndividualSchedules(agency, from.toLocalDate())

      verifyNoMoreInteractions(movementsRepository)
    }

    @Test
    fun testAgencyEventsCombinationQuery() {
      val from = LocalDateTime.parse("2019-05-01T11:00:00")
      val to = LocalDateTime.parse("2019-05-01T17:00:00")
      val agency = "LEI"

      val listOfCourtEvents = listOf<CourtEvent?>(
        CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(LocalDateTime.now()).build(),
      )

      val listOfTransferEvents = listOf<TransferEvent?>(
        TransferEvent.builder().offenderNo("7777").fromAgency("MDI").startTime(from).endTime(to).eventClass("EXT_MOV")
          .eventStatus("SCH").createDateTime(
            LocalDateTime.now(),
          ).build(),
      )

      whenever(movementsRepository.getCourtEvents(agency, from.toLocalDate()))
        .thenReturn(listOfCourtEvents)
      whenever(
        movementsRepository.getIndividualSchedules(
          agency,
          from.toLocalDate(),
        ),
      ).thenReturn(listOfTransferEvents)

      val transferSummary = movementsService.getTransferMovementsForAgencies(
        agency,
        from,
      )

      assertThat(transferSummary).isNotNull()

      assertThat(transferSummary.courtEvents).containsAll(listOfCourtEvents)
      assertThat(transferSummary.releaseEvents).isNullOrEmpty()
      assertThat(transferSummary.transferEvents).containsAll(listOfTransferEvents)
      assertThat(transferSummary.movements).isNullOrEmpty()

      verify(movementsRepository).getCourtEvents(agency, from.toLocalDate())
      verify(movementsRepository).getOffenderReleases(agency, from.toLocalDate())
      verify(movementsRepository).getIndividualSchedules(agency, from.toLocalDate())

      verifyNoMoreInteractions(movementsRepository)
    }

    @Nested
    inner class GetTransfers {
      @Test
      fun makesASingleCallToTheRepository() {
        val agency = "LEI"
        movementsService.getTransferMovementsForAgencies(
          agency,
          LocalDateTime.now(),
        )

        verify(movementsRepository, Mockito.times(1))
          .getIndividualSchedules(any(), any())
        verify(movementsRepository).getIndividualSchedules(agency, LocalDate.now())
      }

      @Test
      fun returnSchedulesThatAreTransfersAnd_notDeleted() {
        val startDateTime = LocalDateTime.now()
        val endDateTime = LocalDateTime.now()

        whenever(
          movementsRepository.getIndividualSchedules(
            any(),
            any(),
          ),
        ).thenReturn(
          listOf(
            makeTransfer("A12345", "SCH", "LEI", "MDI", startDateTime, endDateTime),
            makeTransfer("A12346", "DEL", "MDI", "LEI", startDateTime, endDateTime),
            makeInternalMovement("A12347"),
          ),
        )

        val transfers = movementsService.getTransferMovementsForAgencies(
          "LEI",
          startDateTime,
        )

        assertThat(transfers.transferEvents).hasSize(1)
        assertThat(transfers.transferEvents)
          .extracting("offenderNo", "fromAgency", "toAgency")
          .contains(Tuple.tuple("A12345", "LEI", "MDI"))
      }

      @Test
      fun returnScheduledTransfers_agencyDescriptionsFormattedCorrectly() {
        val startDateTime = LocalDateTime.now()
        val endDateTime = LocalDateTime.now()

        whenever(
          movementsRepository.getIndividualSchedules(
            any(),
            any(),
          ),
        ).thenReturn(
          listOf(
            makeTransfer("A12345", "SCH", "LEI", "MDI", startDateTime, endDateTime),
            makeInternalMovement("A12347"),
          ),
        )

        val transfers = movementsService.getTransferMovementsForAgencies(
          "LEI",
          startDateTime,
        )

        assertThat(transfers.transferEvents).hasSize(1)
        assertThat(transfers.transferEvents)
          .extracting("offenderNo", "fromAgency", "fromAgencyDescription", "toAgency", "toAgencyDescription")
          .contains(Tuple.tuple("A12345", "LEI", "Leeds (HMP)", "MDI", "Moorland (HMP & YOI)"))
      }

      private fun makeTransfer(
        offenderNo: String?,
        eventStatus: String?,
        fromAgencyId: String?,
        toAgencyId: String?,
        startDateTime: LocalDateTime?,
        endDateTime: LocalDateTime?,
      ): TransferEvent? {
        val agencyDescriptionMap = mapOf("LEI" to "LEEDS (HMP)", "MDI" to "MOORLAND (HMP & YOI)")

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
          .build()
      }

      private fun makeInternalMovement(offenderNo: String): TransferEvent? = TransferEvent.builder()
        .offenderNo(offenderNo)
        .eventType("MOV")
        .eventClass("INT_MOV")
        .eventStatus("SCH")
        .build()
    }
  }

  @Test
  fun testGetLatestArrivalDate() {
    val offenderNumber = "Z0024ZZ"
    val arrivalDate = LocalDate.of(2017, 6, 16)

    whenever(movementsRepository.getLatestArrivalDate(offenderNumber)).thenReturn(
      Optional.of(arrivalDate),
    )

    val latestArrivalDate = movementsService.getLatestArrivalDate(offenderNumber)
    assertThat(latestArrivalDate).isEqualTo(Optional.of(arrivalDate))

    verify(movementsRepository).getLatestArrivalDate(offenderNumber)
  }

  @Test
  fun testGetLatestArrivalDates() {
    val offenderNumbers = listOf("Z0019ZZ", "Z0024ZZ")
    val latestArrivalDates = listOf<OffenderLatestArrivalDate?>(
      OffenderLatestArrivalDate("Z0019ZZ", LocalDate.of(2011, 11, 7)),
      OffenderLatestArrivalDate("Z0024ZZ", LocalDate.of(2017, 7, 16)),
    )

    whenever(
      movementsRepository.getLatestArrivalDates(
        listOf("Z0019ZZ"),
      ),
    ).thenReturn(
      listOf(latestArrivalDates.first()),
    )
    whenever(
      movementsRepository.getLatestArrivalDates(
        listOf("Z0024ZZ"),
      ),
    ).thenReturn(
      listOf(
        latestArrivalDates.last(),
      ),
    )

    val result = movementsService.getLatestArrivalDates(offenderNumbers)
    assertThat(result).isEqualTo(latestArrivalDates)
  }
}
