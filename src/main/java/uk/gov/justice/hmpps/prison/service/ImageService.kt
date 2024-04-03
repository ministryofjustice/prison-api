package uk.gov.justice.hmpps.prison.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.ImageDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findLatestOffenderBookingByNomsIdOrNull
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.util.Optional
import javax.imageio.ImageIO

@Service
@Transactional(readOnly = true)
class ImageService(
  private val offenderImageRepository: OffenderImageRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
) {

  fun findOffenderImagesFor(offenderNumber: String): List<ImageDetail> =
    offenderImageRepository.getImagesByOffenderNumber(offenderNumber)
      .map(OffenderImage::transform)

  fun findImageDetail(imageId: Long): ImageDetail =
    offenderImageRepository.findById(imageId)
      .map(OffenderImage::transform)
      .orElseThrow(EntityNotFoundException.withId(imageId))

  fun getImageContent(imageId: Long, fullSizeImage: Boolean): Optional<ByteArray> =
    offenderImageRepository.findById(imageId)
      .map { if (fullSizeImage) it.fullSizeImage else it.thumbnailImage }

  fun getImageContent(offenderNo: String, fullSizeImage: Boolean): Optional<ByteArray> =
    offenderImageRepository.findLatestByOffenderNumber(offenderNo)
      .map { if (fullSizeImage) it.fullSizeImage else it.thumbnailImage }

  @Transactional
  fun putImageForOffender(offenderNumber: String, receivedImage: InputStream): ImageDetail {
    // Uses a 4:3 aspect ratio - will distort square photos! Compact cameras and phones use 4:3 for portrait.
    val fullWidth = 427
    val fullHeight = 570
    val thumbWidth = 150
    val thumbHeight = 200
    val booking = offenderBookingRepository.findLatestOffenderBookingByNomsIdOrNull(offenderNumber)
      ?: throw EntityNotFoundException.withMessage("There are no bookings for $offenderNumber")

    // Set the previously active facial image for this bookingId to inactive
    val previousImage = offenderImageRepository.findLatestByBookingId(booking.bookingId)
    if (previousImage.isPresent) {
      val prev = previousImage.get()
      log.info(
        "Setting previous facial image to active=false - Id {}, bookingId {}, bookingSeq {}, offenderNo {}",
        prev.id,
        booking.bookingId,
        booking.bookingSequence,
        offenderNumber,
      )
      prev.isActive = false
      offenderImageRepository.save(prev)
    }
    return try {
      val imageToScale = receivedImage.readAllBytes()
      val fullImage = scaleImage(fullWidth, fullHeight, imageToScale)
      val thumbImage = scaleImage(thumbWidth, thumbHeight, imageToScale)
      val newImage = OffenderImage
        .builder()
        .captureDateTime(LocalDateTime.now())
        .orientationType("FRONT")
        .viewType("FACE")
        .imageType("OFF_BKG")
        .active(true)
        .sourceCode("GEN")
        .offenderBooking(booking)
        .thumbnailImage(thumbImage)
        .fullSizeImage(fullImage)
        .build()

      // Suggested method - does add the image but does not flush or return the imageId for the response
      // var savedImage = booking.addImage(newImage);
      val savedImage = offenderImageRepository.save(newImage)
      log.info(
        "Saved image - Id {}, bookingId {}, bookingSeq {}, offenderNo {}",
        savedImage.id,
        booking.bookingId,
        booking.bookingSequence,
        offenderNumber,
      )
      savedImage.transform()
    } catch (e: Exception) {
      throw BadRequestException.withMessage("Error scaling the image. Must be in JPEG format.")
    }
  }

  @Throws(IOException::class, IllegalArgumentException::class, InterruptedException::class)
  private fun scaleImage(width: Int, height: Int, source: ByteArray): ByteArray {
    val inputStream = ByteArrayInputStream(source)
    val original = ImageIO.read(inputStream)
    val scaled = original.getScaledInstance(width, height, Image.SCALE_DEFAULT)
    val outputImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val ready = outputImage.graphics.drawImage(scaled, 0, 0, ImageWait())
    if (!ready) {
      // Large images may take slightly longer to scale - not seen any (so far) though
      log.info("Initial image response not ready - waiting 500 ms")
      Thread.sleep(500)
    }
    val baos = ByteArrayOutputStream()
    ImageIO.write(outputImage, "jpg", baos)
    return baos.toByteArray()
  }

  private class ImageWait : ImageObserver {
    override fun imageUpdate(img: Image, infoFlags: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
      log.info("Image update received a response for image {} x {}", width, height)
      return true
    }
  }

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
