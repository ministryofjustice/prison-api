package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.AddressDto
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMark
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkImageDetail
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords
import uk.gov.justice.hmpps.prison.api.model.PrisonerProfileSummaryDto
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.model.ReferenceDataValue
import uk.gov.justice.hmpps.prison.api.model.Telephone
import java.time.LocalDate
import java.time.LocalDateTime

class PrisonerProfilePersonServiceTest {

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService = mock()
  private val offenderPhonesService: OffenderPhonesService = mock()
  private val offenderEmailsService: OffenderEmailsService = mock()
  private val offenderAddressService: OffenderAddressService = mock()
  private val offenderMilitaryRecordService: OffenderMilitaryRecordService = mock()
  private val distinguishingMarkService: DistinguishingMarkService = mock()

  private val service = PrisonerProfilePersonService(
    prisonerProfileUpdateService = prisonerProfileUpdateService,
    offenderPhonesService = offenderPhonesService,
    offenderEmailsService = offenderEmailsService,
    offenderAddressService = offenderAddressService,
    offenderMilitaryRecordService = offenderMilitaryRecordService,
    distinguishingMarkService = distinguishingMarkService,
  )

  private val prisonerNumber = "A1234AA"

  private val alias = CorePersonRecordAlias(
    prisonerNumber = prisonerNumber,
    offenderId = 543548L,
    workingNameBookingId = 111222L,
    firstName = "John",
    middleName1 = "Michael",
    middleName2 = "Andrew",
    lastName = "Smith",
    dateOfBirth = LocalDate.of(1980, 2, 28),
    nameTypeCode = "PRI",
    nameTypeDescription = "Prison Name",
    titleCode = "MR",
    titleDescription = "Mr",
    sexCode = "M",
    sexDescription = "Male",
    ethnicityCode = "W1",
    ethnicityDescription = "White: British",
  )

  private val address = AddressDto()

  private val phone = Telephone(
    1L,
    "07123456789",
    "MOBILE",
    "123",
  )

  private val email = Email(
    1L,
    "test@example.com",
  )

  private val militaryRecord = MilitaryRecord(
    bookingId = 1234567L,
    militarySeq = 1,
    warZoneCode = "AFG",
    warZoneDescription = "Afghanistan",
    startDate = LocalDate.of(2017, 6, 1),
    endDate = LocalDate.of(2019, 12, 1),
    militaryDischargeCode = "HON",
    militaryDischargeDescription = "Honourable",
    militaryBranchCode = "ARM",
    militaryBranchDescription = "Army",
    description = "Deployed to Afghanistan",
    unitNumber = "2nd Battalion",
    enlistmentLocation = "Windsor",
    dischargeLocation = "Colchester",
    selectiveServicesFlag = false,
    militaryRankCode = "CPL",
    militaryRankDescription = "Corporal",
    serviceNumber = "2345678",
    disciplinaryActionCode = "CM",
    disciplinaryActionDescription = "Court Martial",
  )

  private val referenceDataValue = ReferenceDataValue(
    domain = "DM",
    code = "CD",
    description = "DESC",
  )

  private val physicalAttributes = CorePersonPhysicalAttributes(
    height = 180,
    weight = 70,
    hair = referenceDataValue,
    facialHair = referenceDataValue,
    face = referenceDataValue,
    build = referenceDataValue,
    leftEyeColour = referenceDataValue,
    rightEyeColour = referenceDataValue,
    shoeSize = "10",
  )

  private val distinguishingMark = DistinguishingMark(
    id = 1,
    bookingId = 1234567L,
    offenderNo = prisonerNumber,
    bodyPart = ReferenceCode(),
    markType = ReferenceCode(),
    side = ReferenceCode(),
    partOrientation = ReferenceCode(),
    comment = "Scar on left arm",
    createdAt = LocalDateTime.of(2020, 5, 10, 14, 30),
    createdBy = "USER1",
    photographUuids = listOf(DistinguishingMarkImageDetail(id = 98765L, latest = true)),
  )

  private fun assertEntityNotFound(action: () -> Unit) {
    val ex = assertThrows<EntityNotFoundException> { action() }
    assertThat(ex.message).isEqualTo("test")
  }

  private val fullPerson = PrisonerProfileSummaryDto(
    aliases = listOf(alias),
    addresses = listOf(address),
    phones = listOf(phone),
    emails = listOf(email),
    militaryRecord = MilitaryRecords(listOf(militaryRecord)),
    physicalAttributes = physicalAttributes,
    distinguishingMarks = listOf(distinguishingMark),
  )

  @BeforeEach
  fun beforeEach() {
    whenever(prisonerProfileUpdateService.getAliases(prisonerNumber)).thenReturn(listOf(alias))
    whenever(offenderAddressService.getAddressesByOffenderNo(prisonerNumber)).thenReturn(listOf(address))
    whenever(offenderPhonesService.getPhoneNumbersByOffenderNo(prisonerNumber)).thenReturn(listOf(phone))
    whenever(offenderEmailsService.getEmailsByPrisonerNumber(prisonerNumber)).thenReturn(listOf(email))
    whenever(offenderMilitaryRecordService.getMilitaryRecords(prisonerNumber)).thenReturn(MilitaryRecords(listOf(militaryRecord)))
    whenever(prisonerProfileUpdateService.getPhysicalAttributes(prisonerNumber)).thenReturn(physicalAttributes)
    whenever(distinguishingMarkService.findMarksForLatestBooking(prisonerNumber)).thenReturn(listOf(distinguishingMark))
  }

  @Test
  fun `getPerson returns a full person`() {
    val result = service.getPerson(prisonerNumber)
    assertThat(result).isEqualTo(fullPerson)
  }

  @Test
  fun `getPerson handles when military records aren't available`() {
    whenever(offenderMilitaryRecordService.getMilitaryRecords(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    val result = service.getPerson(prisonerNumber)
    val expected = fullPerson.copy(militaryRecord = null)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getPerson handles when physical attributes aren't available due to EntityNotFoundException`() {
    whenever(prisonerProfileUpdateService.getPhysicalAttributes(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    val result = service.getPerson(prisonerNumber)
    val expected = fullPerson.copy(physicalAttributes = null)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getPerson propagates EntityNotFoundException when offender isn't found by prisonerProfileUpdateService`() {
    whenever(prisonerProfileUpdateService.getAliases(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    assertEntityNotFound { service.getPerson(prisonerNumber) }
  }

  @Test
  fun `getPerson propagates EntityNotFoundException when offender isn't found by offenderEmailsService`() {
    whenever(offenderEmailsService.getEmailsByPrisonerNumber(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    assertEntityNotFound { service.getPerson(prisonerNumber) }
  }

  @Test
  fun `getPerson propagates EntityNotFoundException when offender isn't found by offenderPhonesService`() {
    whenever(offenderPhonesService.getPhoneNumbersByOffenderNo(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    assertEntityNotFound { service.getPerson(prisonerNumber) }
  }

  @Test
  fun `getPerson propagates NoSuchElementException when offender isn't found by offenderAddressService`() {
    whenever(offenderAddressService.getAddressesByOffenderNo(prisonerNumber))
      .thenThrow(EntityNotFoundException("test"))

    assertEntityNotFound { service.getPerson(prisonerNumber) }
  }
}
