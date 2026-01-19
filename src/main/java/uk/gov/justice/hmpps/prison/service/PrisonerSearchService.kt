package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.OffenderLanguageDto
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier as OffenderIdentifierModel

@Component
class PrisonerSearchService(
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderRepository: OffenderRepository,
  private val offenderTransformer: OffenderTransformer,
  private val inmateService: InmateService,
  private val healthService: HealthService,
  private val offenderLanguageRepository: OffenderLanguageRepository,
) {
  @Transactional
  fun getPrisonerDetails(offenderNo: String): PrisonerSearchDetails {
    val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(offenderNo).getOrNull()
    val offender = booking?.offender ?: offenderRepository.findRootOffenderByNomsId(offenderNo).getOrNull()
    if (offender == null) throw EntityNotFoundException.withId(offenderNo)

    val (transferPrisonId, transferDate) = booking.getPreviousPrisonTransfer()

    return getInmateDetail(offender, booking)
      .let { detail ->
        PrisonerSearchDetails(
          offenderNo = detail.offenderNo,
          offenderId = detail.offenderId,
          bookingId = detail.bookingId,
          bookingNo = detail.bookingNo,
          title = offender.title?.description,
          firstName = detail.firstName,
          middleName = detail.middleName,
          lastName = detail.lastName,
          dateOfBirth = detail.dateOfBirth,
          agencyId = detail.agencyId,
          assignedLivingUnit = detail.assignedLivingUnit,
          religion = detail.religion,
          physicalAttributes = detail.physicalAttributes,
          physicalCharacteristics = detail.physicalCharacteristics,
          profileInformation = detail.profileInformation,
          physicalMarks = detail.physicalMarks,
          csra = detail.csra,
          categoryCode = detail.categoryCode,
          inOutStatus = detail.inOutStatus,
          identifiers = detail.identifiers?.sortedBy { it.whenCreated },
          allIdentifiers = offender.allIdentifiers?.map { oi -> oi.toModel(detail.offenderNo) }?.sortedBy { it.whenCreated },
          sentenceDetail = detail.sentenceDetail?.apply { additionalDaysAwarded = booking?.additionalDaysAwarded },
          mostSeriousOffence = detail.offenceHistory?.filter { off -> off.bookingId == detail.bookingId }?.filter { it.mostSerious }?.minByOrNull { it.offenceSeverityRanking }?.offenceDescription,
          indeterminateSentence = detail.sentenceTerms?.any { st -> st.lifeSentence && detail.bookingId == st.bookingId },
          aliases = detail.aliases,
          status = detail.status,
          lastMovementTypeCode = detail.lastMovementTypeCode,
          lastMovementReasonCode = detail.lastMovementReasonCode,
          lastMovementTime = findLastMovementTime(
            booking?.externalMovements,
            detail.lastMovementTypeCode,
            detail.lastMovementReasonCode,
          ),
          lastAdmissionTime = findLastAdmissionTime(booking?.externalMovements),
          previousPrisonId = transferPrisonId,
          previousPrisonLeavingDate = transferDate,
          legalStatus = detail.legalStatus,
          recall = detail.recall,
          imprisonmentStatus = detail.imprisonmentStatus,
          imprisonmentStatusDescription = detail.imprisonmentStatusDescription,
          convictedStatus = detail.convictedStatus,
          receptionDate = detail.receptionDate,
          locationDescription = detail.locationDescription,
          latestLocationId = detail.latestLocationId,
          latestPrisonLocationId = detail.latestPrisonLocationId,
          addresses = offender.rootOffender.addresses.filter { a ->
            // we only want a no fixed address if it is their primary address
            a.noFixedAddressFlag == "N" || (a.noFixedAddressFlag == "Y" && a.primaryFlag == "Y")
          }.map(AddressTransformer::translate),
          phones = offender.rootOffender.phones.map(AddressTransformer::translate),
          emailAddresses = offender.rootOffender.emailAddresses.map(AddressTransformer::translate),
          allConvictedOffences = detail.offenceHistory,
          personalCareNeeds = detail.personalCareNeeds,
          languages = findLanguages(detail.bookingId),
          imageId = detail.facialImageId,
          militaryRecord = true == booking?.militaryRecords?.isNotEmpty(),
        )
      }
  }

  private fun findLanguages(bookingId: Long?): List<OffenderLanguageDto>? = bookingId
    ?.let {
      offenderLanguageRepository.findByOffenderBookId(bookingId)
        .map {
          OffenderLanguageDto(
            type = it.type,
            code = it.code,
            readSkill = it.readSkill,
            writeSkill = it.writeSkill,
            speakSkill = it.speakSkill,
            interpreterRequested = it.interpreterRequestedFlag == "Y",
          )
        }
    }

  private fun findLastMovementTime(
    externalMovements: List<ExternalMovement>?,
    lastMovementTypeCode: String?,
    lastMovementReasonCode: String?,
  ) = externalMovements?.filter { em ->
    em.movementType?.code == lastMovementTypeCode &&
      em.movementReasonCode == lastMovementReasonCode
  }
    ?.maxByOrNull { em -> em.movementDateTime }
    ?.movementDateTime

  private fun findLastAdmissionTime(externalMovements: List<ExternalMovement>?): LocalDateTime? = externalMovements
    ?.filter { it.movementType?.code == "ADM" }
    ?.maxByOrNull { it.movementDateTime }
    ?.movementDateTime

  private fun getInmateDetail(offender: Offender, booking: OffenderBooking?): InmateDetail = booking
    ?.let { offenderTransformer.transform(it) }
    ?.apply {
      // TODO These need to be modelled in JPA and set by the OffenderTransformer
      physicalCharacteristics = inmateService.getPhysicalCharacteristics(bookingId)
      physicalMarks = inmateService.getPhysicalMarks(bookingId)
      aliases = inmateService.getAliases(bookingId)
      inmateService.getAllAssessmentsOrdered(bookingId).also {
        csra = it.firstOrNull { it.isCellSharingAlertFlag }?.classification
        categoryCode = it.firstOrNull { "CATEGORY" == it.assessmentCode }?.classificationCode
      }
      personalCareNeeds = healthService.getPersonalCareNeeds(
        bookingId,
        listOf("DISAB", "MATSTAT", "PHY", "PSYCH", "SC"),
      ).getPersonalCareNeeds()
      facialImageId = booking.latestFaceImage.getOrNull()?.id
    }
    ?: let { offenderTransformer.transformWithoutBooking(offender) }

  private fun OffenderIdentifier.toModel(offenderNo: String) = OffenderIdentifierModel(
    this.identifierType,
    this.identifier,
    offenderNo,
    null,
    this.issuedAuthorityText,
    this.issuedDate,
    this.caseloadType,
    this.createDateTime,
    this.offender.id,
    this.rootOffenderId,
    this.offenderIdentifierPK.offenderIdSeq,
  )
}

internal fun OffenderBooking?.getPreviousPrisonTransfer(): Pair<String?, LocalDateTime?> {
  var transferPrisonId: String? = null
  var transferDate: LocalDateTime? = null
  if (this != null && isActive) {
    val last = externalMovements
      ?.filter {
        it.movementDirection == MovementDirection.OUT
      }
      ?.sortedBy { it.movementDateTime }
      ?.lastOrNull { it.fromAgency?.id != location.id && it.fromAgency?.isPrison == true }

    transferPrisonId = last?.fromAgency?.id
    transferDate = last?.movementDateTime
  }
  return transferPrisonId to transferDate
}
