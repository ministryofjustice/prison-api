package uk.gov.justice.hmpps.prison.dsl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.time.LocalDateTime

@NomisDataDslMarker
interface TemporaryAbsenceScheduleDsl

@Component
class TemporaryAbsenceScheduleBuilderRepository(
  private val scheduleRepository: OffenderIndividualScheduleRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val eventStatusRepository: ReferenceCodeRepository<EventStatus>,
  private val escortAgencyTypeRepository: ReferenceCodeRepository<EscortAgencyType>,
) {
  fun save(
    bookingId: Long,
    startTime: LocalDateTime,
    toAddressId: Long,
  ): OffenderIndividualSchedule = offenderBookingRepository.findByIdOrNull(bookingId)!!.let {
    scheduleRepository.save(
      OffenderIndividualSchedule.builder()
        .eventDate(startTime.toLocalDate())
        .startTime(startTime)
        .eventClass(OffenderIndividualSchedule.EventClass.EXT_MOV)
        .eventType("TAP")
        .eventSubType("ET")
        .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
        .escortAgencyType(
          escortAgencyTypeRepository.findByIdOrNull(
            EscortAgencyType.pk("L"),
          ),
        )
        .fromLocation(it.location)
        .toAddressOwnerClass("CORP")
        .toAddressId(toAddressId)
        .movementDirection(MovementDirection.OUT)
        .offenderBooking(it)
        .build(),
    )
  }
}

@Component
class TemporaryAbsenceScheduleBuilderFactory(
  private val repository: TemporaryAbsenceScheduleBuilderRepository,
) {

  fun builder(): TemporaryAbsenceScheduleBuilder {
    return TemporaryAbsenceScheduleBuilder(repository)
  }
}

class TemporaryAbsenceScheduleBuilder(
  private val repository: TemporaryAbsenceScheduleBuilderRepository,
) : TemporaryAbsenceScheduleDsl {
  fun build(
    bookingId: Long,
    startTime: LocalDateTime,
    toAddressId: Long,
  ) = repository.save(
    bookingId = bookingId,
    startTime = startTime,
    toAddressId = toAddressId,
  )
}
