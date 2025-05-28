package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.OffenderPhoneNumberCreateRequest
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPhoneRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.util.Optional

class OffenderPhonesServiceTest {
  private val offenderPhoneRepository: OffenderPhoneRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val offenderPhonesService: OffenderPhonesService = OffenderPhonesService(
    offenderRepository,
    offenderPhoneRepository,
    referenceDomainService,
  )

  @BeforeEach
  fun setUp() {
    whenever(offenderRepository.findRootOffenderByNomsId(PRISONER_NUMBER)).thenReturn(
      Optional.of(OFFENDER),
    )
    whenever(offenderRepository.findRootOffenderByNomsId(PRISONER_NUMBER_NOT_FOUND)).thenReturn(Optional.empty())

    whenever(referenceDomainService.isReferenceCodeActive("PHONE_USAGE", VALID_PHONE_TYPE)).thenReturn(true)
    whenever(referenceDomainService.isReferenceCodeActive("PHONE_USAGE", VALID_PHONE_TYPE_2)).thenReturn(true)
    whenever(referenceDomainService.isReferenceCodeActive("PHONE_USAGE", INVALID_PHONE_TYPE)).thenReturn(false)

    whenever(
      offenderPhoneRepository.findByRootNomsIdAndPhoneId(
        PRISONER_NUMBER,
        EXISTING_PHONE_ID,
      ),
    ).thenReturn(Optional.of(PHONE_NUMBER_ONE))

    whenever(
      offenderPhoneRepository.findByRootNomsIdAndPhoneId(
        PRISONER_NUMBER,
        PHONE_ID_NOT_FOUND,
      ),
    ).thenReturn(
      Optional.empty(),
    )

    whenever(offenderPhoneRepository.save(any())).thenReturn(PHONE_NUMBER_ONE)
  }

  @Test
  fun `getPhoneNumbersByOffenderNo for prisoner number`() {
    val phoneNumbers = offenderPhonesService.getPhoneNumbersByOffenderNo(PRISONER_NUMBER)
    assertThat(phoneNumbers).hasSize(1)
    assertThat(phoneNumbers[0].phoneId).isEqualTo(PHONE_NUMBER_ONE_ID)
  }

  @Test
  fun `getPhoneNumbersByOffenderNo throws exception when prisoner not found`() {
    assertThatThrownBy { offenderPhonesService.getPhoneNumbersByOffenderNo(PRISONER_NUMBER_NOT_FOUND) }.isInstanceOf(
      EntityNotFoundException::class.java,
    ).hasMessageContaining("Prisoner with prisonerNumber $PRISONER_NUMBER_NOT_FOUND not found")
  }

  @Test
  fun `addOffenderNumber adds the phone number`() {
    val createdPhone = offenderPhonesService.addOffenderPhoneNumber(
      PRISONER_NUMBER,
      OffenderPhoneNumberCreateRequest(
        phoneNumberType = PhoneToCreate.TYPE,
        phoneNumber = PhoneToCreate.NUMBER,
      ),
    )

    assertThat(createdPhone).isNotNull()
    assertThat(createdPhone.phoneNo).isEqualTo(PHONE_NUMBER_ONE.phoneNo)
    assertThat(createdPhone.phoneType).isEqualTo(PHONE_NUMBER_ONE.phoneType)
  }

  @Test
  fun `addOffenderPhoneNumber throws exception when prisoner not found`() {
    assertThatThrownBy {
      offenderPhonesService.addOffenderPhoneNumber(
        PRISONER_NUMBER_NOT_FOUND,
        OffenderPhoneNumberCreateRequest(
          phoneNumberType = PhoneToCreate.TYPE,
          phoneNumber = PhoneToCreate.NUMBER,
        ),
      )
    }.isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Prisoner with prisonerNumber $PRISONER_NUMBER_NOT_FOUND not found")
  }

  @Test
  fun `addOffenderPhoneNumber throws exception when phone number type is not found`() {
    assertThatThrownBy {
      offenderPhonesService.addOffenderPhoneNumber(
        PRISONER_NUMBER,
        OffenderPhoneNumberCreateRequest(
          phoneNumberType = INVALID_PHONE_TYPE,
          phoneNumber = PhoneToCreate.NUMBER,
        ),
      )
    }.isInstanceOf(BadRequestException::class.java).hasMessageContaining("Phone number type NOT_REAL is not valid")
  }

  @Test
  fun `updateOffenderPhoneNumber updates the phone number`() {
    val updatedPhone = offenderPhonesService.updateOffenderPhoneNumber(
      PRISONER_NUMBER,
      EXISTING_PHONE_ID,
      OffenderPhoneNumberCreateRequest(
        phoneNumberType = PhoneToCreate.TYPE,
        phoneNumber = PhoneToCreate.NUMBER,
      ),
    )

    assertThat(updatedPhone).isNotNull()
    assertThat(updatedPhone.phoneNo).isEqualTo(PHONE_NUMBER_ONE.phoneNo)
    assertThat(updatedPhone.phoneType).isEqualTo(PHONE_NUMBER_ONE.phoneType)
  }

  @Test
  fun `updateOffenderPhoneNumber throws exception when prisoner number not found`() {
    assertThatThrownBy {
      offenderPhonesService.updateOffenderPhoneNumber(
        PRISONER_NUMBER,
        PHONE_ID_NOT_FOUND,
        OffenderPhoneNumberCreateRequest(
          phoneNumberType = PhoneToCreate.TYPE,
          phoneNumber = PhoneToCreate.NUMBER,
        ),
      )
    }.isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Phone number with prisoner number $PRISONER_NUMBER and phone ID $PHONE_ID_NOT_FOUND not found")
  }

  @Test
  fun `updateOffenderPhoneNumber throws exception when phone number type is not found`() {
    assertThatThrownBy {
      offenderPhonesService.updateOffenderPhoneNumber(
        PRISONER_NUMBER,
        EXISTING_PHONE_ID,
        OffenderPhoneNumberCreateRequest(
          phoneNumberType = INVALID_PHONE_TYPE,
          phoneNumber = PhoneToCreate.NUMBER,
        ),
      )
    }.isInstanceOf(BadRequestException::class.java).hasMessageContaining("Phone number type NOT_REAL is not valid")
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234BC"
    const val PRISONER_NUMBER_NOT_FOUND = "A4321BC"
    const val EXISTING_PHONE_ID = 54321L
    const val PHONE_ID_NOT_FOUND = 12345L
    const val VALID_PHONE_TYPE = "BUS"
    const val VALID_PHONE_TYPE_2 = "HOME"
    const val INVALID_PHONE_TYPE = "NOT_REAL"

    const val PHONE_NUMBER_ONE_ID = 54321L
    val PHONE_NUMBER_ONE =
      OffenderPhone.builder().phoneId(PHONE_NUMBER_ONE_ID).phoneType(VALID_PHONE_TYPE).phoneNo("01234 567 890")
        .offender(Offender().apply { id = 123 }).build()

    data object PhoneToCreate {
      const val TYPE = VALID_PHONE_TYPE_2
      const val NUMBER = "07878 787878"
    }

    val OFFENDER = Offender().apply {
      id = 123
      nomsId = PRISONER_NUMBER
      phones = listOf(PHONE_NUMBER_ONE)
    }
  }
}
