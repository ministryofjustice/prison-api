package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.api.support.Status
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.filters.NameFilter
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class UserRepositoryTest(
  @Autowired private val userRepository: UserRepository,
  @Autowired private val caseLoadRepository: CaseLoadRepository,
) {

  @Test
  fun testFindUserByUsername() {
    val user = userRepository.findByUsername("ITAG_USER").orElseThrow(
      EntityNotFoundException.withId("ITAG_USER"),
    )

    assertThat(user.lastName).isEqualTo("User")
  }

  @Test
  fun testFindUserByUsernameNotExists() {
    val user = userRepository.findByUsername("XXXXXXXX")
    assertThat(user).isNotPresent()
  }

  @Test
  fun testFindUsersByCaseload() {
    val page = userRepository.findUsersByCaseload(
      "LEI",
      null,
      NameFilter(null),
      Status.ALL,
      null,
      PageRequest("last_name", Order.ASC, 0L, 40L),
    )
    val items = page.getItems()

    assertThat(items).hasSizeBetween(5, 40)
    assertThat(items)
      .extracting<String, RuntimeException> { it.username }
      .contains("JBRIEN")
  }

  @Test
  fun testFindUsersByCaseloadInactiveOnly() {
    val page = userRepository.findUsersByCaseload(
      "LEI",
      null,
      NameFilter(null),
      Status.INACTIVE,
      null,
      PageRequest("last_name", Order.ASC, 0L, 5L),
    )
    val items = page.getItems()

    assertThat(items).hasSize(0)
  }

  @Test
  fun testFindUsersByCaseloadAndNameFilter() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("User"), Status.ALL, null, PageRequest())

    assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER")
  }

  @Test
  fun testFindUserByFullNameSearch() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("User Api"), Status.ALL, null, PageRequest())

    assertThat(usersByCaseload.getItems())
      .extracting<String, RuntimeException> { it.username }
      .containsExactly("ITAG_USER")
  }

  @Test
  fun testFindUserByFullNameSearchReversed() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("Api User"), Status.ALL, null, PageRequest())
    assertThat(usersByCaseload.getItems())
      .extracting<String, RuntimeException> { it.username }
      .containsExactly("ITAG_USER")
  }

  @Test
  fun testFindUserByFullNameSearchWithComma() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("User, Api"), Status.ALL, null, PageRequest())
    assertThat(usersByCaseload.getItems())
      .extracting<String, RuntimeException> { it.username }
      .containsExactly("ITAG_USER")
  }

  @Test
  fun testFindUserByFullNameSearchNoresults() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("Other Api"), Status.ALL, null, PageRequest())

    assertThat(usersByCaseload.getItems()).isEmpty()
  }

  @Test
  fun testFindUsersByCaseloadAndNameFilterUsername() {
    val usersByCaseload =
      userRepository.findUsersByCaseload("LEI", null, NameFilter("ITAG_USER"), Status.ALL, null, PageRequest())

    assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER")
  }

  @Test
  fun testFindUsersByCaseloadAndNameFilterAndAccessRoleFilter() {
    val usersByCaseload = userRepository.findUsersByCaseload(
      "LEI",
      listOf("OMIC_ADMIN"),
      NameFilter("User"),
      Status.ALL,
      null,
      PageRequest(),
    )

    assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER")
  }

  @Test
  fun testFindUsersByCaseloadAndAccessRoleFilter() {
    val usersByCaseload = userRepository.findUsersByCaseload(
      "LEI",
      listOf("OMIC_ADMIN"),
      NameFilter("User Api"),
      Status.ALL,
      null,
      PageRequest(),
    )

    assertThat(usersByCaseload.getItems())
      .extracting<String, RuntimeException> { it.username }
      .contains("ITAG_USER")
  }

  @Test
  fun testFindUsersByCaseloadAndAccessRoleFilterRoleNotAssigned() {
    val usersByCaseload = userRepository.findUsersByCaseload(
      "LEI",
      listOf("ACCESS_ROLE_GENERAL"),
      NameFilter("User"),
      Status.ALL,
      null,
      PageRequest(),
    )

    assertThat(usersByCaseload.getItems()).isEmpty()
  }

  @Test
  fun testFindUsersByCaseloadAndAccessRoleFilterRoleNotAnAccessRole() {
    val usersByCaseload = userRepository.findUsersByCaseload(
      "LEI",
      listOf("WING_OFF"),
      NameFilter("User"),
      Status.ALL,
      null,
      PageRequest(),
    )

    assertThat(usersByCaseload.getItems()).isEmpty()
  }

  @Test
  fun testFindUsersByCaseloadAndAccessRoleFilterNonExistantRole() {
    val usersByCaseload = userRepository.findUsersByCaseload(
      "LEI",
      listOf("OMIC_ADMIN_DOESNT_EXIST"),
      NameFilter("User"),
      Status.ALL,
      null,
      PageRequest(),
    )

    assertThat(usersByCaseload.getItems()).isEmpty()
  }

  @Test
  fun testUpdateWorkingCaseLoad() {
    // STAFF_USER_ACCOUNTS for genUsername and admUsername have the same staff_id (-1).
    val genUsername = "PRISON_API_USER"
    val admUsername = "PRISON_API_USER_ADM"

    assertThat(
      caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map { it.getCaseLoadId() },
    ).contains("LEI")
    assertThat(
      caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map { it.getCaseLoadId() },
    ).contains("CADM_I")
    userRepository.updateWorkingCaseLoad(genUsername, "NWEB")
    assertThat(
      caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map { it.getCaseLoadId() },
    ).contains("NWEB")
    assertThat(
      caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map { it.getCaseLoadId() },
    ).contains("CADM_I")
    // Restore the original so we don't break other tests
    userRepository.updateWorkingCaseLoad(genUsername, "LEI")
  }

  @Test
  fun testGetBasicInmateDetailsByBookingIds() {
    val users = userRepository.getUserListByUsernames(listOf("JBRIEN", "RENEGADE"))
    assertThat(users).extracting("username", "firstName", "staffId").contains(
      Tuple.tuple("JBRIEN", "Jo", -12L),
      Tuple.tuple("RENEGADE", "Renegade", -11L),
    )
  }
}
