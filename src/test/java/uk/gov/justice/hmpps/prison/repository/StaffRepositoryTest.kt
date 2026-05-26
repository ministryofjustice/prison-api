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
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.time.LocalDate

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class StaffRepositoryTest(
  @Autowired private val repository: StaffRepository,
) {
  @Test
  fun testFindStaffDetailsByValidStaffId() {
    val testStaffId = -1L

    val staffDetail = repository.findByStaffId(testStaffId)
      .orElseThrow(EntityNotFoundException.withId(testStaffId))

    assertThat(staffDetail.firstName).isEqualTo("PRISON")
  }

  @Test
  fun testFindStaffDetailsByInvalidStaffId() {
    val staffDetail = repository.findByStaffId(9999999999L)

    assertThat(staffDetail).isNotPresent()
  }

  @Test
  fun testFindStaffByPersonnelIdentifierInvalidIdentifier() {
    val staffDetail = repository.findStaffByPersonnelIdentifier("X", "X")

    assertThat(staffDetail).isNotPresent()
  }

  @Test
  fun testFindStaffByPersonnelIdentifierWrongIdType() {
    val testIdType = "SYS2"
    val testId = "sysuser@system1.com"

    assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty()
  }

  @Test
  fun testFindStaffByPersonnelIdentifierWrongId() {
    val testIdType = "SYS1"
    val testId = "sysuser@system2.com"

    assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty()
  }

  @Test
  fun testFindStaffByPersonnelIdentifierDuplicateIdentifier() {
    val testIdType = "SYS9"
    val testId = "sysuser@system9.com"

    assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty()
  }

  @Test
  fun testFindStaffByPersonnelIdentifierActive() {
    val testIdType = "SYS1"
    val testId = "sysuser@system1.com"

    val staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
      .orElseThrow(EntityNotFoundException.withId(testId))

    assertThat(staffDetail.staffId).isEqualTo(-1L)
    assertThat(staffDetail.firstName).isEqualTo("PRISON")
    assertThat(staffDetail.status).isEqualTo("ACTIVE")
  }

  @Test
  fun testFindStaffByPersonnelIdentifierInactive() {
    val testIdType = "ITAG"
    val testId = "ex.officer5@itag.com"

    val staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
      .orElseThrow(EntityNotFoundException.withId(testId))

    assertThat(staffDetail.staffId).isEqualTo(-10L)
    assertThat(staffDetail.firstName).isEqualTo("EX")
    assertThat(staffDetail.status).isEqualTo("INACT")
  }

  @Test
  fun testFindStaffLocationRolesByRole() {
    val testAgency = "SYI"
    val testAgencyDescription = "Shrewsbury"
    val testRole = "KW"

    val page = repository.findStaffByAgencyRole(testAgency, testRole, null, null, true, PageRequest())

    val items = page.getItems()

    assertThat(items).hasSize(1)

    val slr = items[0]

    assertThat(slr.agencyId).isEqualTo(testAgency)
    assertThat(slr.agencyDescription).isEqualTo(testAgencyDescription)
    assertThat(slr.staffId).isEqualTo(-9)
    assertThat(slr.firstName).isEqualTo("WING")
    assertThat(slr.lastName).isEqualTo("OFFICER4")
    assertThat(slr.fromDate).isEqualTo(LocalDate.of(2018, 1, 2))
    assertThat(slr.toDate).isNull()
    assertThat(slr.position).isEqualTo("AO")
    assertThat(slr.positionDescription).isEqualTo("Admin Officer")
    assertThat(slr.role).isEqualTo(testRole)
    assertThat(slr.roleDescription).isEqualTo("Key Worker")
    assertThat(slr.scheduleType).isEqualTo("FT")
    assertThat(slr.scheduleTypeDescription).isEqualTo("Full Time")
    assertThat(slr.hoursPerWeek).isEqualByComparingTo("11")
  }

  @Test
  fun testFindStaffLocationRolesByRole_handles_special_character() {
    val testAgency = "LEI"
    val testRole = "KW"
    val nameFilter = "O'brien"

    val page = repository.findStaffByAgencyRole(testAgency, testRole, nameFilter, null, true, PageRequest())

    val items = page.getItems()

    assertThat(items).hasSize(1)

    val slr = items[0]

    assertThat(slr.agencyId).isEqualTo(testAgency)
    assertThat(slr.staffId).isEqualTo(-12)
    assertThat(slr.firstName).isEqualTo("JO")
    assertThat(slr.lastName).isEqualTo("O'BRIEN")
    assertThat(slr.position).isEqualTo("AO")
    assertThat(slr.roleDescription).isEqualTo("Key Worker")
  }

  @Test
  fun testFindStaffLocationRolesByPositionAndRole() {
    val testAgency = "LEI"
    val testPosition = "PRO"
    val testAgencyDescription = "Leeds"
    val testRole = "OS"

    val page =
      repository.findStaffByAgencyPositionRole(testAgency, testPosition, testRole, null, null, true, PageRequest())

    val items = page.getItems()

    assertThat(items).hasSize(1)

    val slr = items[0]

    assertThat(slr.agencyId).isEqualTo(testAgency)
    assertThat(slr.agencyDescription).isEqualTo(testAgencyDescription)
    assertThat(slr.staffId).isEqualTo(-2)
    assertThat(slr.firstName).isEqualTo("API")
    assertThat(slr.lastName).isEqualTo("USER")
    // assertThat(slr.getEmail()).isEqualTo("itaguser@syscon.net");
    assertThat(slr.fromDate).isEqualTo(LocalDate.of(2016, 8, 8))
    assertThat(slr.toDate).isNull()
    assertThat(slr.position).isEqualTo("PRO")
    assertThat(slr.positionDescription).isEqualTo("Prison Officer")
    assertThat(slr.role).isEqualTo("OS")
    assertThat(slr.roleDescription).isEqualTo("Offender Supervisor")
    assertThat(slr.scheduleType).isEqualTo("FT")
    assertThat(slr.scheduleTypeDescription).isEqualTo("Full Time")
    assertThat(slr.hoursPerWeek).isEqualByComparingTo("7")
  }

  @Test
  fun testFindStaffLocationRolesByStaffId() {
    val testAgency = "LEI"
    val testRole = "KW"

    val page = repository.findStaffByAgencyRole(
      testAgency,
      testRole,
      null,
      -1L,
      true,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(1)

    val slr = items[0]
    assertThat(slr.agencyId).isEqualTo(testAgency)
    assertThat(slr.staffId).isEqualTo(-1)
    assertThat(slr.role).isEqualTo(testRole)
  }

  @Test
  fun testFindStaffLocationRolesByStaffIdActiveOnly() {
    val testAgency = "SYI"
    val testRole = "KW"

    val page = repository.findStaffByAgencyRole(
      testAgency,
      testRole,
      null,
      -10L,
      true,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(0)
  }

  @Test
  fun testFindStaffLocationRolesByStaffIdDontIncludeInactive() {
    val testAgency = "SYI"
    val testRole = "KW"

    val page = repository.findStaffByAgencyRole(
      testAgency,
      testRole,
      null,
      -10L,
      false,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(0)
  }

  @Test
  fun testFindStaffLocationPositionRolesByStaffId() {
    val testAgency = "LEI"
    val testPosition = "AO"
    val testRole = "KW"

    val page = repository.findStaffByAgencyPositionRole(
      testAgency,
      testPosition,
      testRole,
      null,
      -4L,
      true,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(1)

    val slr = items[0]
    assertThat(slr.agencyId).isEqualTo(testAgency)
    assertThat(slr.staffId).isEqualTo(-4)
  }

  @Test
  fun testFindStaffLocationRolesByInvalidStaffId() {
    val testAgency = "LEI"
    val testRole = "KW"

    val page = repository.findStaffByAgencyRole(
      testAgency,
      testRole,
      null,
      -999999L,
      true,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(0)
  }

  @Test
  fun testFindStaffLocationPositionRolesByInvalidStaffId() {
    val testAgency = "LEI"
    val testPosition = "AO"
    val testRole = "KW"

    val page = repository.findStaffByAgencyPositionRole(
      testAgency,
      testPosition,
      testRole,
      null,
      -999999L,
      true,
      PageRequest(),
    )

    val items = page.getItems()

    assertThat(items).hasSize(0)
  }

  @Test
  fun testEmailAddresses() {
    val validStaffId = -1L

    val staffDetail = repository.findByStaffId(validStaffId)
      .orElseThrow(EntityNotFoundException.withId(validStaffId))
    assertThat(staffDetail).isNotNull()

    // The data has a single email address configured for this user
    val staffEmails = repository.findEmailAddressesForStaffId(validStaffId)
    assertThat(staffEmails).containsOnly("prison-api-user@test.com")
  }

  @Test
  fun testNoEmailAddresses() {
    val validStaffId = -3L

    val staffDetail = repository.findByStaffId(validStaffId)
    assertThat(staffDetail).isPresent()

    // The data has no email addresses for this staff member
    val staffEmails = repository.findEmailAddressesForStaffId(validStaffId)
    assertThat(staffEmails).isEmpty()
  }
}
