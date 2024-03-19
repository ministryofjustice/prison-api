package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
  AuthenticationFacade::class,
  AuditorAwareImpl::class,
)
@WithMockUser
class AdjudicationRepositoryTest {
  @Autowired
  private lateinit var repository: AdjudicationRepository

  @Autowired
  private lateinit var adjudicationOffenceTypeRepository: AdjudicationOffenceTypeRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Autowired
  private lateinit var agencyInternalLocationRepository: AgencyInternalLocationRepository

  @Autowired
  private lateinit var bookingRepository: OffenderBookingRepository

  @Autowired
  private lateinit var staffUserAccountRepository: StaffUserAccountRepository

  @Autowired
  private lateinit var incidentTypeRepository: ReferenceCodeRepository<AdjudicationIncidentType>

  @Autowired
  private lateinit var actionCodeRepository: ReferenceCodeRepository<AdjudicationActionCode>

  @Test
  fun adjudicationCreated() {
    val adjudicationToCreate = makeAdjudicationObject()
    val savedAdjudication = repository.save(adjudicationToCreate)
    val storedAdjudication = repository.findById(savedAdjudication.agencyIncidentId)
    assertThat(storedAdjudication.get()).usingRecursiveComparison().ignoringFields("agencyIncidentId")
      .isEqualTo(adjudicationToCreate)

    // Revert the save
    repository.delete(storedAdjudication.get())
  }

  @Test
  fun adjudicationSearchByNumber() {
    val adjudicationNumber = -5L
    val storedAdjudication = repository.findByParties_AdjudicationNumber(adjudicationNumber)
    val incidentDateAndTime = LocalDateTime.of(1999, 5, 25, 0, 0)
    val reportDate = LocalDateTime.of(1999, 5, 25, 0, 0)
    val reportTime = LocalDateTime.of(2019, 1, 25, 0, 2)
    val partyAddedTime = LocalDateTime.of(2005, 11, 15, 0, 0)
    val incidentType = AdjudicationIncidentType.MISCELLANEOUS
    val actionCode = AdjudicationActionCode.PLACED_ON_REPORT
    val expectedAdjudication = Adjudication.builder()
      .agencyIncidentId(-2L)
      .incidentDate(incidentDateAndTime.toLocalDate())
      .incidentTime(incidentDateAndTime)
      .reportDate(reportDate.toLocalDate())
      .reportTime(reportTime)
      .agencyLocation(agencyLocationRepository.findById("LEI").get())
      .internalLocation(agencyInternalLocationRepository.findById(-2L).get())
      .incidentDetails("mKSouDOCmKSouDO")
      .incidentStatus("ACTIVE")
      .incidentType(incidentTypeRepository.findById(incidentType).get())
      .lockFlag("N")
      .staffReporter(staffUserAccountRepository.findById("JBRIEN").get().staff)
      .build()
    val adjudicationParty1 = AdjudicationParty.builder()
      .id(AdjudicationParty.PK(expectedAdjudication, 1L))
      .adjudicationNumber(adjudicationNumber)
      .incidentRole("S")
      .partyAddedDate(partyAddedTime.toLocalDate())
      .offenderBooking(bookingRepository.findById(-49L).get()) // -51L
      .actionCode(actionCodeRepository.findById(actionCode).get()) // ??
      .build()
    val adjudicationParty2 = AdjudicationParty.builder()
      .id(AdjudicationParty.PK(expectedAdjudication, 2L))
      .adjudicationNumber(-6L)
      .incidentRole("V")
      .partyAddedDate(partyAddedTime.toLocalDate())
      .offenderBooking(bookingRepository.findById(-51L).get())
      .actionCode(actionCodeRepository.findById(actionCode).get()) // ??
      .build()
    val adjudicationParty1ChargeOffenceCode = "51:8D"
    assertThat(storedAdjudication.get()).usingRecursiveComparison()
      .ignoringFields("createDatetime", "createUserId", "modifyDatetime", "modifyUserId", "parties")
      .isEqualTo(expectedAdjudication)
    assertThat(storedAdjudication.get().parties).usingRecursiveComparison()
      .ignoringFields("id", "charges", "createDatetime", "createUserId", "modifyDatetime", "modifyUserId")
      .isEqualTo(listOf(adjudicationParty1, adjudicationParty2))
    assertThat(
      storedAdjudication.get().parties[0].charges,
    ).hasSize(1)
      .extracting("offenceType.offenceCode")
      .isEqualTo(listOf(adjudicationParty1ChargeOffenceCode))
    assertThat(
      storedAdjudication.get().parties[1].charges,
    ).hasSize(0)
  }

  private fun makeAdjudicationObject(): Adjudication {
    val reportedDateAndTime = LocalDateTime.now()
    val incidentDateAndTime = reportedDateAndTime.minusDays(2)
    val partyAddedDateAndTime = reportedDateAndTime.minusDays(1)
    val offenceCode = "51:2D"
    val offenderBookingId = -6L
    val agencyId = "LEI"
    val internalLocationId = -14L
    val reporterUsername = "PPL_USER"
    val incidentDetails = "A detail"
    val incidentStatus = "ACTIVE"
    val lockFlag = "Y"
    val incidentRole = "S"
    val incidentType = AdjudicationIncidentType.GOVERNORS_REPORT
    val actionCode = AdjudicationActionCode.PLACED_ON_REPORT
    val agencyLocation = agencyLocationRepository.findById(agencyId)
    val agencyInternalLocation = agencyInternalLocationRepository.findById(internalLocationId)
    val reporter = staffUserAccountRepository.findById(reporterUsername)
    val offenderBooking = bookingRepository.findById(offenderBookingId)
    val incidentTypeRef = incidentTypeRepository.findById(incidentType)
    val actionCodeRef = actionCodeRepository.findById(actionCode)
    val adjudicationNumber = repository.getNextAdjudicationNumber()
    val adjudicationOffenceType = adjudicationOffenceTypeRepository.findByOffenceCodeIn(listOf(offenceCode))[0]
    val adjudicationToCreate = Adjudication.builder()
      .incidentDate(incidentDateAndTime.toLocalDate())
      .incidentTime(incidentDateAndTime)
      .reportDate(reportedDateAndTime.toLocalDate())
      .reportTime(reportedDateAndTime)
      .agencyLocation(agencyLocation.get())
      .internalLocation(agencyInternalLocation.get())
      .incidentDetails(incidentDetails)
      .incidentStatus(incidentStatus)
      .incidentType(incidentTypeRef.get())
      .lockFlag(lockFlag)
      .staffReporter(reporter.get().staff)
      .build()
    val adjudicationParty = AdjudicationParty.builder()
      .id(AdjudicationParty.PK(adjudicationToCreate, 1L))
      .adjudicationNumber(adjudicationNumber)
      .incidentRole(incidentRole)
      .partyAddedDate(partyAddedDateAndTime.toLocalDate())
      .offenderBooking(offenderBooking.get())
      .actionCode(actionCodeRef.get())
      .build()
    val adjudicationCharge = AdjudicationCharge.builder()
      .id(AdjudicationCharge.PK(adjudicationParty, 1L))
      .offenceType(adjudicationOffenceType)
      .build()
    adjudicationParty.charges =
      listOf(adjudicationCharge)
    adjudicationToCreate.parties = listOf(adjudicationParty)
    return adjudicationToCreate
  }
}
