package uk.gov.justice.hmpps.prison.service.transfer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import java.time.LocalDate
import java.time.LocalDateTime

internal class BedAssignmentTransferServiceTest {

  private val bedAssignmentHistoryRepository: BedAssignmentHistoriesRepository = mock()
  private val service = BedAssignmentTransferService(bedAssignmentHistoryRepository)

  @Test
  internal fun `will create history with next sequence`() {
    whenever(bedAssignmentHistoryRepository.save(any())).thenAnswer { it.getArgument<BedAssignmentHistory>(0) }
    whenever(bedAssignmentHistoryRepository.getMaxSeqForBookingId(99L)).thenReturn(15)
    val booking = OffenderBooking().apply { bookingId = 99L }
    val cellLocation = AgencyInternalLocation().apply { locationId = 88L }

    service.createBedHistory(booking, cellLocation, LocalDateTime.parse("2020-01-01T00:00:00"))

    verify(bedAssignmentHistoryRepository).save(
      check {
        assertThat(it.assignmentDate).isEqualTo(LocalDate.parse("2020-01-01"))
        assertThat(it.assignmentDateTime).isEqualTo(LocalDateTime.parse("2020-01-01T00:00:00"))
        assertThat(it.offenderBooking).isEqualTo(booking)
        assertThat(it.livingUnitId).isEqualTo(88L)
        assertThat(it.assignmentReason).isEqualTo("ADM")
        assertThat(it.bedAssignmentHistoryPK.sequence).isEqualTo(16)
      }
    )
  }
}
