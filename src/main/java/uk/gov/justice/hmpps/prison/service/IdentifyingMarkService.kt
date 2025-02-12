package uk.gov.justice.hmpps.prison.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifyingMark
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifyingMarkRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.util.Base64
import javax.imageio.ImageIO

@Service
class IdentifyingMarkService(
  private val identifyingMarksRepository: OffenderIdentifyingMarkRepository,
  private val imageRepository: OffenderImageRepository,
) {

  @Transactional(readOnly = true)
  fun findIdentifyingMarksForLatestBooking(offenderNumber: String): List<IdentifyingMark> =
    identifyingMarksRepository.findAllMarksForLatestBooking(offenderNumber).map(OffenderIdentifyingMark::transform)

  @Transactional(readOnly = true)
  fun getIdentifyingMarkForLatestBooking(offenderNumber: String, markId: Int): IdentifyingMark =
    identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(offenderNumber, markId).transform()

  @Transactional
  fun addPhotoToMark(offenderNumber: String, markId: Int, image: InputStream) {
    val mark = identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(offenderNumber, markId)
    val imageContent = image.readAllBytes()
    val newImage = OffenderImage
      .builder()
      .captureDateTime(LocalDateTime.now())
      .orientationType(mark.bodyPart)
      .viewType(mark.markType)
      .imageType("OFF_IDM")
      .imageObjectId(mark.sequenceId.toLong())
      .active(true)
      .sourceCode("FILE")
      .offenderBooking(mark.offenderBooking)
      .thumbnailImage(scaleImage(imageContent))
      .fullSizeImage(imageContent)
      .build()

    imageRepository.save(newImage)
  }

  @Throws(IOException::class, IllegalArgumentException::class, InterruptedException::class)
  private fun scaleImage(source: ByteArray, width: Int = 150, height: Int = 200): ByteArray {
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

  // Needed?
  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String, table: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get $table lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
