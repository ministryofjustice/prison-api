package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class CaseLoadRepositoryTest(
  @Autowired private val repository: CaseLoadRepository,
) {
  @Test
  fun testGetCaseLoad() {
    val caseLoad = repository.getCaseLoad("LEI")

    assertThat(caseLoad).get().extracting { it.description }.isEqualTo("Leeds (HMP)")
  }

  @Test
  fun testGetAllCaseLoadsByUsername() {
    val caseLoads = repository.getAllCaseLoadsByUsername(TEST_USERNAME)
    assertThat(caseLoads)
      .extracting<String, RuntimeException> { it.caseLoadId }
      .containsOnly("LEI", "BXI", "MDI", "RNI", "SYI", "WAI", "NWEB")
  }

  @Test
  fun testGetCaseLoadsByUsername() {
    val caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME)
    assertThat(caseLoads)
      .extracting<String, RuntimeException> { it.caseLoadId }
      .containsOnly("LEI", "BXI", "MDI", "RNI", "SYI", "WAI")
  }

  @Test
  fun testGetCaseLoadsByUsername_CurrentActive() {
    val caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME)
    val activeCaseLoads = caseLoads.filter { it.isCurrentlyActive }
    assertThat(activeCaseLoads)
      .extracting<String, RuntimeException> { it.caseLoadId }
      .containsOnly("LEI")
  }

  @Test
  fun testGetCaseLoadsByUsername_CurrentInactive() {
    val caseLoads = repository.getCaseLoadsByUsername(TEST_USERNAME)
    val activeCaseLoads = caseLoads.filter { !it.isCurrentlyActive }
    assertThat(activeCaseLoads)
      .extracting<String, RuntimeException> { it.caseLoadId }
      .containsOnly("BXI", "MDI", "RNI", "SYI", "WAI")
  }

  @Test
  fun testGetWorkingCaseLoadByUsername() {
    val caseLoad = repository.getWorkingCaseLoadByUsername(TEST_USERNAME)

    assertThat(caseLoad).get().extracting { it.description }.isEqualTo("Leeds (HMP)")
  }

  companion object {
    private const val TEST_USERNAME = "ITAG_USER"
  }
}
