package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.exception.LockTimeoutException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.api.model.OffenderEmailAddressCreateRequest
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderInternetAddress
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderInternetAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.sql.SQLException
import java.util.Optional

class OffenderEmailsServiceTest {
  private val offenderInternetAddressRepository: OffenderInternetAddressRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val offenderEmailsService: OffenderEmailsService = OffenderEmailsService(
    offenderRepository,
    offenderInternetAddressRepository,
  )

  @BeforeEach
  fun setUp() {
    whenever(offenderRepository.findRootOffenderByNomsId(PRISONER_NUMBER)).thenReturn(
      Optional.of(OFFENDER),
    )
    whenever(offenderRepository.findRootOffenderByNomsId(PRISONER_NUMBER_NOT_FOUND)).thenReturn(Optional.empty())

    whenever(
      offenderInternetAddressRepository.findByRootNomsIdAndInternetAddressIdForUpdate(
        PRISONER_NUMBER,
        EXISTING_INTERNET_ADDRESS_ID,
      ),
    ).thenReturn(Optional.of(INTERNET_ADDRESS_ONE))

    whenever(
      offenderInternetAddressRepository.findByRootNomsIdAndInternetAddressIdForUpdate(
        PRISONER_NUMBER,
        INTERNET_ADDRESS_ID_NOT_FOUND,
      ),
    ).thenReturn(
      Optional.empty(),
    )

    whenever(
      offenderInternetAddressRepository.findByRootNomsIdAndInternetAddressIdForUpdate(
        PRISONER_NUMBER_LOCKED_TABLE,
        EXISTING_INTERNET_ADDRESS_ID,
      ),
    ).thenThrow(CannotAcquireLockException("test", LockTimeoutException("[ORA-30006]", SQLException())))

    whenever(offenderInternetAddressRepository.save(any<OffenderInternetAddress>())).thenReturn(INTERNET_ADDRESS_ONE)
  }

  @Test
  fun `getEmailsByPrisonerNumber for prisoner number`() {
    val emailAddresses = offenderEmailsService.getEmailsByPrisonerNumber(PRISONER_NUMBER)
    assertThat(emailAddresses).hasSize(1)
    assertThat(emailAddresses[0].emailAddressId).isEqualTo(INTERNET_ADDRESS_ID_ONE)
  }

  @Test
  fun `getEmailsByPrisonerNumber throws exception when prisoner not found`() {
    assertThatThrownBy { offenderEmailsService.getEmailsByPrisonerNumber(PRISONER_NUMBER_NOT_FOUND) }.isInstanceOf(
      EntityNotFoundException::class.java,
    ).hasMessageContaining("Prisoner with prisonerNumber $PRISONER_NUMBER_NOT_FOUND not found")
  }

  @Test
  fun `addOffenderEmailAddress adds the email address`() {
    val createdEmail = offenderEmailsService.addOffenderEmailAddress(
      PRISONER_NUMBER,
      OffenderEmailAddressCreateRequest(
        emailAddress = EmailToCreate.EMAIL_ADDRESS,
      ),
    )

    assertThat(createdEmail).isNotNull()
    assertThat(createdEmail.email).isEqualTo(INTERNET_ADDRESS_ONE.internetAddress)
  }

  @Test
  fun `addOffenderEmailAddress throws exception when prisoner not found`() {
    assertThatThrownBy {
      offenderEmailsService.addOffenderEmailAddress(
        PRISONER_NUMBER_NOT_FOUND,
        OffenderEmailAddressCreateRequest(
          emailAddress = EmailToCreate.EMAIL_ADDRESS,
        ),
      )
    }.isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Prisoner with prisonerNumber $PRISONER_NUMBER_NOT_FOUND not found")
  }

  @Test
  fun `updateOffenderEmailAddress updates the email address`() {
    val updatedEmail = offenderEmailsService.updateOffenderEmailAddress(
      PRISONER_NUMBER,
      EXISTING_INTERNET_ADDRESS_ID,
      OffenderEmailAddressCreateRequest(
        emailAddress = EmailToCreate.EMAIL_ADDRESS,
      ),
    )

    assertThat(updatedEmail).isNotNull()
    assertThat(updatedEmail.email).isEqualTo(INTERNET_ADDRESS_ONE.internetAddress)
  }

  @Test
  fun `updateOffenderEmailAddress throws exception when prisoner number not found`() {
    assertThatThrownBy {
      offenderEmailsService.updateOffenderEmailAddress(
        PRISONER_NUMBER,
        INTERNET_ADDRESS_ID_NOT_FOUND,
        OffenderEmailAddressCreateRequest(
          emailAddress = EmailToCreate.EMAIL_ADDRESS,
        ),
      )
    }.isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Email address with prisoner number $PRISONER_NUMBER and email address ID $INTERNET_ADDRESS_ID_NOT_FOUND not found")
  }

  @Test
  fun `updateOffenderEmailAddress throws exception when database table is locked`() {
    assertThatThrownBy {
      offenderEmailsService.updateOffenderEmailAddress(
        PRISONER_NUMBER_LOCKED_TABLE,
        EXISTING_INTERNET_ADDRESS_ID,
        OffenderEmailAddressCreateRequest(
          emailAddress = EmailToCreate.EMAIL_ADDRESS,
        ),
      )
    }.isInstanceOf(DatabaseRowLockedException::class.java)
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234BC"
    const val PRISONER_NUMBER_NOT_FOUND = "A4321BC"
    const val PRISONER_NUMBER_LOCKED_TABLE = "A1212BC"
    const val EXISTING_INTERNET_ADDRESS_ID = 54321L
    const val INTERNET_ADDRESS_ID_NOT_FOUND = 12345L
    const val INTERNET_ADDRESS_ID_ONE = 54321L

    val INTERNET_ADDRESS_ONE =
      OffenderInternetAddress.builder().internetAddressId(INTERNET_ADDRESS_ID_ONE).internetAddress("foo@bar.example").build()

    data object EmailToCreate {
      const val EMAIL_ADDRESS = "foo@bar.example"
    }

    val OFFENDER = Offender().apply {
      id = 123
      nomsId = PRISONER_NUMBER
      emailAddresses = listOf(INTERNET_ADDRESS_ONE)
    }
  }
}
