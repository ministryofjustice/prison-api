package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.hmpps.prison.api.model.BedAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import java.time.LocalDate
import java.time.LocalDateTime

internal class BedAssignmentHistoryServiceTest {
  private companion object {
    private const val MAX_BATCH_SIZE = 1
  }
  private val repository: BedAssignmentHistoriesRepository = mock()
  private val locationRepository: AgencyInternalLocationRepository = mock()

  private val service = BedAssignmentHistoryService(repository, locationRepository, MAX_BATCH_SIZE)

  @Test
  fun add() {
    val now = LocalDateTime.now()
    val pk = service.add(1L, 2L, "RSN", now)

    verify(repository)
      .save<BedAssignmentHistory>(
        ArgumentMatchers.argThat { bedAssignment: BedAssignmentHistory ->
          bedAssignment.bedAssignmentHistoryPK
            .offenderBookingId == 1L &&
            bedAssignment.livingUnitId == 2L &&
            bedAssignment.assignmentReason == "RSN" &&
            bedAssignment.assignmentDate.isEqual(now.toLocalDate()) &&
            bedAssignment.assignmentDateTime.isEqual(now)
        },
      )

    assertThat(pk).isEqualTo(BedAssignmentHistoryPK(1L, 1))
  }

  @Test
  fun getBedAssignmentsHistory() {
    val assignments = listOf(
      BedAssignmentHistory.builder()
        .bedAssignmentHistoryPK(BedAssignmentHistoryPK(1L, 2))
        .assignmentDate(LocalDate.of(2015, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2016, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving")
        .livingUnitId(1L)
        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
        .location(AgencyInternalLocation.builder().description("MDI-1-2").agencyId("MDI").build())
        .build(),
      BedAssignmentHistory.builder()
        .bedAssignmentHistoryPK(BedAssignmentHistoryPK(1L, 3))
        .assignmentDate(LocalDate.of(2016, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2017, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving again")
        .livingUnitId(2L)
        .location(AgencyInternalLocation.builder().description("MDI-1-2").agencyId("MDI").build())
        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
        .build(),
    )
    val page = PageImpl<BedAssignmentHistory>(assignments)
    whenever(
      repository.findAllByBedAssignmentHistoryPKOffenderBookingId(
        1L,
        PageRequest.of(0, 20),
      ),
    ).thenReturn(page)
    val response: Page<BedAssignment> = service.getBedAssignmentsHistory(1L, PageRequest.of(0, 20))
    assertThat(response).containsOnly(
      BedAssignment.builder()
        .bookingId(1L)
        .livingUnitId(1L)
        .assignmentDate(LocalDate.of(2015, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2016, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving")
        .description("MDI-1-2")
        .agencyId("MDI")
        .bedAssignmentHistorySequence(2)
        .build(),
      BedAssignment.builder()
        .bookingId(1L)
        .livingUnitId(2L)
        .assignmentDate(LocalDate.of(2016, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2017, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving again")
        .description("MDI-1-2")
        .agencyId("MDI")
        .bedAssignmentHistorySequence(3)
        .build(),
    )
  }

  @Test
  fun getBedAssignmentHistory_forLocationIdAndDateRange() {
    val livingUnitId = 1L
    val bedHistoryAssignment = aBedAssignment(
      1L,
      livingUnitId,
      AgencyInternalLocation.builder()
        .description("MDI-1-2")
        .agencyId("MDI")
        .build(),
    )

    whenever(locationRepository.existsById(livingUnitId)).thenReturn(true)
    whenever(
      repository.findByLivingUnitIdAndDateTimeRange(
        anyLong(),
        any(),
        any(),
      ),
    )
      .thenReturn(listOf(bedHistoryAssignment))

    val cellHistory = service.getBedAssignmentsHistory(livingUnitId, LocalDateTime.now(), LocalDateTime.now())

    assertThat(cellHistory).containsOnly(
      BedAssignment.builder()
        .bookingId(1L)
        .livingUnitId(livingUnitId)
        .assignmentDate(LocalDate.of(2015, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2016, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving")
        .description("MDI-1-2")
        .agencyId("MDI")
        .bedAssignmentHistorySequence(2)
        .offenderNo("A12345")
        .build(),
    )
  }

  @Test
  fun getBedAssignmentHistory_byDate_filteredByAgencyId() {
    val livingUnitIdInMoorland = 1L
    val bookingId = 1L
    val assignmentDate = LocalDate.now()
    val moorland = "MDI"

    whenever(
      locationRepository.findAgencyInternalLocationsByAgencyIdAndLocationType(
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        AgencyInternalLocation.builder().locationId(livingUnitIdInMoorland).build(),
      ),
    )

    whenever(
      repository.findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(
        any(),
        any(),
      ),
    )
      .thenReturn(
        listOf(
          aBedAssignment(
            bookingId,
            livingUnitIdInMoorland,
            AgencyInternalLocation.builder()
              .locationId(livingUnitIdInMoorland)
              .description("MDI-1-2")
              .agencyId(moorland)
              .build(),
          ),
          aBedAssignment(
            bookingId,
            livingUnitIdInMoorland,
            AgencyInternalLocation.builder()
              .locationId(livingUnitIdInMoorland)
              .description("MDI-1-2")
              .agencyId(moorland)
              .build(),
          ),
        ),
      )

    val cellHistory = service.getBedAssignmentsHistoryByDateForAgency(moorland, assignmentDate)

    verify(repository)
      .findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(
        assignmentDate,
        setOf(livingUnitIdInMoorland),
      )

    assertThat(cellHistory).containsOnly(
      BedAssignment.builder()
        .bookingId(bookingId)
        .offenderNo("A12345")
        .livingUnitId(livingUnitIdInMoorland)
        .assignmentDate(LocalDate.of(2015, 5, 1))
        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
        .assignmentEndDate(LocalDate.of(2016, 5, 1))
        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
        .assignmentReason("Needs moving")
        .description("MDI-1-2")
        .agencyId("MDI")
        .bedAssignmentHistorySequence(2)
        .build(),
    )
  }

  @Test
  fun getBedAssignmentHistory_byDate_batchCalls() {
    whenever(
      locationRepository.findAgencyInternalLocationsByAgencyIdAndLocationType(
        any(),
        any(),
      ),
    ).thenReturn(
      listOf(
        AgencyInternalLocation.builder().locationId(1L).build(),
        AgencyInternalLocation.builder().locationId(2L).build(),
      ),
    )

    val assignmentDate = LocalDate.now()

    service.getBedAssignmentsHistoryByDateForAgency("MDI", assignmentDate)

    verify(repository)
      .findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(assignmentDate, mutableSetOf(1L))
    verify(repository)
      .findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(assignmentDate, mutableSetOf(2L))
  }

  @Test
  fun getBedAssignmentHistory_cellNotFound() {
    whenever(locationRepository.existsById(anyLong())).thenReturn(false)

    assertThatThrownBy {
      service.getBedAssignmentsHistory(
        1L,
        LocalDateTime.now(),
        LocalDateTime.now(),
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Cell 1 not found")
  }

  @Test
  fun getBedAssignmentHistory_checkDateOrder() {
    assertThatThrownBy {
      service.getBedAssignmentsHistory(
        1L,
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now(),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("The fromDate should be less then or equal to the toDate")
  }

  private fun aBedAssignment(
    bookingId: Long,
    livingUnitId: Long,
    location: AgencyInternalLocation?,
  ): BedAssignmentHistory = BedAssignmentHistory.builder()
    .bedAssignmentHistoryPK(BedAssignmentHistoryPK(bookingId, 2))
    .assignmentDate(LocalDate.of(2015, 5, 1))
    .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
    .assignmentEndDate(LocalDate.of(2016, 5, 1))
    .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
    .assignmentReason("Needs moving")
    .livingUnitId(livingUnitId)
    .location(location)
    .offenderBooking(
      OffenderBooking.builder().bookingId(bookingId).offender(Offender.builder().nomsId("A12345").build()).build(),
    )
    .build()
}
