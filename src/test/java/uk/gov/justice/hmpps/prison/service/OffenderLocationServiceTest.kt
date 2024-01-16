package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import java.util.Optional

class OffenderLocationServiceTest {
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository = mock()

  private val offenderLocationService: OffenderLocationService =
    OffenderLocationService(agencyInternalLocationRepository, bedAssignmentHistoriesRepository)

  @Test
  internal fun `getOffenderLocation currently out of prison`() {
    val (levels, _) = offenderLocationService.getOffenderLocation(
      12345,
      OffenderSummary().also { it.currentlyInPrison = "N" },
    )
    assertThat(levels).isNull()
  }

  @Test
  internal fun `getOffenderLocation no current location`() {
    val (levels, _) = offenderLocationService.getOffenderLocation(
      12345,
      OffenderSummary().also { it.internalLocationId = "Y" },
    )
    assertThat(levels).isNull()
  }

  @Test
  internal fun `getOffenderLocation current is permanent`() {
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "LOC"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
        },
      ),
    )
    val (levels, _) = offenderLocationService.getOffenderLocation(12345, offenderInPrison())
    assertThat(levels).isEqualTo(
      listOf(
        HousingLocation(
          level = 1,
          code = "LOC",
          LivingUnitReferenceCode("LIV", "desc"),
          userDescription = "DESC",
        ),
      ),
    )
  }

  @Test
  internal fun `getOffenderLocation current is permanent calls agencyInternalLocationRepository`() {
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "LOC"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
        },
      ),
    )
    offenderLocationService.getOffenderLocation(12345, offenderInPrison())
    verify(agencyInternalLocationRepository).findOneByLocationId(2345L)
  }

  @Test
  internal fun `getOffenderLocation current is permanent with parent`() {
    val parentLocation = AgencyInternalLocation().also {
      it.locationCode = "PLOC"
      it.livingUnit = LivingUnitReferenceCode("PLIV", "desc")
      it.userDescription = "PDESC"
    }
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "LOC"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
          it.parentLocation = parentLocation
        },
      ),
    )
    val (levels, _) = offenderLocationService.getOffenderLocation(
      12345,
      offenderInPrison(),
    )
    assertThat(levels).containsExactly(
      HousingLocation(
        level = 1,
        code = "PLOC",
        LivingUnitReferenceCode("PLIV", "desc"),
        userDescription = "PDESC",
      ),
      HousingLocation(
        level = 2,
        code = "LOC",
        LivingUnitReferenceCode("LIV", "desc"),
        userDescription = "DESC",
      ),
    )
  }

  @Test
  internal fun `getOffenderLocation current is temporary with no previous`() {
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "RECEPTION"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
        },
      ),
    )
    whenever(
      bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(
        any(),
        any(),
      ),
    ).thenReturn(
      PageImpl(listOf(), Pageable.ofSize(5), 6),
    )
    val (levels, lastPreviousLevels) = offenderLocationService.getOffenderLocation(12345, offenderInPrison())
    assertThat(levels).isEqualTo(
      listOf(
        HousingLocation(
          level = 1,
          code = "RECEPTION",
          LivingUnitReferenceCode("LIV", "desc"),
          userDescription = "DESC",
        ),
      ),
    )
    assertThat(lastPreviousLevels).isNull()
  }

  @Test
  internal fun `getOffenderLocation current is temporary with previous permanent`() {
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "CSWAP"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
        },
      ),
    )
    whenever(
      bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(any(), any()),
    ).thenReturn(
      PageImpl(
        listOf(
          BedAssignmentHistory().also { bed ->
            bed.location = AgencyInternalLocation().also {
              it.locationCode = "COURT"
              it.agencyId = "MDI"
            }
          },
          BedAssignmentHistory().also { bed ->
            bed.location = AgencyInternalLocation().also {
              it.locationCode = "LOC"
              it.livingUnit = LivingUnitReferenceCode("LIV2", "desc")
              it.userDescription = "PREV"
              it.agencyId = "MDI"
            }
          },
        ),
        Pageable.ofSize(5),
        6,
      ),
    )
    val (levels, lastPreviousLevels) = offenderLocationService.getOffenderLocation(
      12345,
      offenderInPrison(),
    )
    assertThat(levels).isEqualTo(
      listOf(
        HousingLocation(
          level = 1,
          code = "CSWAP",
          LivingUnitReferenceCode("LIV", "desc"),
          userDescription = "DESC",
        ),
      ),
    )
    assertThat(lastPreviousLevels).isEqualTo(
      listOf(
        HousingLocation(
          level = 1,
          code = "LOC",
          LivingUnitReferenceCode("LIV2", "desc"),
          userDescription = "PREV",
        ),
      ),
    )
  }

  @Test
  internal fun `getOffenderLocation current is temporary with previous permanent at different prison`() {
    whenever(agencyInternalLocationRepository.findOneByLocationId(any())).thenReturn(
      Optional.of(
        AgencyInternalLocation().also {
          it.locationCode = "CSWAP"
          it.livingUnit = LivingUnitReferenceCode("LIV", "desc")
          it.userDescription = "DESC"
        },
      ),
    )
    whenever(
      bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(any(), any()),
    ).thenReturn(
      PageImpl(
        listOf(
          BedAssignmentHistory().also { bed ->
            bed.location = AgencyInternalLocation().also {
              it.locationCode = "COURT"
              it.agencyId = "MDI"
            }
          },
          BedAssignmentHistory().also { bed ->
            bed.location = AgencyInternalLocation().also {
              it.locationCode = "LOC"
              it.livingUnit = LivingUnitReferenceCode("LIV2", "desc")
              it.userDescription = "PREV"
              it.agencyId = "LEI"
            }
          },
        ),
        Pageable.ofSize(5),
        6,
      ),
    )
    val (levels, lastPreviousLevels) = offenderLocationService.getOffenderLocation(
      12345,
      offenderInPrison(),
    )
    assertThat(levels).isEqualTo(
      listOf(
        HousingLocation(
          level = 1,
          code = "CSWAP",
          LivingUnitReferenceCode("LIV", "desc"),
          userDescription = "DESC",
        ),
      ),
    )
    assertThat(lastPreviousLevels).isNull()
  }

  private fun offenderInPrison() = OffenderSummary().also {
    it.currentlyInPrison = "Y"
    it.internalLocationId = "2345"
    it.agencyLocationId = "MDI"
  }
}
