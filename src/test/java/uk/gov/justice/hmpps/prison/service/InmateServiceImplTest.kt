package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.AssignedLivingUnit
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail
import uk.gov.justice.hmpps.prison.api.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment
import uk.gov.justice.hmpps.prison.api.model.SecondaryLanguage
import uk.gov.justice.hmpps.prison.api.model.UserDetail
import uk.gov.justice.hmpps.prison.api.support.Page
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.OffenderAssessmentService.CurrentCsraAssessment
import uk.gov.justice.hmpps.prison.service.support.AssessmentDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.Optional

class InmateServiceImplTest {
  private val inmateAlertService: InmateAlertService = mock()
  private val repository: InmateRepository = mock()
  private val caseLoadService: CaseLoadService = mock()
  private val bookingService: BookingService = mock()
  private val agencyService: AgencyService = mock()
  private val userService: UserService = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val offenderAssessmentService: OffenderAssessmentService = mock()
  private val offenderLanguageRepository: OffenderLanguageRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val healthService: HealthService = mock()

  private var serviceToTest: InmateService = InmateService(
    repository, caseLoadService, inmateAlertService,
    referenceDomainService, bookingService, agencyService, healthService, userService, authenticationFacade,
    telemetryClient, 100, offenderAssessmentService, offenderLanguageRepository,
    offenderRepository, externalMovementRepository, null,
  )

