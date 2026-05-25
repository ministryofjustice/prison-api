package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
@WithMockAuthUser
@AutoConfigureTestEntityManager
class OffenderIndividualScheduleRepositoryTest(
  @Autowired private val offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository,
  @Autowired private val offenderBookingRepository: OffenderBookingRepository,
  @Autowired private val eventStatusRepository: ReferenceCodeRepository<EventStatus?>,
  @Autowired private val escortAgencyTypeRepository: ReferenceCodeRepository<EscortAgencyType?>,
  @Autowired private val transferCancellationReasonRepository: ReferenceCodeRepository<TransferCancellationReason?>,
  @Autowired private val agencyRepository: AgencyLocationRepository,
  @Autowired private val entityManager: TestEntityManager,
) {
  @Test
  fun persistence_and_retrieval_of_schedule() {
    val persistedEntity = offenderIndividualScheduleRepository.save(
      OffenderIndividualSchedule.builder()
        .eventDate(EVENT_DATE)
        .startTime(START_TIME)
        .eventClass(EventClass.EXT_MOV)
        .eventType("TRN")
        .eventSubType("NOTR")
        .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
        .escortAgencyType(escortAgencyTypeRepository.findById(EscortAgencyType.pk("PECS")).orElseThrow())
        .fromLocation(agencyRepository.findById("BXI").orElseThrow())
        .toLocation(agencyRepository.findById("LEI").orElseThrow())
        .movementDirection(MovementDirection.OUT)
        .offenderBooking(offenderBookingRepository.findById(-1L).orElseThrow())
        .cancellationReason(
          transferCancellationReasonRepository.findById(TransferCancellationReason.pk("ADMI")).orElseThrow(),
        )
        .build(),
    )

    entityManager.flush()

    assertThat(
      offenderIndividualScheduleRepository.findById(persistedEntity.id).orElseThrow(),
    ).isEqualTo(persistedEntity)
  }

  companion object {
    private val EVENT_DATE: LocalDate = LocalDate.now()

    private val START_TIME: LocalDateTime = EVENT_DATE.atTime(12, 0)
  }
}
