package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.service.GlobalSearchService
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class PrisonerRepositoryTest(
  @Autowired private val repository: PrisonerRepository,
) {
  private val defaultPageRequest = PageRequest(GlobalSearchService.DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT)

  @Test
  fun testfindOffendersWithValidPNCNumberOnly() {
    val testPncNumber = "14/12345F"

    val criteria = criteriaForPNCNumber(testPncNumber)

    val offender = findOffender(criteria)

    assertThat(offender.offenderNo).isEqualTo("A1234AF")
    assertThat(offender.lastName).isEqualTo("ANDREWS")
  }

  @Test
  fun testfindOffendersWithInvalidPNCNumberOnly() {
    val testPncNumber = "PNC0193032"

    val criteria = criteriaForPNCNumber(testPncNumber)

    assertThatThrownBy { findOffender(criteria) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
  }

  @Test
  fun testfindOffendersWithValidCRONumberOnly() {
    val testCroNumber = "CRO112233"

    val criteria = criteriaForCRONumber(testCroNumber)

    val offender = findOffender(criteria)

    assertThat(offender.offenderNo).isEqualTo("A1234AC")
    assertThat(offender.lastName).isEqualTo("BATES")
  }

  @Test
  fun listAllOffenders() {
    val offenderIds = repository.listAllOffenders(PageRequest(0L, 100L))

    assertThat(offenderIds.totalRecords).isGreaterThan(0)
  }

  @Test
  fun getOffenderIds() {
    val ids = repository.getOffenderIdsFor("A1234AL")
    assertThat(ids).containsExactlyInAnyOrder(-1012L, -1013L)
  }

  @Test
  fun getOffenderIdsReturnsEmptySet() {
    val ids = repository.getOffenderIdsFor("unknown")
    assertThat(ids).isEmpty()
  }

  @Test
  fun testNomsIdSequenceCanBeRetrieved() {
    val nomsIdSequence = repository.getNomsIdSequence()

    assertThat(nomsIdSequence).isNotNull()
  }

  @Test
  fun testNomsIdSequenceCanBeUpdated() {
    val nomsIdSequence = repository.getNomsIdSequence()

    val rowUpdated = repository.updateNomsIdSequence(nomsIdSequence.next(), nomsIdSequence)

    assertThat(rowUpdated).isGreaterThan(0)
  }

  @Test
  fun testNomsIdSequenceCanBeUpdatedAndStored() {
    val initalValue = repository.getNomsIdSequence()

    val next = initalValue.next()
    repository.updateNomsIdSequence(next, initalValue)

    val newValue = repository.getNomsIdSequence()

    assertThat(newValue).isEqualTo(next)
  }

  @Test
  fun testNomsIdSequenceHandlesUPdateByOtherClient() {
    val client1InitialValue = repository.getNomsIdSequence()
    val client2InitialValue = repository.getNomsIdSequence()

    val client1Next = client1InitialValue.next()
    val client2Next = client2InitialValue.next()

    assertThat(repository.updateNomsIdSequence(client2Next, client2InitialValue)).isGreaterThan(0)

    assertThat(repository.updateNomsIdSequence(client1Next, client1InitialValue)).isEqualTo(0)
  }

  private fun criteriaForPNCNumber(pncNumber: String): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .pncNumber(pncNumber)
    .build()

  private fun criteriaForCRONumber(croNumber: String): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .croNumber(croNumber)
    .build()

  private fun findOffender(criteria: PrisonerDetailSearchCriteria): PrisonerDetail {
    val page = repository.findOffenders(criteria, defaultPageRequest)

    assertThat(page.getItems()).hasSize(1)

    return page.getItems().get(0)
  }
}
