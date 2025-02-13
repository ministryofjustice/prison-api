package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMarkDetails
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.ReferenceDataRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifyingMark
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifyingMarkRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class IdentifyingMarkServiceImplTest {
  private val identifyingMarkRepository: OffenderIdentifyingMarkRepository = mock()
  private val imageRepository: OffenderImageRepository = mock()
  private val bookingRepository: OffenderBookingRepository = mock()
  private val referenceDataRepository: ReferenceDataRepository = mock()

  private var service = IdentifyingMarkService(
    identifyingMarkRepository,
    imageRepository,
    bookingRepository,
    referenceDataRepository,
  )

  @Test
  fun `get identifying marks for latest booking`() {
    val offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking = OffenderBooking.builder().offender(offender).build()
    val identifyingMarks = listOf(
      OffenderIdentifyingMark.builder()
        .bookingId(-1L)
        .offenderBooking(booking)
        .sequenceId(1)
        .markType("TAT")
        .bodyPart("TORSO")
        .commentText("Some comment")
        .build(),
      OffenderIdentifyingMark.builder()
        .bookingId(-1L)
        .offenderBooking(booking)
        .sequenceId(2)
        .markType("TAT")
        .bodyPart("ARM")
        .side("L")
        .partOrientation("UPP")
        .images(
          listOf(
            OffenderImage.builder().id(1L).imageObjectId(1).build(),
            OffenderImage.builder().id(2L).imageObjectId(2).build(),
          ),
        )
        .build(),
    )
    whenever(identifyingMarkRepository.findAllMarksForLatestBooking(anyString())).thenReturn(identifyingMarks)

    val expected = listOf(
      IdentifyingMark.builder()
        .bookingId(-1L)
        .id(1)
        .offenderNo(OFFENDER_ID)
        .markType("TAT")
        .bodyPart("TORSO")
        .comment("Some comment")
        .photographUuids(listOf())
        .build(),
      IdentifyingMark.builder()
        .bookingId(-1L)
        .id(2)
        .offenderNo(OFFENDER_ID)
        .markType("TAT")
        .bodyPart("ARM")
        .side("L")
        .partOrientation("UPP")
        .photographUuids(
          listOf(
            IdentifyingMark.IdentifyingMarkImageDetail(1L, false),
            IdentifyingMark.IdentifyingMarkImageDetail(2L, true),
          ),
        )
        .build(),
    )

    val response = service.findIdentifyingMarksForLatestBooking(OFFENDER_ID)

    verify(identifyingMarkRepository).findAllMarksForLatestBooking(OFFENDER_ID)
    assertThat(response).isEqualTo(expected)
  }

  @Test
  fun `get specific identifying mark for latest booking`() {
    val offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking = OffenderBooking.builder().offender(offender).build()
    val identifyingMark = OffenderIdentifyingMark.builder()
      .bookingId(-1L)
      .offenderBooking(booking)
      .sequenceId(2)
      .markType("TAT")
      .bodyPart("ARM")
      .side("L")
      .partOrientation("UPP")
      .images(
        listOf(
          OffenderImage.builder().id(1L).imageObjectId(1).build(),
          OffenderImage.builder().id(2L).imageObjectId(2).build(),
        ),
      )
      .build()

    whenever(
      identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(
        anyString(),
        anyInt(),
      ),
    ).thenReturn(identifyingMark)

    val expected = IdentifyingMark.builder()
      .bookingId(-1L)
      .id(2)
      .offenderNo(OFFENDER_ID)
      .markType("TAT")
      .bodyPart("ARM")
      .side("L")
      .partOrientation("UPP")
      .photographUuids(
        listOf(
          IdentifyingMark.IdentifyingMarkImageDetail(1L, false),
          IdentifyingMark.IdentifyingMarkImageDetail(2L, true),
        ),
      )
      .build()

    val response = service.getIdentifyingMarkForLatestBooking(OFFENDER_ID, 2)

    verify(identifyingMarkRepository).getMarkForLatestBookingByOffenderNumberAndSequenceId(OFFENDER_ID, 2)
    assertThat(response).isEqualTo(expected)
  }

  @Test
  fun `Add photo to existing identifying mark`() {
    val offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking = OffenderBooking.builder().offender(offender).build()
    val identifyingMark = OffenderIdentifyingMark.builder()
      .bookingId(-1L)
      .offenderBooking(booking)
      .sequenceId(2)
      .markType("TAT")
      .bodyPart("ARM")
      .side("L")
      .partOrientation("UPP")
      .images(listOf())
      .build()

    whenever(
      identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(
        anyString(),
        anyInt(),
      ),
    ).thenReturn(identifyingMark)

    val imageCaptor = argumentCaptor<OffenderImage>()

    service.addPhotoToMark(OFFENDER_ID, 2, ByteArrayInputStream(IMAGE_DATA))

    verify(imageRepository).save(imageCaptor.capture())
    val savedImage = imageCaptor.firstValue
    assertThat(savedImage.orientationType).isEqualTo("ARM")
    assertThat(savedImage.imageType).isEqualTo("OFF_IDM")
    assertThat(savedImage.viewType).isEqualTo("TAT")
    assertThat(savedImage.isActive).isTrue()
    assertThat(savedImage.imageObjectId).isEqualTo(2)
    assertThat(savedImage.fullSizeImage).isEqualTo(IMAGE_DATA)
    assertThat(savedImage.thumbnailImage).isNotEmpty()
  }

  @Nested
  @DisplayName("Update existing identifying mark")
  inner class UpdateExistingMark {

    @BeforeEach
    fun setup() {
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("MARK_TYPE", "SCAR", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("BODY_PART", "ARM", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("SIDE", "L", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("PART_ORIENT", "UPP", false))
        .thenReturn(Optional.of(ReferenceCode()))
    }

    @Test
    fun `Update existing mark`() {
      val offender = Offender.builder().nomsId(OFFENDER_ID).build()
      val booking = OffenderBooking.builder().offender(offender).build()
      val existingMark = OffenderIdentifyingMark.builder()
        .bookingId(-1L)
        .offenderBooking(booking)
        .sequenceId(1)
        .markType("TAT")
        .bodyPart("TORSO")
        .commentText("Some comment")
        .build()
      whenever(identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(OFFENDER_ID, 1))
        .thenReturn(existingMark)

      val updateRequest = IdentifyingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = IdentifyingMark.builder()
        .bookingId(-1L)
        .id(1)
        .offenderNo(OFFENDER_ID)
        .markType("SCAR")
        .bodyPart("ARM")
        .side("L")
        .partOrientation("UPP")
        .comment("Old wound")
        .photographUuids(listOf())
        .build()
      val response = service.updateIdentifyingMark(OFFENDER_ID, 1, updateRequest)

      assertThat(response).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reference code not found")
    @ValueSource(strings = ["MARK_TYPE", "BODY_PART", "SIDE", "PART_ORIENT"])
    fun `Reference code not found`(domain: String) {
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode(eq(domain), anyString(), anyBoolean()))
        .thenReturn(Optional.empty())
      val updateRequest = IdentifyingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")

      val exception = assertThrows<BadRequestException> { service.updateIdentifyingMark(OFFENDER_ID, 1, updateRequest) }
      assertThat(exception.message).isEqualTo("Reference code not found: $domain")
    }
  }

  @Nested
  @DisplayName("Create new identifying mark")
  inner class CreateMark {
    val offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking = OffenderBooking.builder().offender(offender).build()
    val saveResponse = OffenderIdentifyingMark.builder()
      .bookingId(-1L)
      .offenderBooking(booking)
      .sequenceId(1)
      .markType("SCAR")
      .bodyPart("ARM")
      .side("L")
      .partOrientation("UPP")
      .commentText("Old wound")
      .images(listOf())
      .build()

    @BeforeEach
    fun setup() {
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("MARK_TYPE", "SCAR", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("BODY_PART", "ARM", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("SIDE", "L", false))
        .thenReturn(Optional.of(ReferenceCode()))
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("PART_ORIENT", "UPP", false))
        .thenReturn(Optional.of(ReferenceCode()))
    }

    @Test
    fun `Create new mark without image`() {
      whenever(bookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_ID)).thenReturn(Optional.of(booking))
      whenever(identifyingMarkRepository.save(any())).thenReturn(saveResponse)

      val createRequest = IdentifyingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = IdentifyingMark.builder()
        .bookingId(-1L)
        .id(1)
        .offenderNo(OFFENDER_ID)
        .markType("SCAR")
        .bodyPart("ARM")
        .side("L")
        .partOrientation("UPP")
        .comment("Old wound")
        .photographUuids(listOf())
        .build()
      val response = service.createIdentifyingMark(OFFENDER_ID, createRequest)

      verify(identifyingMarkRepository).save(any())
      verify(imageRepository, never()).save(any())
      assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `Create new mark with image`() {
      whenever(bookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_ID)).thenReturn(Optional.of(booking))
      whenever(identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(OFFENDER_ID, 1))
        .thenReturn(saveResponse)
      whenever(identifyingMarkRepository.save(any())).thenReturn(saveResponse)

      val createRequest = IdentifyingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = IdentifyingMark.builder()
        .bookingId(-1L)
        .id(1)
        .offenderNo(OFFENDER_ID)
        .markType("SCAR")
        .bodyPart("ARM")
        .side("L")
        .partOrientation("UPP")
        .comment("Old wound")
        .photographUuids(listOf())
        .build()
      val response = service.createIdentifyingMark(OFFENDER_ID, createRequest, ByteArrayInputStream(IMAGE_DATA))

      val imageCaptor = argumentCaptor<OffenderImage>()
      verify(identifyingMarkRepository).save(any())
      verify(imageRepository).save(imageCaptor.capture())
      assertThat(response).isEqualTo(expected)

      val savedImage = imageCaptor.firstValue
      assertThat(savedImage.orientationType).isEqualTo("ARM")
      assertThat(savedImage.imageType).isEqualTo("OFF_IDM")
      assertThat(savedImage.viewType).isEqualTo("SCAR")
      assertThat(savedImage.isActive).isTrue()
      assertThat(savedImage.imageObjectId).isEqualTo(1)
      assertThat(savedImage.fullSizeImage).isEqualTo(IMAGE_DATA)
      assertThat(savedImage.thumbnailImage).isNotEmpty()
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reference code not found")
    @ValueSource(strings = ["MARK_TYPE", "BODY_PART", "SIDE", "PART_ORIENT"])
    fun `Reference code not found`(domain: String) {
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode(eq(domain), anyString(), anyBoolean()))
        .thenReturn(Optional.empty())
      val updateRequest = IdentifyingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")

      val exception = assertThrows<BadRequestException> { service.updateIdentifyingMark(OFFENDER_ID, 1, updateRequest) }
      assertThat(exception.message).isEqualTo("Reference code not found: $domain")
    }
  }

  companion object {
    private const val OFFENDER_ID = "A1234AA"
    private val IMAGE_DATA = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==")
  }
}
