package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria
import uk.gov.justice.hmpps.prison.api.support.Page
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository
import java.time.LocalDate

class GlobalSearchServiceImplTest {
  private val inmateRepository: InmateRepository = mock()
  private val prisonerRepository: PrisonerRepository = mock()

  private val service: GlobalSearchService = GlobalSearchService(inmateRepository, prisonerRepository)

  private lateinit var criteria: PrisonerDetailSearchCriteria

  private val pageRequest = PageRequest()

  @Test
  fun testFindOffendersBlankCriteria() {
    criteria = PrisonerDetailSearchCriteria.builder().build()

    service.findOffenders(criteria, pageRequest)

    verify(inmateRepository, never()).findOffenders(
      anyString(),
      any(),
    )
  }

  @Test
  fun testFindOffendersByOffenderNo() {
    criteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()

    whenever(inmateRepository.generateFindOffendersQuery(criteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )
    whenever(
      inmateRepository.findOffenders(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(0))

    service.findOffenders(criteria, pageRequest)

    verify(inmateRepository).findOffenders(
      eq(
        TEST_OFFENDER_NO_QUERY,
      ),
      any(),
    )
  }

  @Test
  fun testFindOffendersPrioritisedMatchWithOffenderNoMatch() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .pncNumber(TEST_PNC_NUMBER)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )

    whenever(
      inmateRepository.findOffenders(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffenders(
      eq(
        TEST_OFFENDER_NO_QUERY,
      ),
      any(),
    )
    verify(prisonerRepository, never()).findOffenders(
      any(),
      any(),
    )
  }

  @Test
  fun testFindOffendersPrioritisedMatchWithPncNumberMatch() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .pncNumber(TEST_PNC_NUMBER)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()
    val pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY)

    whenever(
      inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any()),
    ).thenReturn(pageResponse(0))
    whenever(
      prisonerRepository.findOffenders(eq(pncNumberCriteria), any()),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any())
    verify(prisonerRepository).findOffenders(eq(pncNumberCriteria), any())
  }

  @Test
  fun testFindOffendersWithPncNumberMatch() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(false)
      .pncNumber(TEST_PNC_NUMBER)
      .build()

    val pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build()

    whenever(
      prisonerRepository.findOffenders(
        eq(pncNumberCriteria),
        any(),
      ),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verifyNoInteractions(inmateRepository)
    verify(prisonerRepository).findOffenders(
      eq(pncNumberCriteria),
      any(),
    )
  }

  @Test
  fun testFindOffendersPrioritisedMatchWithCroNumberMatch() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .pncNumber(TEST_PNC_NUMBER)
      .croNumber(TEST_CRO_NUMBER)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()
    val pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build()
    val croNumberCriteria = PrisonerDetailSearchCriteria.builder().croNumber(TEST_CRO_NUMBER).build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY)

    whenever(
      inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any()),
    ).thenReturn(pageResponse(0))
    whenever(
      prisonerRepository.findOffenders(
        eq(pncNumberCriteria),
        any(),
      ),
    ).thenReturn(pageResponse(0))
    whenever(
      prisonerRepository.findOffenders(eq(croNumberCriteria), any()),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any())
    verify(prisonerRepository).findOffenders(eq(pncNumberCriteria), any())
    verify(prisonerRepository).findOffenders(eq(croNumberCriteria), any())
  }

  @Test
  fun testFindOffendersWithCroNumberMatch() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(false)
      .croNumber(TEST_CRO_NUMBER)
      .build()

    val croNumberCriteria = PrisonerDetailSearchCriteria.builder().croNumber(TEST_CRO_NUMBER).build()

    whenever(
      prisonerRepository.findOffenders(
        eq(croNumberCriteria),
        any(),
      ),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verifyNoInteractions(inmateRepository)
    verify(prisonerRepository).findOffenders(eq(croNumberCriteria), any())
  }

  @Test
  fun testFindOffendersPrioritisedMatchWithPersonalAttrsMatch() {
    val testLastName = "STEPHENS"
    val testPersonalAttrsQuery = "lastName:eq:'STEPHENS'"

    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .lastName(testLastName)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()
    val personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(testLastName).build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )
    whenever(inmateRepository.generateFindOffendersQuery(personalAttrsCriteria))
      .thenReturn(testPersonalAttrsQuery)

    whenever(
      inmateRepository.findOffenders(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(0))
    whenever(
      inmateRepository.findOffenders(
        eq(testPersonalAttrsQuery),
        any(),
      ),
    ).thenReturn(pageResponse(3))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffenders(
      eq(
        TEST_OFFENDER_NO_QUERY,
      ),
      any(),
    )
    verify(inmateRepository).findOffenders(
      eq(testPersonalAttrsQuery),
      any(),
    )
  }

  @Test
  fun testFindOffendersAliasSearchLocationFilter() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .location(LOCATION_FILTER_OUT)
      .includeAliases(true)
      .build()

    whenever(inmateRepository.generateFindOffendersQuery(criteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )
    whenever(
      inmateRepository.findOffendersWithAliases(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffendersWithAliases(
      eq(
        TEST_OFFENDER_NO_QUERY,
      ),
      any(),
    )
  }

  @Test
  fun testFindOffendersAliasSearchInvalidLocationFilter() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .location("ABC")
      .includeAliases(true)
      .build()

    assertThatThrownBy { service.findOffenders(criteria, pageRequest) }
      .isInstanceOf(HttpClientErrorException::class.java)
  }

  @Test
  fun testFindOffendersPrioritisedMatchWithDobRangeMatch() {
    val testLastName = "STEPHENS"
    val testDobFrom = LocalDate.of(1960, 1, 1)
    val testDobTo = LocalDate.of(1964, 12, 31)
    val testPersonalAttrsQuery = "lastName:eq:'STEPHENS'"
    val testDobRangeQuery =
      "(and:dateOfBirth:gteq:'1960-01-01':'YYYY-MM-DD',and:dateOfBirth:lteq:'1964-12-31':'YYYY-MM-DD')"

    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .lastName(testLastName)
      .dobFrom(testDobFrom)
      .dobTo(testDobTo)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()
    val personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(testLastName).build()

    val dobRangeCriteria = PrisonerDetailSearchCriteria.builder()
      .dobFrom(testDobFrom)
      .dobTo(testDobTo)
      .build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )
    whenever(inmateRepository.generateFindOffendersQuery(personalAttrsCriteria))
      .thenReturn(testPersonalAttrsQuery)
    whenever(inmateRepository.generateFindOffendersQuery(dobRangeCriteria))
      .thenReturn(testDobRangeQuery)

    whenever(
      inmateRepository.findOffenders(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(0))
    whenever(
      inmateRepository.findOffenders(
        eq(testPersonalAttrsQuery),
        any(),
      ),
    ).thenReturn(pageResponse(0))
    whenever(
      inmateRepository.findOffenders(
        eq(testDobRangeQuery),
        any(),
      ),
    ).thenReturn(pageResponse(5))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    verify(inmateRepository).findOffenders(
      eq(
        TEST_OFFENDER_NO_QUERY,
      ),
      any(),
    )
    verify(inmateRepository).findOffenders(
      eq(testPersonalAttrsQuery),
      any(),
    )
    verify(inmateRepository).findOffenders(
      eq(testDobRangeQuery),
      any(),
    )
  }

  @Test
  fun testFindOffendersLocationFormatting() {
    criteria = PrisonerDetailSearchCriteria.builder()
      .prioritisedMatch(true)
      .offenderNos(TEST_OFFENDER_NO)
      .build()

    val offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build()

    whenever(inmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(
      TEST_OFFENDER_NO_QUERY,
    )

    whenever(
      inmateRepository.findOffenders(
        eq(
          TEST_OFFENDER_NO_QUERY,
        ),
        any(),
      ),
    ).thenReturn(pageResponse(1))

    val response = service.findOffenders(criteria, pageRequest)

    assertThat(response.getItems()).isNotEmpty()

    assertThat(response.getItems()[0].latestLocation).isEqualTo("Wakefield (HMP)")
  }

  @Test
  fun getOffenderNumbers() {
    val pageRequest = PageRequest(0L, 1L)

    whenever(prisonerRepository.listAllOffenders(pageRequest))
      .thenReturn(Page(listOf(OffenderNumber("offender1")), 1L, pageRequest))

    assertThat(service.getOffenderNumbers(0L, 1L).getItems())
      .containsExactly(OffenderNumber("offender1"))
  }

  private fun pageResponse(prisonerCount: Int): Page<PrisonerDetail> {
    val prisoners = (1..prisonerCount).map {
      PrisonerDetail.builder().offenderNo(String.format("A%4dAA", it)).latestLocation("WAKEFIELD (HMP)").build()
    }

    return Page<PrisonerDetail>(prisoners, prisonerCount.toLong(), 0, 10)
  }

  companion object {
    private const val LOCATION_FILTER_OUT = "OUT"
    private val TEST_OFFENDER_NO = listOf("AA1234B")
    private const val TEST_PNC_NUMBER = "2002/713491N"
    private const val TEST_CRO_NUMBER = "CRO987654"
    private const val TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'"
  }
}
