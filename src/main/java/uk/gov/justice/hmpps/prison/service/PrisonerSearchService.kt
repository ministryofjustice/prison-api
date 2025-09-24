package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.OffenderLanguageDto
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
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
  private val telemetryClient: TelemetryClient,
) {

  @Transactional
  fun getPrisonerDetails(offenderNo: String): PrisonerSearchDetails {
    val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(offenderNo).getOrNull()
    val offender = booking?.offender ?: offenderRepository.findRootOffenderByNomsId(offenderNo).getOrNull()
    if (offender == null) throw EntityNotFoundException.withId(offenderNo)

    val lastTransfer = if (booking?.isActive == true) {
      offender.getPrisonerInPrisonSummary()
        .prisonPeriod
        .find { it.bookingId == booking.bookingId }
        ?.transfers
        ?.lastOrNull()
    } else {
      null
    }

    val (transferPrisonId, transferDate) = booking.getTransfer()

    telemetryClient.trackEvent(
      "getPrisonerDetails-previous-prison",
      mapOf(
        "offenderNo" to offenderNo,
        "booking" to booking?.bookingId.toString(),
        "recall" to booking.includesRecall().toString(),
        "active" to booking?.isActive.toString(),
        "lastTransferPrisonId" to lastTransfer?.fromPrisonId.toString(),
        "lastTransferDate" to lastTransfer?.dateOutOfPrison.toString(),
        "previousReleasePrisonId" to transferPrisonId.toString(),
        "previousReleaseDate" to transferDate.toString(),
      ),
      null,
    )
    // log.info("lastTransferPrisonId=${lastTransfer?.fromPrisonId} previousReleasePrisonId=${transferPrisonId} lastTransferDate=${lastTransfer?.dateOutOfPrison} previousReleaseDate=${transferDate}")
    return getInmateDetail(offender, booking)
      .let {
        PrisonerSearchDetails(
          offenderNo = it.offenderNo,
          offenderId = it.offenderId,
          bookingId = it.bookingId,
          bookingNo = it.bookingNo,
          title = offender.title?.description,
          firstName = it.firstName,
          middleName = it.middleName,
          lastName = it.lastName,
          dateOfBirth = it.dateOfBirth,
          agencyId = it.agencyId,
          assignedLivingUnit = it.assignedLivingUnit,
          religion = it.religion,
          physicalAttributes = it.physicalAttributes,
          physicalCharacteristics = it.physicalCharacteristics,
          profileInformation = it.profileInformation,
          physicalMarks = it.physicalMarks,
          csra = it.csra,
          categoryCode = it.categoryCode,
          inOutStatus = it.inOutStatus,
          identifiers = it.identifiers?.sortedBy { it.whenCreated },
          allIdentifiers = offender.allIdentifiers?.map { oi -> oi.toModel(it.offenderNo) }?.sortedBy { it.whenCreated },
          sentenceDetail = it.sentenceDetail?.apply { additionalDaysAwarded = booking?.additionalDaysAwarded },
          mostSeriousOffence = it.offenceHistory?.filter { off -> off.bookingId == it.bookingId }?.filter { it.mostSerious }?.minByOrNull { it.offenceSeverityRanking }?.offenceDescription,
          indeterminateSentence = it.sentenceTerms?.any { st -> st.lifeSentence && it.bookingId == st.bookingId },
          aliases = it.aliases,
          status = it.status,
          lastMovementTypeCode = it.lastMovementTypeCode,
          lastMovementReasonCode = it.lastMovementReasonCode,
          lastMovementTime = findLastMovementTime(
            booking?.externalMovements,
            it.lastMovementTypeCode,
            it.lastMovementReasonCode,
          ),
          lastAdmissionTime = findLastAdmissionTime(booking?.externalMovements),
          previousPrisonId = transferPrisonId, // lastTransfer?.fromPrisonId,
          previousPrisonLeavingDate = transferDate, // lastTransfer?.dateOutOfPrison,
          legalStatus = it.legalStatus,
          recall = it.recall,
          imprisonmentStatus = it.imprisonmentStatus,
          imprisonmentStatusDescription = it.imprisonmentStatusDescription,
          convictedStatus = it.convictedStatus,
          receptionDate = it.receptionDate,
          locationDescription = it.locationDescription,
          latestLocationId = it.latestLocationId,
          addresses = offender.rootOffender.addresses.filter { a ->
            // we only want a no fixed address if it is their primary address
            a.noFixedAddressFlag == "N" || (a.noFixedAddressFlag == "Y" && a.primaryFlag == "Y")
          }.map(AddressTransformer::translate),
          phones = offender.rootOffender.phones.map(AddressTransformer::translate),
          emailAddresses = offender.rootOffender.emailAddresses.map(AddressTransformer::translate),
          allConvictedOffences = it.offenceHistory,
          personalCareNeeds = it.personalCareNeeds,
          languages = findLanguages(it.bookingId),
          imageId = it.facialImageId,
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

//  private fun findPreviousPrisonId2(externalMovements: List<ExternalMovement>?): String? =
//    externalMovements
//      ?.last { em -> em.movementType == MovementType.REL && em.toAgency.id == "OUT" }
//      ?.fromAgency?.id

//  private fun findPreviousPrisonId3(prisonerInPrisonSummary: PrisonerInPrisonSummary, lastPrisonId: String): String? =
//    prisonerInPrisonSummary
//      .prisonPeriod
//      .find { it.bookingId == 1L }
//        ?.transfers?.lastOrNull()?.fromPrisonId

//  private fun findPreviousPrisonId(prisonerInPrisonSummary: PrisonerInPrisonSummary): String? {
//    // If OUT and no transfers: get most recent prisonPeriod ("bookingSequence": 1) and movementDates  and use releaseFromPrisonId and dateOutOfPrison
//    // If IN and transfers: get most recent prisonPeriod ("bookingSequence": 1), use last transfer fromPrisonId & dateOutOfPrison
//    // If IN and no transfers: get most recent prisonPeriod ("bookingSequence": 1), and movementDates: no releaseFromPrisonId and dateOutOfPrison, so set null
//    // If out and transfers(as no trn) :get most recent prisonPeriod ("bookingSequence": 1) and movementDates  and use releaseFromPrisonId and dateOutOfPrison
//    // TAPs ??
  // Recalls: last transfer could be within previous term ???
//    return prisonerInPrisonSummary
//      .prisonPeriod
//      .find { it.bookingId == 1L }
//      ?.movementDates
//      ?.filterNot { it.inwardType == "TAP" || it.outwardType == "TAP" }
//      ?.maxByOrNull { it.dateOutOfPrison }
//      ?.releaseFromPrisonId
//  }

  private val transferCodes = arrayOf("REL", "TRN")

  private fun OffenderBooking?.getTransfer(): Pair<String?, LocalDateTime?> {
    val transfers = this?.externalMovements
      ?.filter { transferCodes.contains(it.movementType?.code) }
      ?.sortedBy { it.movementDateTime }

    var transferPrisonId: String? = null
    var transferDate: LocalDateTime? = null
    if (this != null) {
      val last = if (isActive) {
        transfers?.lastOrNull { it.fromAgency.id != location.id }
      } else {
        null
      }
      transferPrisonId = last?.fromAgency?.id
      transferDate = last?.movementDateTime
    }
    return transferPrisonId to transferDate
  }

  private fun OffenderBooking?.includesRecall(): Boolean = this
    ?.externalMovements
    ?.firstOrNull { em -> em.movementType?.code == "ADM" && em.movementReasonCode == "L" } != null

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
