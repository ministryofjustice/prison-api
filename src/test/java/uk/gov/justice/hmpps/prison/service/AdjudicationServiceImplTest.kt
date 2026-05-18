package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence
import uk.gov.justice.hmpps.prison.api.support.Page
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository

class AdjudicationServiceImplTest {
  private val adjudicationsRepository: AdjudicationsRepository = mock()
  private var adjudicationService: AdjudicationService = AdjudicationService(adjudicationsRepository)

  @Test
  fun findAdjudications() {
    val adjudication = Adjudication.builder().build()
    val expectedResult = Page(listOf(adjudication), 1, PageRequest())
    val criteria = AdjudicationSearchCriteria.builder().offenderNumber("OFF-1").build()

    whenever(adjudicationsRepository.findAdjudications(any())).thenReturn(expectedResult)

    assertThat(adjudicationService.findAdjudications(criteria)).isEqualTo(expectedResult)
  }

  @Test
  fun adjudicationOffences() {
    val expectedResult = listOf(AdjudicationOffence.builder().build())

    whenever(adjudicationsRepository.findAdjudicationOffences(ArgumentMatchers.anyString()))
      .thenReturn(expectedResult)

    assertThat(adjudicationService.findAdjudicationsOffences("OFF-1")).isEqualTo(expectedResult)

    verify(adjudicationsRepository).findAdjudicationOffences("OFF-1")
  }

  @Test
  fun adjudicationAgencies() {
    val dbResult = listOf(Agency.builder().description("MOORLANDS (HMP)").build())
    val expectedResult = listOf(Agency.builder().description("Moorlands (HMP)").build())

    whenever(adjudicationsRepository.findAdjudicationAgencies(ArgumentMatchers.anyString()))
      .thenReturn(dbResult)

    assertThat(adjudicationService.findAdjudicationAgencies("OFF-1")).isEqualTo(expectedResult)

    verify(adjudicationsRepository).findAdjudicationAgencies("OFF-1")
  }
}
