package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.SearchLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.Visit
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitType
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
@WithMockAuthUser
class VisitRepositoryTest(
  @Autowired private val repository: VisitRepository,
  @Autowired private val offenderBookingRepository: OffenderBookingRepository,
  @Autowired private val visitOrderRepository: VisitOrderRepository,
  @Autowired private val visitVisitorRepository: VisitVisitorRepository,
  @Autowired private val visitTypeRepository: ReferenceCodeRepository<VisitType?>,
  @Autowired private val visitStatusRepository: ReferenceCodeRepository<VisitStatus?>,
  @Autowired private val searchRepository: ReferenceCodeRepository<SearchLevel?>,
  @Autowired private val agencyRepository: AgencyLocationRepository,
  @Autowired private val agencyInternalRepository: AgencyInternalLocationRepository,
  @Autowired private val agencyVisitSlotRepository: AgencyVisitSlotRepository,
  @Autowired private val personRepository: PersonRepository,
  @Autowired private val entityManager: TestEntityManager,
) {

  @Test
  fun saveVisit() {
    val offenderBooking = offenderBookingRepository.findById(-10L).orElseThrow()
    val visit = Visit.builder()
      .offenderBooking(offenderBooking)
      .visitType(visitTypeRepository.findById(VisitType.pk("SCON")).orElseThrow())
      .visitStatus(visitStatusRepository.findById(VisitStatus.pk("SCH")).orElseThrow())
      .commentText("comment text")
      .visitDate(LocalDate.of(2009, 12, 21))
      .startTime(LocalDateTime.of(2009, 12, 21, 13, 15))
      .endTime(LocalDateTime.of(2009, 12, 21, 14, 15))
      .location(agencyRepository.findById("LEI").orElseThrow())
      .searchLevel(searchRepository.findById(SearchLevel.pk("FULL")).orElseThrow())
      .agencyInternalLocation(agencyInternalRepository.findById(-3L).orElseThrow())
      .visitorConcernText("visitor concerns")
      .visitOrder(visitOrderRepository.findById(-2L).orElseThrow())
      .agencyVisitSlot(agencyVisitSlotRepository.findById(-1L).orElseThrow())
      .build()
    repository.save<Visit>(visit)

    entityManager.flush()

    val persistedVisitList = repository.findByOffenderBooking(offenderBooking)
    assertThat(persistedVisitList).isNotEmpty()
    val persistedVisit = persistedVisitList[0]

    visitVisitorRepository.save<VisitVisitor>(
      VisitVisitor.builder()
        .offenderBooking(offenderBooking)
        .visitId(persistedVisitList[0].id)
        .groupLeader(true).assistedVisit(true)
        .person(personRepository.findById(-1L).orElseThrow()).build(),
    )

    entityManager.flush()
    entityManager.refresh<Visit>(persistedVisit)

    assertThat(persistedVisit.visitDate).isEqualTo(LocalDate.of(2009, 12, 21))
    assertThat(persistedVisit.startTime).isEqualTo(LocalDateTime.of(2009, 12, 21, 13, 15))
    assertThat(persistedVisit.endTime).isEqualTo(LocalDateTime.of(2009, 12, 21, 14, 15))
    assertThat(persistedVisit.id).isNotNull()
    assertThat(persistedVisit.searchLevel.description).isEqualTo("Full Search")
    assertThat(persistedVisit.visitStatus.description).isEqualTo("Scheduled")
    assertThat(persistedVisit.visitorConcernText).isEqualTo("visitor concerns")

    val visitOrder = persistedVisit.visitOrder
    assertThat(visitOrder).isNotNull()
    assertThat(visitOrder.visitOrderType.description).isEqualTo("Visiting Order")
    assertThat(visitOrder.issueDate).isEqualTo(LocalDate.of(2001, 1, 1))
    assertThat(visitOrder.status.description).isEqualTo("Active")
    assertThat(visitOrder.commentText).isEqualTo("Some VO Comment Text")
    assertThat(visitOrder.offenderBooking.bookingId).isEqualTo(-10)

    val agencyVisitSlot = persistedVisit.agencyVisitSlot
    assertThat(agencyVisitSlot.maxAdults).isEqualTo(18017)
    assertThat(agencyVisitSlot.maxGroups).isEqualTo(30)
    assertThat(agencyVisitSlot.weekDay).isEqualTo("SUN")
    assertThat(agencyVisitSlot.location.id).isEqualTo("LEI")
    assertThat(agencyVisitSlot.agencyInternalLocation.description).isEqualTo("LEI-A-1-1")

    val visitOrderVisitors = visitOrder.visitors
    assertThat(visitOrderVisitors.size).isEqualTo(1)

    val visitOrderVisitorPerson = visitOrderVisitors[0].person
    assertThat(visitOrderVisitorPerson).isNotNull()
    assertThat(visitOrderVisitorPerson.id).isEqualTo(-1)

    val visitVisitors = persistedVisit.visitors
    assertThat(visitVisitors.size).isEqualTo(1)
    val visitVisitor = visitVisitors[0]
    assertThat(visitVisitor.isGroupLeader).isTrue()
    assertThat(visitVisitor.isAssistedVisit).isTrue()

    val visitVisitorPerson = visitVisitors[0].person
    assertThat(visitVisitorPerson.id).isEqualTo(-1)
  }
}