  @Test
  fun testGetAssessments() {
    // need to ensure we OrderBy:
    // Order.DESC, "cellSharingAlertFlag"
    // Order.DESC, "assessmentDate"
    // 	Order.DESC, "assessmentSeq"
    val data = listOf(
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 2)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 1)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 8)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 7)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).build(),
    )
    whenever(repository.findAssessments(listOf(10L), null, emptySet())).thenReturn(data)
    val assessments = serviceToTest.getAssessments(10L)
    assertThat(assessments).hasSize(4) // 1 per code
    assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate").contains(
      tuple(10L, "CODE1", LocalDate.of(2018, Month.APRIL, 5)),
      tuple(10L, "CODE2", LocalDate.of(2018, Month.APRIL, 8)),
      tuple(10L, "CODE3", LocalDate.of(2018, Month.MAY, 2)),
      tuple(10L, "CODE4", LocalDate.of(2018, Month.MAY, 7)),
    )
  }

  @Test
  fun testGetInmatesCsraAssessmentsByCodeMostRecent() {
    val data = listOf(
      AssessmentDto.builder().calcSupLevelType("STANDARD").overridedSupLevelType("STANDARD").bookingId(10L)
        .offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.APRIL, 5),
        ).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(
          LocalDate.of(2018, Month.MAY, 5),
        ).assessmentSeq(3).build(),
      AssessmentDto.builder().reviewSupLevelType("HI").calcSupLevelType("HI").bookingId(10L).offenderNo("OFFENDER10")
        .assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.APRIL, 4),
        ).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(
          LocalDate.of(2018, Month.JUNE, 5),
        ).assessmentSeq(2).build(),
      AssessmentDto.builder().reviewSupLevelType("STANDARD").calcSupLevelType("STANDARD").bookingId(10L)
        .offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.APRIL, 1),
        ).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(
          LocalDate.of(2018, Month.OCTOBER, 5),
        ).assessmentSeq(1).build(),
      AssessmentDto.builder().bookingId(11L).calcSupLevelType("STANDARD").overridedSupLevelType("STANDARD")
        .offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.MAY, 7),
        ).cellSharingAlertFlag(true).assessmentCreateLocation("EXI").assessmentSeq(2).build(),
      AssessmentDto.builder().bookingId(11L).calcSupLevelType("HI").offenderNo("OFFENDER11").assessmentCode("THECODE")
        .assessmentDate(
          LocalDate.of(2018, Month.MAY, 6),
        ).cellSharingAlertFlag(true).assessmentSeq(1).build(),
      AssessmentDto.builder().bookingId(12L).calcSupLevelType("HI").overridedSupLevelType("HI").offenderNo("OFFENDER12")
        .assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.MAY, 6),
        ).cellSharingAlertFlag(true).assessmentSeq(1).build(),
      AssessmentDto.builder().bookingId(12L).calcSupLevelType("STANDARD").offenderNo("OFFENDER12")
        .assessmentCode("THECODE").assessmentDate(
          LocalDate.of(2018, Month.MAY, 7),
        ).cellSharingAlertFlag(true).assessmentCreateLocation("EXI").assessmentSeq(2).build(),
    )
    whenever(
      repository.findAssessmentsByOffenderNo(
        mutableListOf("OFFENDER10", "OFFENDER11", "OFFENDER12"),
        "THECODE",
        emptySet(),
        true,
        true,
      ),
    ).thenReturn(data)
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(true)
    val assessments = serviceToTest.getInmatesAssessmentsByCode(
      mutableListOf("OFFENDER10", "OFFENDER11", "OFFENDER12"),
      "THECODE",
      true,
      true,
      true,
      true,
    )

    // tests reviewed csra
    assertThat(
      assessments.first { it.bookingId == 10L }.classificationCode,
    ).isEqualTo("HI")
    // tests calculated csra
    assertThat(
      assessments.first { it.bookingId == 11L }.classificationCode,
    ).isEqualTo("HI")
    // test default
    assertThat(
      assessments.first { it.bookingId == 12L }.classificationCode,
    ).isEqualTo("STANDARD")
  }

  @Test
  fun testGetInmatesAssessmentsByCodeMostRecent() {
    val data = listOf(
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 5),
      ).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(
        LocalDate.of(2018, Month.MAY, 5),
      ).assessmentSeq(3).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 4),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(
        LocalDate.of(2018, Month.JUNE, 5),
      ).assessmentSeq(2).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 1),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(
        LocalDate.of(2018, Month.OCTOBER, 5),
      ).assessmentSeq(1).build(),
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.MAY, 7),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("EXI").assessmentSeq(2).build(),
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.MAY, 6),
      ).cellSharingAlertFlag(true).assessmentSeq(1).build(),
    )
    whenever(
      repository.findAssessmentsByOffenderNo(
        mutableListOf("OFFENDER10", "OFFENDER11"),
        "THECODE",
        emptySet(),
        false,
        false,
      ),
    ).thenReturn(data)
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(true)
    val assessments = serviceToTest.getInmatesAssessmentsByCode(
      mutableListOf("OFFENDER10", "OFFENDER11"),
      "THECODE",
      false,
      false,
      false,
      true,
    )
    assertThat(assessments).hasSize(2) // 1 per offender
    assertThat(assessments).extracting(
      "bookingId",
      "assessmentCode",
      "assessmentDate",
      "assessmentAgencyId",
      "approvalDate",
      "assessmentSeq",
    ).containsExactly(
      tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5), "MDI", LocalDate.of(2018, Month.MAY, 5), 3),
      tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 7), "EXI", null, 2),
    )
  }

  @Test
  fun testGetInmatesAssessmentsByCodeIncludingHistorical() {
    val data = listOf(
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 5),
      ).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(
        LocalDate.of(2018, Month.MAY, 5),
      ).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 4),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(
        LocalDate.of(2018, Month.JUNE, 5),
      ).build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.APRIL, 1),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(
        LocalDate.of(2018, Month.OCTOBER, 5),
      ).build(),
      AssessmentDto.builder().bookingId(9L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2016, Month.APRIL, 1),
      ).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(
        LocalDate.of(2016, Month.OCTOBER, 5),
      ).build(),
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.MAY, 7),
      ).cellSharingAlertFlag(true).build(),
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(
        LocalDate.of(2018, Month.MAY, 6),
      ).cellSharingAlertFlag(true).build(),
    )
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(true)
    whenever(
      repository.findAssessmentsByOffenderNo(
        mutableListOf("OFFENDER10", "OFFENDER11"),
        "THECODE",
        emptySet(),
        false,
        true,
      ),
    ).thenReturn(data)
    val assessments = serviceToTest.getInmatesAssessmentsByCode(
      mutableListOf("OFFENDER10", "OFFENDER11"),
      "THECODE",
      false,
      true,
      false,
      false,
    )
    assertThat(assessments).hasSize(6)
    assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate").containsExactly(
      tuple(9L, "THECODE", LocalDate.of(2016, Month.APRIL, 1)),
      tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5)),
      tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 4)),
      tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 1)),
      tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 7)),
      tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 6)),
    )
  }

  @Test
  fun testAllCodes() {
    // Ensure Ordering is same as from repository
    val data = listOf(
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).reviewSupLevelType("STANDARD")
        .reviewSupLevelTypeDesc("Standard").build(),
      AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1")
        .assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH")
        .reviewSupLevelTypeDesc("High").build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH")
        .reviewSupLevelTypeDesc("High").build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).reviewSupLevelType("LOW")
        .reviewSupLevelTypeDesc("Low").build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 3)).cellSharingAlertFlag(true).reviewSupLevelType("MED")
        .reviewSupLevelTypeDesc("Medium").build(),
      AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2")
        .assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(false).reviewSupLevelType("HIGH")
        .reviewSupLevelTypeDesc("High").build(),
    )
    whenever(
      repository.findAssessmentsByOffenderNo(
        mutableListOf("OFFENDER10", "OFFENDER11"),
        null,
        emptySet(),
        false,
        true,
      ),
    ).thenReturn(data)
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(true)
    val assessments = serviceToTest.getInmatesAssessmentsByCode(
      mutableListOf("OFFENDER10", "OFFENDER11"),
      null,
      false,
      true,
      false,
      true,
    )
    assertThat(assessments).hasSize(2) // 1 per offender
    assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "classification")
      .contains(
        tuple(10L, "CODE3", LocalDate.of(2018, Month.APRIL, 5), "High"),
        tuple(11L, "CODE1", LocalDate.of(2018, Month.MAY, 7), "Standard"),
      )
  }

  @Test
  fun testCreateCategorisation() {
    val catDetail =
      CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment").build()
    whenever(bookingService.getLatestBookingByBookingId(1234L))
      .thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build())
    whenever(userService.getUserByUsername("ME"))
      .thenReturn(UserDetail.builder().staffId(444L).username("ME").build())
    whenever(repository.insertCategory(catDetail, "CDI", 444L, "ME"))
      .thenReturn(mapOf("sequenceNumber" to 2L, "bookingId" to -5L))
    whenever(authenticationFacade.getCurrentUsername()).thenReturn("ME")
    val responseMap = serviceToTest.createCategorisation(1234L, catDetail)
    assertThat(responseMap)
      .contains(Assertions.entry("bookingId", -5L), Assertions.entry("sequenceNumber", 2L))
    assertThat(catDetail.nextReviewDate).isEqualTo(LocalDate.now().plusMonths(6))
    Mockito.verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME")
  }

  @Test
  fun testCreateCategorisationWithReviewDateSpecified() {
    val catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment")
      .nextReviewDate(LocalDate.of(2019, 4, 1)).build()
    whenever(bookingService.getLatestBookingByBookingId(1234L))
      .thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build())
    whenever(userService.getUserByUsername("ME"))
      .thenReturn(UserDetail.builder().staffId(444L).username("ME").build())
    whenever(authenticationFacade.getCurrentUsername()).thenReturn("ME")
    serviceToTest.createCategorisation(1234L, catDetail)
    Mockito.verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME")
  }

  @Test
  fun testMappingForOffenderDetailsAreCorrect() {
    val offenderNumbers = setOf("A123")
    val caseLoadsIds = setOf("1")
    whenever(authenticationFacade.getCurrentUsername()).thenReturn("ME")
    whenever(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoadsIds)
    whenever(repository.getBasicInmateDetailsForOffenders(offenderNumbers, false, caseLoadsIds, true))
      .thenReturn(
        java.util.List.of(
          InmateBasicDetails.builder()
            .lastName("LAST NAME")
            .firstName("FIRST NAME")
            .middleName("MIDDLE NAME")
            .build(),
        ),
      )
    val offenders = serviceToTest.getBasicInmateDetailsForOffenders(offenderNumbers, true)
    assertThat(offenders)
      .containsExactly(
        InmateBasicDetails.builder()
          .lastName("Last Name")
          .firstName("First Name")
          .middleName("Middle Name")
          .build(),
      )
  }

  @Test
  fun testThatAnExceptionIsThrown_whenAStandardUserWithNoActiveCaseloadsRequestsInmateDetails() {
    whenever(authenticationFacade.getCurrentUsername()).thenReturn("ME")
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(false)
    whenever(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(emptySet())
    Assertions.assertThatThrownBy { serviceToTest.getBasicInmateDetailsForOffenders(setOf("A123"), true) }
      .isInstanceOf(HttpClientErrorException::class.java)
      .hasMessageContaining("User has no active caseloads")
  }

  @Test
  fun testThatAnExceptionIsNotThrown_whenGlobalSearchUserWithNoActiveCaseloadsRequestsInmateDetails() {
    whenever(
      authenticationFacade.isOverrideRole(anyVararg()),
    ).thenReturn(true)
    serviceToTest.getBasicInmateDetailsForOffenders(setOf("A123"), false)
    Mockito.verify(repository).getBasicInmateDetailsForOffenders(setOf("A123"), true, emptySet(), false)
  }

  @Test
  fun testThatGetBasicInmateDetailsForOffenders_isCalledWithCorrectParameters() {
    val caseLoad = setOf("LEI")
    whenever(authenticationFacade.getCurrentUsername()).thenReturn("ME")
    whenever(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoad)
    serviceToTest.getBasicInmateDetailsForOffenders(setOf("A123"), true)
    Mockito.verify(repository).getBasicInmateDetailsForOffenders(setOf("A123"), false, caseLoad, true)
    Mockito.verify(caseLoadService).getCaseLoadIdsForUser("ME", false)
  }

  @Test
  fun testGetReasonableAdjustmentsByType() {
    val types = listOf("PEEP", "WHEELCHR_ACC")
    val reasonableAdjustments = java.util.List.of(
      ReasonableAdjustment.builder().treatmentCode("WHEELCHR_ACC").commentText("abcd")
        .startDate(LocalDate.parse("2019-01-02")).build(),
    )
    whenever(repository.findReasonableAdjustments(1L, types)).thenReturn(reasonableAdjustments)
    serviceToTest.getReasonableAdjustments(1L, types)
    Mockito.verify(repository).findReasonableAdjustments(1L, types)
  }

  @Test
  fun testGetSecondaryLanguages() {
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(
      java.util.List.of(
        OffenderLanguage.builder()
          .offenderBookId(-1L)
          .speakSkill("Y")
          .readSkill("n")
          .writeSkill("Y")
          .code("ENG")
          .type("SEC")
          .referenceCode(LanguageReferenceCode("ENG", "English"))
          .build(),
        OffenderLanguage.builder()
          .offenderBookId(-1L)
          .speakSkill("Y")
          .readSkill("n")
          .writeSkill("Y")
          .code("ENG")
          .referenceCode(LanguageReferenceCode("ENG", "English"))
          .build(),
        OffenderLanguage.builder()
          .offenderBookId(-1L)
          .code("LAT")
          .type("SEC")
          .referenceCode(LanguageReferenceCode("LAT", "Latvian"))
          .build(),
        OffenderLanguage.builder()
          .offenderBookId(-1L)
          .code("TUR")
          .type("PREF_SPEAK")
          .referenceCode(LanguageReferenceCode("TUR", "Turkish"))
          .build(),
      ),
    )
    val secondaryLanguages = serviceToTest.getSecondaryLanguages(-1L)
    assertThat(secondaryLanguages).containsExactlyInAnyOrder(
      SecondaryLanguage.builder()
        .bookingId(-1L)
        .code("ENG")
        .description("English")
        .canSpeak(true)
        .canRead(false)
        .canWrite(true)
        .build(),
      SecondaryLanguage.builder()
        .bookingId(-1L)
        .code("LAT")
        .description("Latvian")
        .canSpeak(false)
        .canRead(false)
        .canWrite(false)
        .build(),
    )
  }

  @Test
  fun offenderDetails_LocationDescriptionAndIdPrisonerReleased() {
    whenever(repository.findOffender(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnit()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getOffenderIdentifiersByOffenderId(anyLong())).thenReturn(listOf())
    whenever(
      externalMovementRepository.findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(
        any(),
      ),
    ).thenReturn(buildMovementReleased("REL", ""))
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findOffender("S1234AA", true, false)
    assertThat(inmateDetail.locationDescription).isEqualTo("Outside - released from Leeds")
    assertThat(inmateDetail.latestLocationId).isEqualTo("LEI")
  }

  @Test
  fun offenderDetails_LocationDescriptionAndIdPrisonerTransferred() {
    whenever(repository.findOffender(any()))
      .thenReturn(Optional.of(buildInmateDetailTransferring()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnitTransferred()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getOffenderIdentifiersByOffenderId(anyLong())).thenReturn(listOf())
    whenever(
      externalMovementRepository.findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(
        any(),
      ),
    ).thenReturn(buildMovementTransferred("REL", ""))
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findOffender("S1234AA", true, false)
    assertThat(inmateDetail.locationDescription).isEqualTo("Transfer")
    assertThat(inmateDetail.latestLocationId).isEqualTo("LEI")
  }

  @Test
  fun offenderDetails_LocationDescriptionAmdIdPrisonerTemporaryAbsence() {
    whenever(repository.findOffender(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnit()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getOffenderIdentifiersByOffenderId(anyLong())).thenReturn(listOf())
    whenever(
      externalMovementRepository.findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(
        any(),
      ),
    ).thenReturn(buildMovementReleased("TAP", "Temporary Absence"))
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findOffender("S1234AA", true, false)
    assertThat(inmateDetail.locationDescription).isEqualTo("Outside - Temporary Absence")
    assertThat(inmateDetail.latestLocationId).isEqualTo("LEI")
  }

  @Test
  fun offenderDetails_WhenMissingReleaseAgency() {
    whenever(repository.findOffender(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnit()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getOffenderIdentifiersByOffenderId(anyLong())).thenReturn(listOf())
    whenever(
      externalMovementRepository.findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(
        any(),
      ),
    ).thenReturn(buildMovementReleasedWithNullFromAgency("TAP", "Temporary Absence"))
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findOffender("S1234AA", true, false)
    assertThat(inmateDetail.locationDescription).isEqualTo("Outside")
    assertThat(inmateDetail.latestLocationId).isEqualTo("MDI")
  }

  @Test
  fun offenderDetails_LocationDescriptionAndIdPrisonerNoMovementDetailsFound() {
    whenever(repository.findOffender(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnitForOutside()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getOffenderIdentifiersByOffenderId(anyLong())).thenReturn(listOf())
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findOffender("S1234AA", true, false)
    assertThat(inmateDetail.locationDescription).isEqualTo("Outside")
    assertThat(inmateDetail.latestLocationId).isEqualTo("OUT")
  }

  @Test
  fun findInmate_CsraSummaryLoaded() {
    whenever(repository.findInmate(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnit()))
    whenever(offenderAssessmentService.getCurrentCsraClassification("S1234AA"))
      .thenReturn(CurrentCsraAssessment("STANDARD", LocalDate.parse("2019-02-01")))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    val inmateDetail = serviceToTest.findInmate(-1L, false, true)
    assertThat(inmateDetail.csraClassificationCode).isEqualTo("STANDARD")
    assertThat(inmateDetail.csraClassificationDate).isEqualTo(LocalDate.parse("2019-02-01"))
  }

  @Test
  fun findInmate_extraInfo_imprisonmentStatusDetails() {
    val imprisonmentStatus = ImprisonmentStatus()
    imprisonmentStatus.imprisonmentStatus = "LIFE"
    imprisonmentStatus.description = "Life imprisonment"
    whenever(repository.findInmate(any())).thenReturn(Optional.of(buildInmateDetail()))
    whenever(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(listOf())
    whenever(repository.findPhysicalAttributes(anyLong()))
      .thenReturn(Optional.of(buildPhysicalAttributes()))
    whenever(repository.findPhysicalCharacteristics(anyLong())).thenReturn(listOf())
    whenever(repository.getProfileInformation(anyLong())).thenReturn(listOf())
    whenever(repository.findAssignedLivingUnit(anyLong()))
      .thenReturn(Optional.of(buildAssignedLivingUnit()))
    whenever(
      inmateAlertService.getInmateAlerts(
        anyLong(),
        any(),
        anyOrNull(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(repository.getImprisonmentStatus(anyLong()))
      .thenReturn(Optional.of(imprisonmentStatus))
    whenever(
      repository.findInmateAliasesByBooking(
        anyLong(),
        anyString(),
        any(),
        anyLong(),
        anyLong(),
      ),
    ).thenReturn(
      Page(emptyList(), 0, 0, 0),
    )
    whenever(healthService.getPersonalCareNeeds(anyLong(), anyList()))
      .thenReturn(
        PersonalCareNeeds("A1234BC", listOf()),
      )
    val inmateDetail = serviceToTest.findInmate(-1L, true, false)
    assertThat(inmateDetail.imprisonmentStatus).isEqualTo("LIFE")
    assertThat(inmateDetail.imprisonmentStatusDescription).isEqualTo("Life imprisonment")
  }

  private fun buildInmateDetail(): InmateDetail {
    return InmateDetail.builder()
      .offenderNo("S1234AA")
      .bookingId(-1L)
      .bookingNo("Z00001")
      .offenderId(-999L)
      .rootOffenderId(-999L)
      .firstName("FRED")
      .lastName("JAMES")
      .dateOfBirth(LocalDate.of(1955, 12, 1))
      .age(65)
      .agencyId("outside")
      .assignedLivingUnitId(-13L)
      .birthCountryCode("UK")
      .inOutStatus("OUT")
      .status("REL-HP")
      .lastMovementTypeCode("REL")
      .lastMovementReasonCode("HP")
      .build()
  }

  private fun buildInmateDetailTransferring(): InmateDetail {
    return buildInmateDetail()
      .toBuilder()
      .agencyId("TRN")
      .status("INACTIVE TRN")
      .lastMovementTypeCode("TRN")
      .lastMovementReasonCode("PROD")
      .inOutStatus("TRN")
      .build()
  }

  private fun buildPhysicalAttributes(): PhysicalAttributes {
    return PhysicalAttributes.builder()
      .gender("Male")
      .raceCode("W2")
      .ethnicity("White: Irish")
      .build()
  }

  private fun buildAssignedLivingUnit(): AssignedLivingUnit {
    return AssignedLivingUnit.builder()
      .agencyId("MDI")
      .locationId(-41L)
      .description("1-1-001")
      .agencyName("MOORLAND")
      .build()
  }

  private fun buildAssignedLivingUnitForOutside(): AssignedLivingUnit {
    return AssignedLivingUnit.builder()
      .agencyId("OUT")
      .agencyName("Outside")
      .build()
  }

  private fun buildAssignedLivingUnitTransferred(): AssignedLivingUnit {
    return AssignedLivingUnit.builder()
      .agencyId("TRN")
      .agencyName("Transfer")
      .build()
  }

  private fun buildMovementReleased(movementType: String, movementTypeDescription: String): Optional<ExternalMovement> {
    val now = LocalDateTime.now()
    return Optional.of(
      ExternalMovement.builder()
        .movementDate(now.toLocalDate())
        .movementTime(now)
        .fromAgency(
          AgencyLocation.builder().id("LEI").description("Leeds").type(AgencyLocationType.PRISON_TYPE).build(),
        )
        .toAgency(
          AgencyLocation.builder().id("OUT").description("Outside").type(AgencyLocationType.PRISON_TYPE).build(),
        )
        .movementDirection(MovementDirection.OUT)
        .movementType(MovementType(movementType, movementTypeDescription))
        .movementReason(MovementReason(MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code, "to hospital"))
        .build(),
    )
  }

  private fun buildMovementReleasedWithNullFromAgency(
    movementType: String,
    movementTypeDescription: String,
  ): Optional<ExternalMovement> {
    val now = LocalDateTime.now()
    return Optional.of(
      ExternalMovement.builder()
        .movementDate(now.toLocalDate())
        .movementTime(now)
        .toAgency(
          AgencyLocation.builder().id("OUT").description("Outside").type(AgencyLocationType.PRISON_TYPE).build(),
        )
        .movementDirection(MovementDirection.OUT)
        .movementType(MovementType(movementType, movementTypeDescription))
        .movementReason(MovementReason(MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code, "to hospital"))
        .build(),
    )
  }

  private fun buildMovementTransferred(
    movementType: String,
    movementTypeDescription: String,
  ): Optional<ExternalMovement> {
    val now = LocalDateTime.now()
    return Optional.of(
      ExternalMovement.builder()
        .movementDate(now.toLocalDate())
        .movementTime(now)
        .fromAgency(
          AgencyLocation.builder().id("LEI").description("Leeds").type(AgencyLocationType.PRISON_TYPE).build(),
        )
        .toAgency(
          AgencyLocation.builder().id("MDI").description("Moorland").type(AgencyLocationType.PRISON_TYPE).build(),
        )
        .movementDirection(MovementDirection.OUT)
        .movementType(MovementType(movementType, movementTypeDescription))
        .movementReason(MovementReason("P", "PRODUCTION"))
        .build(),
    )
  }
}
