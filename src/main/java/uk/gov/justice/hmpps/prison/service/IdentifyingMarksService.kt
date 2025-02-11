package uk.gov.justice.hmpps.prison.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMarkDto
import uk.gov.justice.hmpps.prison.api.model.ImageDetail
import uk.gov.justice.hmpps.prison.repository.IdentifyingMarksRepository
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
class IdentifyingMarksService(
  private val identifyingMarksRepository: IdentifyingMarksRepository,
) {

  fun findIdentifyingMarksForLatestBooking(offenderNumber: String): List<IdentifyingMark> {
    val identifyingMarks = identifyingMarksRepository.findIdentifyingMarksForLatestBooking(offenderNumber)
    return listOf()
  }

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
