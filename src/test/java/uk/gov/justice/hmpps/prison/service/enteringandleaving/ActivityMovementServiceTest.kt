package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.service.ServiceAgencySwitchesService
import java.time.LocalDate

class ActivityMovementServiceTest {
  private val offenderProgramProfileRepository: OffenderProgramProfileRepository = mock()
  private val serviceAgencySwitchesService: ServiceAgencySwitchesService = mock()
  private val service: ActivityMovementService = ActivityMovementService(offenderProgramProfileRepository, serviceAgencySwitchesService)

  private val booking = OffenderBooking.builder().bookingId(1L).build()
  private val prison = AgencyLocation.builder().id("MDI").build()

  @Test
  fun `should end offender program profiles`() {
    whenever(serviceAgencySwitchesService.checkServiceSwitchedOnForPrison(anyString(), anyString()))
      .thenReturn(false)

    service.endActivitiesAndWaitlist(booking, prison, LocalDate.now(), "some_reason")

    verify(serviceAgencySwitchesService).checkServiceSwitchedOnForPrison("ACTIVITY", prison.id)
    verify(offenderProgramProfileRepository).endActivitiesForBookingAtPrison(booking, prison, LocalDate.now(), "some_reason")
  }

  @Test
  fun `should end waitlist`() {
    whenever(serviceAgencySwitchesService.checkServiceSwitchedOnForPrison(anyString(), anyString()))
      .thenReturn(false)

    service.endActivitiesAndWaitlist(booking, prison, LocalDate.now(), "some_reason")

    verify(offenderProgramProfileRepository).endWaitListActivitiesForBookingAtPrison(booking, prison, LocalDate.now(), "some_reason")
  }

  @Test
  fun `should not end offender program profiles or waitlist in prison switched on for DPS`() {
    whenever(serviceAgencySwitchesService.checkServiceSwitchedOnForPrison(anyString(), anyString()))
      .thenReturn(true)

    service.endActivitiesAndWaitlist(booking, prison, LocalDate.now(), "some_reason")

    verifyNoInteractions(offenderProgramProfileRepository)
  }
}
