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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMark
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkDetails
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkImageDetail
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
class DistinguishingMarkServiceImplTest {
  private val identifyingMarkRepository: OffenderIdentifyingMarkRepository = mock()
  private val imageRepository: OffenderImageRepository = mock()
  private val bookingRepository: OffenderBookingRepository = mock()
  private val referenceDataRepository: ReferenceDataRepository = mock()

  private var service = DistinguishingMarkService(
    identifyingMarkRepository,
    imageRepository,
    bookingRepository,
    referenceDataRepository,
  )

  @BeforeEach
  fun setup() {
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("MARK_TYPE", "SCAR", false))
      .thenReturn(Optional.of(MARK_TYPE_SCAR))
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("MARK_TYPE", "TAT", false))
      .thenReturn(Optional.of(MARK_TYPE_TATTOO))
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("BODY_PART", "ARM", false))
      .thenReturn(Optional.of(BODY_PART_ARM))
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("BODY_PART", "TORSO", false))
      .thenReturn(Optional.of(BODY_PART_TORSO))
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("SIDE", "L", false))
      .thenReturn(Optional.of(SIDE_LEFT))
    whenever(referenceDataRepository.getReferenceCodeByDomainAndCode("PART_ORIENT", "UPP", false))
      .thenReturn(Optional.of(ORIENTATION_UPPER))
  }

  @Test
  fun `get distinguishing marks for latest booking`() {
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
            OffenderImage.builder().id(1L).imageObjectId(1).active(true).build(),
            OffenderImage.builder().id(2L).imageObjectId(2).active(true).build(),
            OffenderImage.builder().id(3L).imageObjectId(2).active(false).build(),
          ),
        )
        .build(),
    )
    whenever(identifyingMarkRepository.findAllMarksForLatestBooking(anyString())).thenReturn(identifyingMarks)

    val expected = listOf(
      DistinguishingMark(
        id = 1,
        bookingId = -1L,
        offenderNo = OFFENDER_ID,
        markType = MARK_TYPE_TATTOO,
        bodyPart = BODY_PART_TORSO,
        comment = "Some comment",
        photographUuids = listOf(),
      ),
      DistinguishingMark(
        id = 2,
        bookingId = -1L,
        offenderNo = OFFENDER_ID,
        markType = MARK_TYPE_TATTOO,
        bodyPart = BODY_PART_ARM,
        side = SIDE_LEFT,
        partOrientation = ORIENTATION_UPPER,
        photographUuids = listOf(
          DistinguishingMarkImageDetail(1L, false),
          DistinguishingMarkImageDetail(2L, true),
        ),
      ),
    )

    val response = service.findMarksForLatestBooking(OFFENDER_ID)

    verify(identifyingMarkRepository).findAllMarksForLatestBooking(OFFENDER_ID)
    assertThat(response).isEqualTo(expected)
  }

  @Test
  fun `get specific distinguishing mark for latest booking`() {
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
          OffenderImage.builder().id(1L).imageObjectId(1).active(true).build(),
          OffenderImage.builder().id(2L).imageObjectId(2).active(true).build(),
          OffenderImage.builder().id(3L).imageObjectId(2).active(false).build(),
        ),
      )
      .build()

    whenever(
      identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(
        anyString(),
        anyInt(),
      ),
    ).thenReturn(identifyingMark)

    val expected = DistinguishingMark(
      id = 2,
      bookingId = -1L,
      offenderNo = OFFENDER_ID,
      markType = MARK_TYPE_TATTOO,
      bodyPart = BODY_PART_ARM,
      side = SIDE_LEFT,
      partOrientation = ORIENTATION_UPPER,
      photographUuids = listOf(
        DistinguishingMarkImageDetail(1L, false),
        DistinguishingMarkImageDetail(2L, true),
      ),
    )

    val response = service.getMarkForLatestBooking(OFFENDER_ID, 2)

    verify(identifyingMarkRepository).getMarkForLatestBookingByOffenderNumberAndSequenceId(OFFENDER_ID, 2)
    assertThat(response).isEqualTo(expected)
  }

  @Test
  fun `throws exception if a reference code cannot be found`() {
    val offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking = OffenderBooking.builder().offender(offender).build()
    val identifyingMark = OffenderIdentifyingMark.builder()
      .bookingId(-1L)
      .offenderBooking(booking)
      .sequenceId(2)
      .markType("NOTFOUND")
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

    val ex = assertThrows<RuntimeException> { service.getMarkForLatestBooking(OFFENDER_ID, 2) }
    assertThat(ex.message).isEqualTo("Reference code NOTFOUND with domain MARK_TYPE not found")
  }

  @Test
  fun `Add photo to existing distinguishing mark`() {
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
  @DisplayName("Update existing image")
  inner class UpdateExistingImage {
    @Test
    fun `Throws exception when image is not found`() {
      whenever(imageRepository.findById(any())).thenReturn(Optional.empty())

      val ex = assertThrows<RuntimeException> { service.updatePhotoImage(123L, ByteArrayInputStream(IMAGE_DATA)) }
      assertThat(ex.message).isEqualTo("Image with id 123 not found")
    }

    @Test
    fun `Updates image and thumbnail successfully`() {
      val imageId = 123L
      val image: OffenderImage = mock()
      whenever(imageRepository.findById(imageId)).thenReturn(Optional.of(image))

      service.updatePhotoImage(imageId, ByteArrayInputStream(IMAGE_DATA))

      verify(image).fullSizeImage = IMAGE_DATA
      verify(image).thumbnailImage = any()
      verifyNoMoreInteractions(image)
    }
  }

  @Nested
  @DisplayName("Update existing distinguishing mark")
  inner class UpdateExistingMark {

    @Test
    fun `Updates successfully`() {
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

      val updateRequest = DistinguishingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = DistinguishingMark(
        id = 1,
        bookingId = -1L,
        offenderNo = OFFENDER_ID,
        markType = MARK_TYPE_SCAR,
        bodyPart = BODY_PART_ARM,
        side = SIDE_LEFT,
        partOrientation = ORIENTATION_UPPER,
        comment = "Old wound",
        photographUuids = listOf(),
      )
      val response = service.updateMark(OFFENDER_ID, 1, updateRequest)

      assertThat(response).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reference code not found")
    @ValueSource(strings = ["MARK_TYPE", "BODY_PART", "SIDE", "PART_ORIENT"])
    fun `Reference code not found`(domain: String) {
      whenever(referenceDataRepository.getReferenceCodeByDomainAndCode(eq(domain), anyString(), anyBoolean()))
        .thenReturn(Optional.empty())
      val updateRequest = DistinguishingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")

      val exception = assertThrows<BadRequestException> { service.updateMark(OFFENDER_ID, 1, updateRequest) }
      assertThat(exception.message).isEqualTo("Reference code not found: $domain")
    }
  }

  @Nested
  @DisplayName("Create new distinguishing mark")
  inner class CreateMark {
    val offender: Offender = Offender.builder().nomsId(OFFENDER_ID).build()
    val booking: OffenderBooking = OffenderBooking.builder().offender(offender).build()
    private val saveResponse: OffenderIdentifyingMark = OffenderIdentifyingMark.builder()
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

    @Test
    fun `Create new distinguishing mark without image`() {
      whenever(bookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_ID)).thenReturn(Optional.of(booking))
      whenever(identifyingMarkRepository.save(any())).thenReturn(saveResponse)

      val createRequest = DistinguishingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = DistinguishingMark(
        id = 1,
        bookingId = -1L,
        offenderNo = OFFENDER_ID,
        markType = MARK_TYPE_SCAR,
        bodyPart = BODY_PART_ARM,
        side = SIDE_LEFT,
        partOrientation = ORIENTATION_UPPER,
        comment = "Old wound",
        photographUuids = listOf(),
      )
      val response = service.createMark(OFFENDER_ID, createRequest)

      verify(identifyingMarkRepository).save(any())
      verify(imageRepository, never()).save(any())
      assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `Create new distinguishing mark with image`() {
      whenever(bookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_ID)).thenReturn(Optional.of(booking))
      whenever(identifyingMarkRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(OFFENDER_ID, 1))
        .thenReturn(saveResponse)
      whenever(identifyingMarkRepository.save(any())).thenReturn(saveResponse)

      val createRequest = DistinguishingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")
      val expected = DistinguishingMark(
        id = 1,
        bookingId = -1L,
        offenderNo = OFFENDER_ID,
        markType = MARK_TYPE_SCAR,
        bodyPart = BODY_PART_ARM,
        side = SIDE_LEFT,
        partOrientation = ORIENTATION_UPPER,
        comment = "Old wound",
        photographUuids = listOf(),
      )
      val response = service.createMark(OFFENDER_ID, createRequest, ByteArrayInputStream(IMAGE_DATA))

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
      val updateRequest = DistinguishingMarkDetails("SCAR", "ARM", "L", "UPP", "Old wound")

      val exception = assertThrows<BadRequestException> { service.updateMark(OFFENDER_ID, 1, updateRequest) }
      assertThat(exception.message).isEqualTo("Reference code not found: $domain")
    }
  }

  companion object {
    private const val OFFENDER_ID = "A1234AA"
    private val IMAGE_DATA = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==")

    private val MARK_TYPE_SCAR = ReferenceCode("MARK_TYPE", "SCAR", listOf())
    private val MARK_TYPE_TATTOO = ReferenceCode("MARK_TYPE", "TAT", listOf())

    private val BODY_PART_ARM = ReferenceCode("BODY_PART", "ARM", listOf())
    private val BODY_PART_TORSO = ReferenceCode("BODY_PART", "TORSO", listOf())

    private val SIDE_LEFT = ReferenceCode("SIDE", "L", listOf())

    private val ORIENTATION_UPPER = ReferenceCode("PART_ORIENT", "UPP", listOf())
  }
}
