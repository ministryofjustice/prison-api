package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.StaffDetail
import uk.gov.justice.hmpps.prison.repository.CaseLoadRepository
import uk.gov.justice.hmpps.prison.repository.StaffRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffJobRole
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffRole
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffJobRoleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository
import java.time.LocalDate
import java.util.Optional

class StaffServiceImplTest {
  private val multipleAddresses = mutableListOf("test1@a.com", "test2@b.com", "test3@c.com")
  private val singleAddress = mutableListOf("test1@a.com")
  private val emptyList = mutableListOf<String?>()

  private val staffRepository: StaffRepository = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val caseLoadRepository: CaseLoadRepository = mock()
  private val userCaseloadRoleRepository: UserCaseloadRoleRepository = mock()
  private val staffJobRoleRepository: StaffJobRoleRepository = mock()

  private val staffService = StaffService(
    staffRepository,
    staffUserAccountRepository,
    caseLoadRepository,
    userCaseloadRoleRepository,
    staffJobRoleRepository,
  )

  @Test
  fun testMultipleEmails() {
    whenever(staffRepository.findByStaffId(ID_MULTIPLE)).thenReturn(
      getValidStaffDetails(
        ID_MULTIPLE,
      ),
    )
    whenever(staffRepository.findEmailAddressesForStaffId(ID_MULTIPLE))
      .thenReturn(multipleAddresses)

    val addresses = staffService.getStaffEmailAddresses(ID_MULTIPLE)

    assertThat(addresses)
      .containsOnly(multipleAddresses[0], multipleAddresses[1], multipleAddresses[2])

    verify(staffRepository).findByStaffId(ID_MULTIPLE)
    verify(staffRepository).findEmailAddressesForStaffId(ID_MULTIPLE)
  }

  @Test
  fun testSingleEmail() {
    whenever(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(
      getValidStaffDetails(
        ID_SINGLE,
      ),
    )
    whenever(staffRepository.findEmailAddressesForStaffId(ID_SINGLE))
      .thenReturn(singleAddress)

    val addresses = staffService.getStaffEmailAddresses(ID_SINGLE)

    assertThat(addresses).containsOnly(singleAddress[0])

    verify(staffRepository).findByStaffId(ID_SINGLE)
    verify(staffRepository).findEmailAddressesForStaffId(ID_SINGLE)
  }

  @Test
  @Throws(NoContentException::class)
  fun testNoneExist() {
    whenever(staffRepository.findByStaffId(ID_NONE)).thenReturn(
      getValidStaffDetails(
        ID_NONE,
      ),
    )
    whenever(staffRepository.findEmailAddressesForStaffId(ID_NONE)).thenReturn(emptyList)

    assertThatThrownBy { staffService.getStaffEmailAddresses(ID_NONE) }
      .isInstanceOf(
        NoContentException::class.java,
      )

    verify(staffRepository).findByStaffId(ID_NONE)
    verify(staffRepository).findEmailAddressesForStaffId(ID_NONE)
  }

  @Test
  @Throws(EntityNotFoundException::class, NoContentException::class)
  fun testInvalidId() {
    whenever(staffRepository.findByStaffId(ID_BAD)).thenThrow(
      EntityNotFoundException.withId(ID_BAD),
    )

    assertThatThrownBy { staffService.getStaffEmailAddresses(ID_BAD) }
      .isInstanceOf(
        EntityNotFoundException::class.java,
      )

    verify(staffRepository).findByStaffId(ID_BAD)
  }

  @Test
  fun testFiltersOutOldRoles() {
    whenever(staffJobRoleRepository.findAllByAgencyIdAndStaffStaffId("MDI", -1L))
      .thenReturn(
        listOf(
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(1),
            "POS",
            "KW",
            StaffRole("KW", "Key worker 1"),
            null,
          ),
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(2),
            "POS",
            "KW",
            StaffRole("KW", "Key worker 2"),
            null,
          ),
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(3),
            "POS",
            "KW",
            StaffRole("KW", "Key worker 3"),
            LocalDate.now().minusDays(2),
          ),
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(5),
            "POS",
            "POM",
            StaffRole("POM", "POM 1"),
            LocalDate.now().minusDays(2),
          ),
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(2),
            "POS",
            "POM",
            StaffRole("POM", "POM 2"),
            null,
          ),
          StaffJobRole(
            Staff.builder().staffId(-1L).build(),
            AgencyLocation.builder().id("MDI").build(),
            LocalDate.now().minusDays(3),
            "POS",
            "POM",
            StaffRole("POM", "POM 3"),
            null,
          ),
        ),
      )

    val returnedData = staffService.getAllRolesForAgency(-1L, "MDI")

    assertThat(returnedData).hasSize(2)

    assertThat(returnedData[0].roleDescription).isEqualTo("POM 2")
    assertThat(returnedData[1].roleDescription).isEqualTo("Key worker 1")
  }

  private fun getValidStaffDetails(staffId: Long?): Optional<StaffDetail> {
    val staffDetail =
      StaffDetail.builder().staffId(staffId).firstName("Bob").lastName("Harris").status("ACTIVE").gender("M")
        .dateOfBirth(
          LocalDate.EPOCH,
        ).build()
    return Optional.of(staffDetail)
  }

  companion object {
    private const val ID_MULTIPLE = 8888L
    private const val ID_SINGLE = 9999L
    private const val ID_NONE = 9999L
    private const val ID_BAD = 123L
  }
}
