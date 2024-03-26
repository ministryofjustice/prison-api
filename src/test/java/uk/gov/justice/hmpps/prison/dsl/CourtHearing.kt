package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.service.CourtHearingsService
import java.time.LocalDateTime

@NomisDataDslMarker
interface CourtHearingDsl

@Component
class CourtHearingBuilderRepository(
  private val courtHearingsService: CourtHearingsService,
) {
  fun save(
    bookingId: Long,
    courtCaseId: Long,
    fromPrisonLocation: String,
    toCourtLocation: String,
    courtHearingDateTime: LocalDateTime,
    comments: String,
  ): CourtHearing =
    courtHearingsService.scheduleHearing(
      bookingId,
      courtCaseId,
      PrisonToCourtHearing
        .builder()
        .toCourtLocation(toCourtLocation)
        .fromPrisonLocation(fromPrisonLocation)
        .courtHearingDateTime(courtHearingDateTime)
        .comments(comments)
        .build(),
    )
}

@Component
class CourtHearingBuilderFactory(
  private val repository: CourtHearingBuilderRepository,
) {

  fun builder(): CourtHearingBuilder {
    return CourtHearingBuilder(repository)
  }
}

class CourtHearingBuilder(
  private val repository: CourtHearingBuilderRepository,
) : CourtHearingDsl {
  fun build(
    bookingId: Long,
    courtCase: OffenderCourtCase,
    fromPrisonLocation: String,
    toCourtLocation: String,
    courtHearingDateTime: LocalDateTime,
    comments: String,
  ) = repository.save(
    bookingId = bookingId,
    courtCaseId = courtCase.id,
    fromPrisonLocation = fromPrisonLocation,
    toCourtLocation = toCourtLocation,
    courtHearingDateTime = courtHearingDateTime,
    comments = comments,
  )
}
