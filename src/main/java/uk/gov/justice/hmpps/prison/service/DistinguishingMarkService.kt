package uk.gov.justice.hmpps.prison.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMark
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkDetails
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkImageDetail
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.ReferenceDataRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifyingMark
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
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
import javax.imageio.ImageIO

@Service
class DistinguishingMarkService(
  private val identifyingMarksRepository: OffenderIdentifyingMarkRepository,
  private val imageRepository: OffenderImageRepository,
  private val bookingRepository: OffenderBookingRepository,
  private val referenceDataRepository: ReferenceDataRepository,
) {
  @Transactional(readOnly = true)
  fun findMarksForLatestBooking(offenderNumber: String): List<DistinguishingMark> = identifyingMarksRepository.findAllMarksForLatestBooking(offenderNumber).map { it.toDistinguishingMark() }

  @Transactional(readOnly = true)
  fun getMarkForLatestBooking(offenderNumber: String, markId: Int): DistinguishingMark = identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(offenderNumber, markId)
    ?.toDistinguishingMark()
    ?: notFound(offenderNumber, markId)

  @Transactional
  fun updateMark(
    offenderNumber: String,
    markId: Int,
    updateRequest: DistinguishingMarkDetails,
  ): DistinguishingMark {
    validateRequest(updateRequest)
    return identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(offenderNumber, markId)
      ?.apply {
        markType = updateRequest.markType
        bodyPart = updateRequest.bodyPart
        side = updateRequest.side
        partOrientation = updateRequest.partOrientation
        commentText = updateRequest.comment
      }?.toDistinguishingMark()
      ?: notFound(offenderNumber, markId)
  }

  @Transactional
  fun createMark(
    offenderNumber: String,
    createRequest: DistinguishingMarkDetails,
    image: InputStream? = null,
  ): DistinguishingMark {
    validateRequest(createRequest)
    val booking = bookingRepository.findLatestOffenderBookingByNomsId(offenderNumber)
      .orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNumber))

    val maxSeqId: Int = identifyingMarksRepository.findAllMarksForLatestBooking(offenderNumber)
      .maxOfOrNull { it.sequenceId } ?: 0

    val mark = OffenderIdentifyingMark.builder()
      .bookingId(booking.bookingId)
      .offenderBooking(booking)
      .sequenceId(maxSeqId + 1)
      .markType(createRequest.markType)
      .bodyPart(createRequest.bodyPart)
      .side(createRequest.side)
      .partOrientation(createRequest.partOrientation)
      .commentText(createRequest.comment)
      .build()

    val savedMark = identifyingMarksRepository.save(mark)
    val savedMarkId = savedMark.sequenceId

    if (image != null) {
      saveImage(savedMark, image)
      return identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(
        offenderNumber,
        savedMarkId,
      )
        ?.toDistinguishingMark()
        ?: notFound(offenderNumber, savedMarkId)
    }

    return savedMark.toDistinguishingMark()
  }

  @Transactional
  fun addPhotoToMark(offenderNumber: String, markId: Int, image: InputStream) {
    val mark = identifyingMarksRepository.getMarkForLatestBookingByOffenderNumberAndSequenceId(offenderNumber, markId)
      ?: notFound(offenderNumber, markId)
    saveImage(mark, image)
  }

  private fun saveImage(mark: OffenderIdentifyingMark, image: InputStream) {
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

  private fun OffenderIdentifyingMark.toDistinguishingMark(): DistinguishingMark {
    val latestImageId = images
      .filter { it.isActive }
      .maxByOrNull { it.id }?.id ?: 1L
    val imageInfo = images
      .filter { it.isActive }
      .map { DistinguishingMarkImageDetail(it.id, latestImageId == it.id) }
      .sortedBy { it.id }

    return DistinguishingMark(
      this.sequenceId,
      this.bookingId,
      this.offenderBooking.offender.nomsId,
      getReferenceCode("BODY_PART", this.bodyPart),
      getReferenceCode("MARK_TYPE", this.markType),
      getReferenceCode("SIDE", this.side),
      getReferenceCode("PART_ORIENT", this.partOrientation),
      this.commentText,
      this.createDatetime,
      this.createUserId,
      imageInfo,
    )
  }

  private fun validateRequest(request: DistinguishingMarkDetails) {
    verifyReferenceCodeExists("MARK_TYPE", request.markType)
    verifyReferenceCodeExists("BODY_PART", request.bodyPart)
    verifyReferenceCodeExists("SIDE", request.side)
    verifyReferenceCodeExists("PART_ORIENT", request.partOrientation)
  }

  private fun getReferenceCode(domain: String, code: String?): ReferenceCode? {
    if (code == null) {
      return null
    }
    return referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false)
      .orElseThrow { RuntimeException("Reference code $code with domain $domain not found") }
  }

  private fun verifyReferenceCodeExists(domain: String, code: String?) {
    if (code != null) {
      referenceDataRepository.getReferenceCodeByDomainAndCode(domain, code, false)
        .orElseThrow { BadRequestException("Reference code not found: $domain") }
    }
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

  private fun notFound(offenderNumber: String, markId: Int): Nothing = throw EntityNotFoundException.withMessage("Mark for $offenderNumber with seq_id $markId not found")

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
