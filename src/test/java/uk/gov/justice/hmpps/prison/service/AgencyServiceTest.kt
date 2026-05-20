package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.isA
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.repository.AgencyRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationFilter
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter

class AgencyServiceTest {
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mock()
  private val agencyRepo: AgencyRepository = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val agencyLocationTypeReferenceCodeRepository: ReferenceCodeRepository<AgencyLocationType> = mock()
  private val service = AgencyService(
    hmppsAuthenticationHolder,
    agencyRepo,
    agencyLocationRepository,
    referenceDomainService,
    agencyLocationTypeReferenceCodeRepository,
    agencyInternalLocationRepository,
  )

  @Test
  fun shouldCallGetAgency() {
    whenever(
      agencyLocationRepository.findAll(isA(AgencyLocationFilter::class.java)),
    ).thenReturn(
      listOf(AgencyLocation.builder().id("LEI").build()),
    )
    service.getAgency("LEI", StatusFilter.ALL, null, false, false, false)
    verify(agencyLocationRepository)
      .findAll(isA(AgencyLocationFilter::class.java))
  }

  @Test
  fun shouldReturnEntityNotFoundForSinglePrisonWithBlankAddress() {
    assertThatThrownBy { service.getPrisonContactDetail("BLANK") }
      .isInstanceOf(EntityNotFoundException::class.java)
  }

  @Test
  fun shouldReturnEntityNotFoundForEmptyResult() {
    assertThatThrownBy { service.getPrisonContactDetail("NOADDRESS") }
      .isInstanceOf(EntityNotFoundException::class.java)
  }

  @Test
  fun shouldIdentifyBlankAddress() {
    assertThat(service.removeBlankAddresses(buildPrisonContactDetailsList())).hasSize(2)
    assertThat(service.removeBlankAddresses(buildPrisonContactDetailsListSingleResult())).hasSize(1)
    assertThat(
      service.removeBlankAddresses(buildPrisonContactDetailsListSingleResultBlankAddress()),
    ).isEmpty()
  }

  @Test
  fun shouldReturnAllActiveReceptionsWithSpaceForAgency() {
    whenever(
      agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive(
        "LEI",
        "RECP",
        true,
      ),
    ).thenReturn(
      listOf(
        AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP")
          .operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles())
          .build(),
        AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECEP").locationCode("AREA")
          .operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles())
          .build(),
      ),
    )

    val receptions = service.getReceptionsWithCapacityInAgency("LEI", null)
    assertThat(receptions).extracting("id").containsExactly(-1L)
  }

  @Test
  fun shouldReturnAllActiveReceptionsWithSpaceForAgencyWithAttribute() {
    whenever(
      agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive(
        "LEI",
        "RECP",
        true,
      ),
    ).thenReturn(
      listOf(
        AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP")
          .operationalCapacity(2).currentOccupancy(1).active(true).profiles(buildAgencyInternalLocationProfiles())
          .build(),
        AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECP").locationCode("RECP").capacity(2)
          .currentOccupancy(1).active(true).profiles(
            mutableListOf<AgencyInternalLocationProfile?>(),
          ).build(),
        AgencyInternalLocation.builder().locationId(-3L).description("LEI-RECP").locationCode("RECP")
          .operationalCapacity(2).currentOccupancy(2).active(true).profiles(
            mutableListOf<AgencyInternalLocationProfile?>(),
          ).build(),
      ),
    )

    val offenderCells = service.getReceptionsWithCapacityInAgency("LEI", "DO")
    assertThat(offenderCells).extracting("id").containsExactly(-1L)
  }

  @Test
  fun shouldReturnAllActiveReceptionsWithIgnoringZeroOperationalCapacityForAgencyWithAttribute() {
    whenever(
      agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive(
        "LEI",
        "RECP",
        true,
      ),
    ).thenReturn(
      listOf(
        AgencyInternalLocation.builder().locationId(-1L).description("LEI-RECP").locationCode("RECP")
          .operationalCapacity(0).capacity(3).currentOccupancy(2).active(true)
          .profiles(buildAgencyInternalLocationProfiles()).build(),
        AgencyInternalLocation.builder().locationId(-2L).description("LEI-RECP").locationCode("RECP")
          .operationalCapacity(0).capacity(2).currentOccupancy(2).active(true).profiles(
            mutableListOf<AgencyInternalLocationProfile?>(),
          ).build(),
      ),
    )

    val offenderCells = service.getReceptionsWithCapacityInAgency("LEI", "DO")
    assertThat(offenderCells).extracting("id").containsExactly(-1L)
  }

  private fun buildPrisonContactDetailsList(): List<PrisonContactDetail> = listOf(
    PrisonContactDetail.builder().agencyId("ABC")
      .premise("ABC prison")
      .city("Manchester")
      .phones(
        listOf(
          Telephone.builder().number("0114 2233444").type("BUS").build(),
          Telephone.builder().number("0114 6667775").type("BUS").build(),
        ),
      )
      .build(),
    PrisonContactDetail.builder().agencyId("DEF")
      .premise("ABC prison")
      .city("Manchester")
      .phones(listOf(Telephone.builder().number("0114 2233444").type("BUS").build()))
      .build(),
    PrisonContactDetail.builder().agencyId("BLANK")
      .phones(listOf(Telephone.builder().number("0114 2233444").type("BUS").build()))
      .build(),
    PrisonContactDetail.builder().agencyId("BLANK_WITH_COUNTRY")
      .country("England")
      .phones(listOf(Telephone.builder().number("0114 2233444").type("BUS").build()))
      .build(),
  )

  private fun buildPrisonContactDetailsListSingleResult(): List<PrisonContactDetail> = listOf(
    PrisonContactDetail.builder().agencyId("ABC")
      .premise("ABC prison")
      .city("Manchester")
      .phones(
        listOf(
          Telephone.builder().number("0114 2233444").type("BUS").build(),
          Telephone.builder().number("0114 6667775").type("BUS").build(),
        ),
      )
      .build(),
  )

  private fun buildPrisonContactDetailsListSingleResultBlankAddress(): List<PrisonContactDetail> = listOf(
    PrisonContactDetail.builder().agencyId("BLANK")
      .country("England")
      .phones(
        listOf(
          Telephone.builder().number("0114 2233444").type("BUS").build(),
          Telephone.builder().number("0114 6667775").type("BUS").build(),
        ),
      )
      .build(),
  )

  private fun buildAgencyInternalLocationProfiles(): List<AgencyInternalLocationProfile> = listOf(
    AgencyInternalLocationProfile.builder()
      .locationId(-1L)
      .profileType("HOU_UNIT_ATT")
      .housingAttributeReferenceCode(HousingAttributeReferenceCode("DO", "Double Occupancy"))
      .build(),
    AgencyInternalLocationProfile.builder()
      .locationId(-1L)
      .profileType("HOU_UNIT_ATT")
      .housingAttributeReferenceCode(HousingAttributeReferenceCode("LC", "Listener Cell"))
      .build(),
    AgencyInternalLocationProfile.builder()
      .locationId(-1L)
      .profileType("HOU_UNIT_ATT")
      .housingAttributeReferenceCode(null)
      .build(),
  )
}
