package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifyingMark
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifyingMarkRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import java.io.ByteArrayInputStream
import java.util.Base64

@ExtendWith(MockitoExtension::class)
class IdentifyingMarkServiceImplTest {
  private val identifyingMarkRepository: OffenderIdentifyingMarkRepository = mock()
  private val imageRepository: OffenderImageRepository = mock()

  private var service = IdentifyingMarkService(
    identifyingMarkRepository,
    imageRepository,
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
        .photographUuids(listOf(1L, 2L))
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
      .photographUuids(listOf(1L, 2L))
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

  companion object {
    private const val OFFENDER_ID = "A1234AA"
    private val IMAGE_DATA = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==")
  }
}
