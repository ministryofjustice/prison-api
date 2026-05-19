package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.UserDetail
import uk.gov.justice.hmpps.prison.repository.UserRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository

/**
 * Test cases for [UserService].
 */
class UserServiceImplTest {
  private val userRepository: UserRepository = mock()
  private val userCaseloadRoleRepository: UserCaseloadRoleRepository = mock()
  private val caseLoadService: CaseLoadService = mock()

  private val userService: UserService = UserService(caseLoadService, userRepository, userCaseloadRoleRepository, API_CASELOAD_ID, 100)

  @Test
  fun testGetOffenderCategorisationsBatching() {
    val setOf150Strings = (1..150).map { it.toString() }.toSet()

    val detail2 = UserDetail.builder().staffId(-3L).lastName("B").build()
    val detail1 = UserDetail.builder().staffId(-2L).lastName("C").build()

    whenever(userRepository.getUserListByUsernames(any())).thenReturn(listOf(detail2, detail1))

    val results = userService.getUserListByUsernames(setOf150Strings)

    assertThat(results).hasSize(4)

    verify(userRepository, Mockito.times(2)).getUserListByUsernames(anyList())
  }

  companion object {
    private const val API_CASELOAD_ID = "NWEB"
  }
}
