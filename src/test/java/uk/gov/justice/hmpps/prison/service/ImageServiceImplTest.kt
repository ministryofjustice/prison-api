package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.ImageDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.Base64
import java.util.Optional

class ImageServiceImplTest {
  private val imageData = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==")

  private val offenderImageRepository: OffenderImageRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val service: ImageService = ImageService(offenderImageRepository, offenderBookingRepository)

  @Test
  fun findOffenderImages() {
    whenever(offenderImageRepository.getImagesByOffenderNumber(OFFENDER_NUMBER)).thenReturn(
      listOf(
        OffenderImage.builder()
          .id(123L)
          .active(false)
          .captureDateTime(DATETIME)
          .viewType("FACE")
          .orientationType("FRONT")
          .imageType("OFF_BKG")
          .imageObjectId(1L)
          .build(),
      ),
    )
    assertThat(service.findOffenderImagesFor(OFFENDER_NUMBER)).containsOnly(
      ImageDetail.builder()
        .imageId(123L)
        .active(false)
        .captureDate(DATETIME.toLocalDate())
        .captureDateTime(DATETIME)
        .imageView("FACE")
        .imageOrientation("FRONT")
        .imageType("OFF_BKG")
        .objectId(1L)
        .build(),
    )
  }

  @Test
  fun imageContent() {
    val data = byteArrayOf(0x12)
    whenever(offenderImageRepository.findById(-1L))
      .thenReturn(Optional.of(OffenderImage.builder().id(-1L).fullSizeImage(data).build()))
    assertThat(service.getImageContent(-1L, true)).isNotEmpty()
    assertThat(service.getImageContent(-1L, true)).get().isEqualTo(data)
  }

  @Test
  fun imageContentForOffender() {
    val data = byteArrayOf(0x12)
    whenever(offenderImageRepository.findLatestByOffenderNumber("A1234AA"))
      .thenReturn(Optional.of(OffenderImage.builder().id(-1L).fullSizeImage(data).build()))
    assertThat(service.getImageContent("A1234AA", true)).get().isEqualTo(data)
  }

  @Test
  fun putImageForOffenderWithNoBooking() {
    whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_NUMBER))
      .thenReturn(Optional.empty())
    assertThatThrownBy { service.putImageForOffender(OFFENDER_NUMBER, ByteArrayInputStream(imageData)) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("There are no bookings for %s", OFFENDER_NUMBER)
  }

  @Test
  fun putImageForOffenderOk() {
    val booking = OffenderBooking.builder().bookingId(1L).bookingSequence(1).build()
    val newImage = OffenderImage
      .builder()
      .captureDateTime(LocalDateTime.now())
      .orientationType("FRONT")
      .viewType("FACE")
      .imageType("OFF_BKG")
      .active(true)
      .sourceCode("GEN")
      .offenderBooking(booking)
      .thumbnailImage(imageData)
      .fullSizeImage(imageData)
      .build()
    whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_NUMBER))
      .thenReturn(Optional.of(booking))
    whenever(offenderImageRepository.findLatestByBookingId(1L)).thenReturn(Optional.empty())
    whenever(offenderImageRepository.save(newImage)).thenReturn(newImage)
    val savedImage = service.putImageForOffender(OFFENDER_NUMBER, ByteArrayInputStream(imageData))
    assertThat(savedImage).isEqualTo(newImage.transform())
  }

  @Test
  fun putImageUpdatesPreviousToInactive() {
    val booking = OffenderBooking.builder().bookingId(1L).bookingSequence(1).build()
    val prevImage = OffenderImage
      .builder()
      .captureDateTime(LocalDateTime.now())
      .orientationType("FRONT")
      .viewType("FACE")
      .imageType("OFF_BKG")
      .active(false)
      .sourceCode("GEN")
      .offenderBooking(booking)
      .thumbnailImage(imageData)
      .fullSizeImage(imageData)
      .build()
    val newImage = OffenderImage
      .builder()
      .captureDateTime(LocalDateTime.now())
      .orientationType("FRONT")
      .viewType("FACE")
      .imageType("OFF_BKG")
      .active(true)
      .sourceCode("GEN")
      .offenderBooking(booking)
      .thumbnailImage(imageData)
      .fullSizeImage(imageData)
      .build()
    whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(OFFENDER_NUMBER))
      .thenReturn(Optional.of(booking))
    whenever(offenderImageRepository.findLatestByBookingId(1L)).thenReturn(Optional.of(prevImage))
    whenever(offenderImageRepository.save(prevImage)).thenReturn(prevImage)
    whenever(offenderImageRepository.save(newImage)).thenReturn(newImage)
    val savedImage = service.putImageForOffender(OFFENDER_NUMBER, ByteArrayInputStream(imageData))
    assertThat(savedImage).isEqualTo(newImage.transform())
  }

  companion object {
    private val DATETIME = LocalDateTime.now()
    private const val OFFENDER_NUMBER = "A1234AA"
  }
}
