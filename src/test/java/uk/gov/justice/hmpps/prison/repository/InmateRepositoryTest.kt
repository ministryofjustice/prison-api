package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment
import uk.gov.justice.hmpps.prison.api.support.CategorisationStatus
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.support.AssessmentDto
import uk.gov.justice.hmpps.prison.util.Extractors.extractInteger
import uk.gov.justice.hmpps.prison.util.Extractors.extractString
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.util.Date

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class InmateRepositoryTest {
  @Autowired
  private lateinit var repository: InmateRepository

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun init() {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("itag_user", "password")
  }

  @Test
  fun testSearchForOffenderBookings() {
    val pageRequest = PageRequest("lastName, firstName")
    val caseloads = setOf("LEI", "BXI")
    val alertFilter = listOf("XA", "HC")
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(caseloads)
        .offenderNo("A1234AA")
        .searchTerm1("A")
        .searchTerm2("A")
        .locationPrefix("LEI")
        .alerts(alertFilter)
        .convictedStatus("All")
        .pageRequest(pageRequest)
        .build(),
    )
    val results = foundInmates.items
    assertThat(results).extracting("bookingId", "offenderNo", "dateOfBirth", "assignedLivingUnitDesc")
      .containsExactly(
        tuple(-1L, "A1234AA", LocalDate.of(1969, Month.DECEMBER, 30), "A-1-1"),
      )
  }

  @Test
  fun testSearchForOffenderBookingsSpaceInSurname() {
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(setOf("LEI", "MDI"))
        .searchTerm1("HAR")
        .searchTerm2("JO")
        .locationPrefix("MDI")
        .pageRequest(PageRequest("lastName, firstName"))
        .build(),
    )
    assertThat(foundInmates.items)
      .extracting(
        { it.bookingId },
        { it.offenderNo },
        { it.dateOfBirth },
        { it.assignedLivingUnitDesc },
      )
      .containsExactly(tuple(-55L, "A1180HL", LocalDate.parse("1980-05-21"), "1-2-014"))
  }

  @Test
  fun testSearchForOffenderBookingsSpaceInForename() {
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(setOf("LEI", "MDI"))
        .searchTerm1("JO")
        .searchTerm2("JAM")
        .locationPrefix("MDI")
        .pageRequest(PageRequest("lastName, firstName"))
        .build(),
    )
    assertThat(foundInmates.items)
      .extracting(
        { it.bookingId },
        { it.offenderNo },
        { it.dateOfBirth },
        { it.assignedLivingUnitDesc },
      )
      .containsExactly(tuple(-55L, "A1180HL", LocalDate.parse("1980-05-21"), "1-2-014"))
  }

  @Test
  fun testSearchForOffenderBookingsReturnsLatestActiveImprisonmentStatus() {
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(setOf("LEI", "MDI"))
        .searchTerm1("JO")
        .searchTerm2("JAM")
        .locationPrefix("MDI")
        .pageRequest(PageRequest("lastName, firstName"))
        .build(),
    )
    val inmates = foundInmates.items
    assertThat(inmates)
      .extracting(
        { it.bookingId },
        { it.imprisonmentStatus },
        { it.bandCode },
      )
      .containsExactly(tuple(-55L, "TRL", "12"))
  }

  @Test
  fun testSearchForOffenderBookingsReturnsEmptyImprisonmentStatusIfNone() {
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(setOf("MDI"))
        .searchTerm2("TRESCOTHICK")
        .locationPrefix("MDI")
        .pageRequest(PageRequest("lastName, firstName"))
        .build(),
    )
    val inmates = foundInmates.items
    assertThat(inmates)
      .extracting(
        { it.bookingId },
        { it.imprisonmentStatus },
        { it.bandCode },
      )
      .containsExactly(tuple(-35L, null, null))
  }

  @Test
  fun testSearchForConvictedOffenderBookings() {
    val pageRequest = PageRequest("lastName, firstName")
    val caseloads = setOf("LEI")
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(caseloads)
        .locationPrefix("LEI")
        .convictedStatus("Convicted")
        .pageRequest(pageRequest)
        .build(),
    )
    val results = foundInmates.items
    assertThat(results).hasSize(8)
    assertThat(results).extracting("convictedStatus").containsOnly("Convicted")
    assertThat(results).extracting("imprisonmentStatus").containsOnly("SENT", "DEPORT")
  }

  @Test
  fun testSearchForRemandOffenderBookings() {
    val pageRequest = PageRequest("lastName, firstName")
    val caseloads = setOf("LEI")
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(caseloads)
        .locationPrefix("LEI")
        .convictedStatus("Remand")
        .pageRequest(pageRequest)
        .build(),
    )
    val results = foundInmates.items
    assertThat(results).hasSize(3)
    assertThat(results).extracting("convictedStatus").containsOnly("Remand")
    assertThat(results).extracting("imprisonmentStatus").containsOnly("TRL")
  }

  @Test
  fun testSearchForAllConvictedStatus() {
    val pageRequest = PageRequest("lastName, firstName")
    val caseloads = setOf("LEI")
    val foundInmates = repository.searchForOffenderBookings(
      OffenderBookingSearchRequest.builder()
        .caseloads(caseloads)
        .locationPrefix("LEI")
        .convictedStatus("All")
        .pageRequest(pageRequest)
        .build(),
    )
    val results = foundInmates.items
    assertThat(results).hasSize(10)
    assertThat(results).extracting("convictedStatus").containsAll(listOf("Convicted", "Remand"))
    assertThat(results).extracting("imprisonmentStatus").containsAll(listOf("TRL", "SENT"))
  }

  @Test
  fun testGetOffender() {
    val inmate = repository.findInmate(-1L)
    assertThat(inmate).isPresent()
    val result = inmate.get()
    assertThat(result.bookingNo).isEqualTo("A00111")
    assertThat(result.bookingId).isEqualTo(-1L)
    assertThat(result.offenderNo).isEqualTo("A1234AA")
    assertThat(result.firstName).isEqualTo("ARTHUR")
    assertThat(result.middleName).isEqualTo("BORIS")
    assertThat(result.lastName).isEqualTo("ANDERSON")
    assertThat(result.agencyId).isEqualTo("LEI")
    assertThat(result.assignedLivingUnitId).isEqualTo(-3L)
    assertThat(result.dateOfBirth).isEqualTo(LocalDate.of(1969, 12, 30))
    assertThat(result.facialImageId).isEqualTo(-1L)
    assertThat(result.birthPlace).isEqualTo("WALES")
    assertThat(result.birthCountryCode).isEqualTo("UK")
  }

  @Test
  fun testGetBasicOffenderDetails() {
    val inmate = repository.getBasicInmateDetail(-1L)
    assertThat(inmate).isPresent()
  }

  @Test
  fun testFindOffendersWithValidOffenderNoOnly() {
    val testOffenderNo = "A1234AP"
    val query = buildQuery(criteriaForOffenderNo(listOf(testOffenderNo)))
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo(testOffenderNo)
    assertThat(offender.lastName).isEqualTo("SCISSORHANDS")
  }

  @Test
  fun testFindOffendersWithLocationFilterIN() {
    val query = buildQuery(criteriaForLocationFilter("IN"))
    val offenders = findOffendersWithAliasesFullResults(query)
    assertThat(offenders).isNotEmpty()
  }

  @Test
  fun testFindOffendersWithLocationFilterOut() {
    val query = buildQuery(criteriaForLocationFilter("OUT"))
    val offenders = findOffendersWithAliasesFullResults(query)
    assertThat(offenders).hasSizeGreaterThan(0)
  }

  @Test
  fun testFindOffendersWithLocationFilterALL() {
    val query = buildQuery(criteriaForLocationFilter("ALL"))
    val offenders = findOffendersWithAliasesFullResults(query)
    assertThat(offenders).hasSizeGreaterThanOrEqualTo(53)
  }

  @Test
  fun testFindOffendersWithGenderFilterMale() {
    val query = buildQuery(criteriaForGenderFilter("M"))
    val offenders = findOffendersWithAliasesFullResults(query)
    assertThat(offenders).hasSizeGreaterThanOrEqualTo(49)
  }

  @Test
  fun testFindOffendersWithGenderFilterALL() {
    val query = buildQuery(criteriaForGenderFilter("ALL"))
    val offenders = findOffendersWithAliasesFullResults(query)
    assertThat(offenders).hasSizeGreaterThanOrEqualTo(53)
  }

  @Test
  fun testFindOffendersWithInvalidOffenderNoOnly() {
    val testOffenderNo = listOf("X9999XX")
    val query = buildQuery(criteriaForOffenderNo(testOffenderNo))
    assertThat(findOffenders(query)).isEmpty()
  }

  @Test
  fun testFindOffendersWithValidPNCNumberOnly() {
    val testPncNumber = "14/12345F"
    val query = buildQuery(criteriaForPNCNumber(testPncNumber))
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AF")
    assertThat(offender.lastName).isEqualTo("ANDREWS")
  }

  @Test
  fun testFindOffendersWithInvalidPNCNumberOnly() {
    val testPncNumber = "PNC0193032"
    assertThatThrownBy { buildQuery(criteriaForPNCNumber(testPncNumber)) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
  }

  @Test
  fun testFindOffendersWithValidCRONumberOnly() {
    val testCroNumber = "CRO112233"
    val query = buildQuery(criteriaForCRONumber(testCroNumber))
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AC")
    assertThat(offender.lastName).isEqualTo("BATES")
  }

  @Test
  fun testFindOffendersWithLastName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null))
    val offenders = findOffenders(query)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ")
  }

  @Test
  fun testFindOffendersWithLastNameLowerCase() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "smith", null))
    val offenders = findOffenders(query)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ")
  }

  @Test
  fun testFindOffendersWithFirstName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"))
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AJ", "A1234AL")
  }

  @Test
  fun testFindOffendersWithFirstNameLowerCase() {
    val query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"))
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AJ", "A1234AL")
  }

  @Test
  fun testFindOffendersWithFirstNameAndLastName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "JONES", "HARRY"))
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AH")
  }

  @Test
  fun testFindOffendersWithDateOfBirth() {
    val criteria = criteriaForDOBRange(
      LocalDate.of(1964, 1, 1),
      null,
      null,
    )
    val query = buildQuery(criteria)
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("Z0021ZZ")
  }

  @Test
  fun testFindOffendersWithDateOfBirthRange() {
    val criteria = criteriaForDOBRange(
      null,
      LocalDate.of(1960, 1, 1),
      LocalDate.of(1969, 12, 31),
    )
    val query = buildQuery(criteria)
    val offenders = findOffenders(query)
    assertThat(offenders).hasSizeGreaterThanOrEqualTo(9)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA")
  }

  @Test
  fun testFindOffendersWithLastNameAndDateOfBirth() {
    var criteria = criteriaForPersonalAttrs(null, "QUIMBY", null)
    criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null)
    val query = buildQuery(criteria)
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("A1178RS")
  }

  @Test
  fun testFindOffendersWithPartialLastName() {
    val criteria = criteriaForPartialPersonalAttrs(null, "ST", null)
    val query = buildQuery(criteria)
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(3)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0019ZZ", "A9876RS", "A1182BS")
  }

  @Test
  fun testFindOffendersWithPartialFirstName() {
    val criteria = criteriaForPartialPersonalAttrs(null, null, "MIC")
    val query = buildQuery(criteria)
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(3)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0017ZZ", "A1180MA", "A1181MV")
  }

  @Test
  fun testFindOffendersWithPartialLastNameAndFirstName() {
    val criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA")
    val query = buildQuery(criteria)
    val offender = findOffender(query)
    assertThat(offender.offenderNo).isEqualTo("A1179MT")
  }

  @Test
  fun testFindOffendersWithLastNameOrFirstName() {
    val criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS")
    val query = buildQuery(criteria)
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1178RS", "A1179MT")
  }

  @Test
  fun testFindOffendersWithLastNameOrDateOfBirth() {
    var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null)
    criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null)
    val query = buildQuery(criteria)
    val offenders = findOffenders(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0021ZZ", "A1183CW")
  }

  /** */
  @Test
  fun testfindOffenderAliasesWithValidOffenderNoOnly() {
    val testOffenderNo = "A1234AP"
    val query = buildQuery(criteriaForOffenderNo(listOf(testOffenderNo)))
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo(testOffenderNo)
    assertThat(offender.lastName).isEqualTo("SCISSORHANDS")
  }

  @Test
  fun testfindOffenderAliasesWithInvalidOffenderNoOnly() {
    val testOffenderNo = listOf("X9999XX")
    val query = buildQuery(criteriaForOffenderNo(testOffenderNo))
    assertThat(findOffendersWithAliases(query)).isEmpty()
  }

  @Test
  fun testfindOffenderAliasesWithValidPNCNumberOnly() {
    val testPncNumber = "14/12345F"
    val query = buildQuery(criteriaForPNCNumber(testPncNumber))
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AF")
    assertThat(offender.lastName).isEqualTo("ANDREWS")
  }

  @Test
  fun testfindOffenderAliasesWithInvalidPNCNumberOnly() {
    val testPncNumber = "PNC0193032"
    assertThatThrownBy { buildQuery(criteriaForPNCNumber(testPncNumber)) }.isInstanceOf(
      IllegalArgumentException::class.java,
    )
  }

  @Test
  fun testfindOffenderAliasesWithValidCRONumberOnly() {
    val testCroNumber = "CRO112233"
    val query = buildQuery(criteriaForCRONumber(testCroNumber))
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AC")
    assertThat(offender.lastName).isEqualTo("BATES")
  }

  @Test
  fun testFindOffenderAliasesWithLastName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null))
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ")
  }

  @Test
  fun testFindOffenderAliasesWithLastNameLowerCase() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "smith", null))
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(4)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ")
  }

  @Test
  fun testFindOffenderAliasesWithFirstName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"))
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .containsOnly("A1234AJ", "A1234AL")
  }

  @Test
  fun testFindOffenderAliasesWithFirstNameLowerCase() {
    val query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"))
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .containsOnly("A1234AJ", "A1234AL")
  }

  @Test
  fun testFindOffenderAliasesWithFirstNameAndLastName() {
    val query = buildQuery(criteriaForPersonalAttrs(null, "JONES", "HARRY"))
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("A1234AH")
  }

  @Test
  fun testFindOffenderAliasesWithDateOfBirth() {
    val criteria = criteriaForDOBRange(
      LocalDate.of(1964, 1, 1),
      null,
      null,
    )
    val query = buildQuery(criteria)
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("Z0021ZZ")
  }

  @Test
  fun testFindOffenderAliasesWithDateOfBirthRange() {
    val criteria = criteriaForDOBRange(
      null,
      LocalDate.of(1960, 1, 1),
      LocalDate.of(1969, 12, 31),
    )
    val query = buildQuery(criteria)
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSizeGreaterThanOrEqualTo(9)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA")
  }

  @Test
  fun testFindOffenderAliasesWithLastNameAndDateOfBirth() {
    var criteria = criteriaForPersonalAttrs(null, "QUIMBY", null)
    criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null)
    val query = buildQuery(criteria)
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("A1178RS")
  }

  @Test
  fun testFindOffenderAliasesWithPartialLastName() {
    val criteria = criteriaForPartialPersonalAttrs(null, "ST", null)
    val query = buildQuery(criteria)
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(3)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0019ZZ", "A9876RS", "A1182BS")
  }

  @Test
  fun testFindOffenderAliasesWithPartialFirstName() {
    val criteria = criteriaForPartialPersonalAttrs(null, null, "MIC")
    val query = buildQuery(criteria)
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(3)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0017ZZ", "A1180MA", "A1181MV")
  }

  @Test
  fun testFindOffenderAliasesWithPartialLastNameAndFirstName() {
    val criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA")
    val query = buildQuery(criteria)
    val offender = findOffenderWithAliases(query)
    assertThat(offender.offenderNo).isEqualTo("A1179MT")
  }

  @Test
  fun testFindOffenderAliasesWithLastNameOrFirstName() {
    val criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS")
    val query = buildQuery(criteria)
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("A1178RS", "A1179MT")
  }

  @Test
  fun testFindOffenderAliasesWithLastNameOrDateOfBirth() {
    var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null)
    criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null)
    val query = buildQuery(criteria)
    val offenders = findOffendersWithAliases(query)
    assertThat(offenders).hasSize(2)
    assertThat(offenders).extracting<String, RuntimeException> { obj: PrisonerDetail -> obj.offenderNo }
      .contains("Z0021ZZ", "A1183CW")
  }

  @Test
  fun testGetUncategorisedGeneral() {
    val list = repository.getUncategorised("LEI")
    val sortedList = list.sortedBy { it.offenderNo }
    assertThat(sortedList)
      .extracting("offenderNo", "bookingId", "firstName", "lastName", "status", "category").contains(
        tuple("A1234AB", -2L, "GILLIAN", "ANDERSON", CategorisationStatus.UNCATEGORISED, null),
        tuple("A1234AC", -3L, "NORMAN", "BATES", CategorisationStatus.UNCATEGORISED, "X"),
        tuple("A1234AD", -4L, "CHARLES", "CHAPLIN", CategorisationStatus.UNCATEGORISED, "U"),
        tuple("A1234AE", -5L, "DONALD", "MATTHEWS", CategorisationStatus.UNCATEGORISED, "Z"),
      )
    assertThat(sortedList).extracting(
      "offenderNo",
      "bookingId",
      "firstName",
      "lastName",
      "status",
      "categoriserFirstName",
      "categoriserLastName",
      "category",
    ).contains(
      tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", CategorisationStatus.AWAITING_APPROVAL, "PRISON", "USER", "B"),
    )
    assertThat(sortedList).extracting("offenderNo")
      .doesNotContain("A1234AF", "A1234AG") // "Active" categorisation should be ignored
    // Note that size of list may vary depending on whether feature tests have run, e.g. approving booking id -34
  }

  @Test
  fun testGetApprovedCategorised() {
    val list = repository.getApprovedCategorised("LEI", LocalDate.of(1976, 5, 5))
    val sortedList = list.sortedBy { it.offenderNo }
    assertThat(sortedList)
      .extracting(
        "offenderNo",
        "bookingId",
        "approverFirstName",
        "approverLastName",
        "categoriserFirstName",
        "categoriserLastName",
        "category",
      )
      .contains(tuple("A5576RS", -31L, "API", "USER", "CA", "USER", "A"))
  }

  @Test
  fun testGetApprovedCategorisedNoResults() {
    val list = repository.getApprovedCategorised("MDI", LocalDate.of(2022, 5, 5))
    assertThat(list).hasSize(0)
  }

  @Test
  fun testGetRecategoriseNoResults() {
    val list = repository.getRecategorise("BMI", LocalDate.of(2003, 5, 5))
    assertThat(list).hasSize(0)
  }

  @Test
  fun testGetRecategoriseRemovesNonStandardCategoryResults() {
    val list = repository.getRecategorise("LEI", LocalDate.of(2018, 6, 7))
    assertThat(list)
      .extracting(
        "offenderNo",
        "bookingId",
        "firstName",
        "lastName",
        "category",
        "nextReviewDate",
        "assessmentSeq",
        "assessStatus",
      )
      // -34 pending may or may not be present during the build as the feature tests approve it
      .contains(
        tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", "B", LocalDate.of(2018, 6, 1), 8, "P"),
        tuple("A1234AF", -6L, "ANTHONY", "ANDREWS", "C", LocalDate.of(2018, 6, 7), 2, "A"),
        tuple("A1234AG", -7L, "GILES", "SMITH", "C", LocalDate.of(2018, 6, 7), 1, "A"),
      )
  }

  @Test
  fun testGetRecategoriseStandardFemaleCodeResults() {
    val list = repository.getRecategorise("MDI", LocalDate.of(2019, 6, 7))
    assertThat(list)
      .extracting(
        "offenderNo",
        "bookingId",
        "firstName",
        "lastName",
        "category",
        "nextReviewDate",
        "assessmentSeq",
        "assessStatus",
      )
      .contains(
        tuple("A1180HL", -55L, "JOHN JAMES", "HARRIS JONES", "R", LocalDate.of(2019, 6, 8), 1, "P"),
      )
  }

  @Test
  @Transactional
  fun testGetRecategoriseRemovesNonStandardCatA() {
    val possibly38or39or40or41 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 30))
    assertThat(possibly38or39or40or41).hasSize(2)

    // -40 is a cat A but was a B earlier.
    assertThat(possibly38or39or40or41).extracting("bookingId").doesNotContain(tuple(-40L))
  }

  @Test
  fun testGetRecategoriseIgnoresEarlierPendingOrActive() {
    // booking id -37 has 3 active or pending categorisation records
    val list = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 30))
    assertThat(list)
      .extracting("offenderNo", "bookingId", "firstName", "lastName", "category", "nextReviewDate", "assessmentSeq")
      .containsExactly(
        tuple("A1182BS", -38L, "BEN", "STOKES", "B", LocalDate.of(2019, 6, 8), 3),
        tuple("A1183CW", -39L, "CHRIS", "WOAKES", "B", LocalDate.of(2019, 6, 8), 2),
      )
  }

  @Test
  fun testGetRecategorisePendingLatestAfterCutoff() {
    val list1 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 30))
    assertThat(list1).hasSize(2)

    // -38 and -39 within the cutoff
    assertThat(list1).extracting(
      "bookingId",
      "assessmentSeq",
      "nextReviewDate",
      "assessStatus",
    ).containsExactlyInAnyOrder(
      tuple(-38L, 3, LocalDate.of(2019, 6, 8), "P"),
      tuple(-39L, 2, LocalDate.of(2019, 6, 8), "A"),
    )

    // The latest seq of booking id -38 is now after the cutoff but is pending - so should be selected, -39 is active and after cutoff:
    val list2 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 1))
    assertThat(list2).extracting(
      "bookingId",
      "assessmentSeq",
      "nextReviewDate",
      "assessStatus",
    ).containsExactly(tuple(-38L, 3, LocalDate.of(2019, 6, 8), "P"))
  }

  @Test
  fun testGetRecategoriseReturnsCategorisationIfStandardCategorisationExistsForOffender() {
    val recategorisations = repository.getRecategorise("MUL", LocalDate.of(2019, 6, 9))

    // -16 has a latest assessment of type U, with a previous assessment of type B
    assertThat(recategorisations).extracting(
      "bookingId",
      "assessmentSeq",
      "nextReviewDate",
      "assessStatus",
      "category",
    ).containsExactly(tuple(-16L, 2, LocalDate.of(2019, 6, 8), "A", "U"))
  }

  @Test
  fun testGetRecategoriseFavoursEarlierCatergorisationsForOffenderIfLatestNotValidAssessStatus() {
    val recategorisations = repository.getRecategorise("ZZGHI", LocalDate.of(2019, 6, 9))

    // -26 has a latest assessment with a status of I, which should be ignored in favour of the earlier valid one
    assertThat(recategorisations).extracting(
      "bookingId",
      "assessmentSeq",
      "nextReviewDate",
      "assessStatus",
      "category",
    ).containsExactly(tuple(-26L, 1, LocalDate.of(2019, 6, 8), "A", "B"))
  }

  @Test
  fun testGetALLActiveAssessments() {
    val list = repository.findAssessmentsByOffenderNo(listOf("A1234AF"), "CATEGORY", emptySet(), false, true)
    list.sortedWith(
      Comparator.comparing { obj: AssessmentDto -> obj.offenderNo }
        .thenComparing { obj: AssessmentDto -> obj.bookingId },
    )
    assertThat(list).extracting(
      "offenderNo",
      "bookingId",
      "assessmentCode",
      "assessmentDescription",
      "assessmentDate",
      "assessmentSeq",
      "nextReviewDate",
      "reviewSupLevelType",
      "reviewSupLevelTypeDesc",
      "assessmentCreateLocation",
      "approvalDate",
      "overridedSupLevelType",
      "overridedSupLevelTypeDesc",
      "calcSupLevelType",
      "calcSupLevelTypeDesc",
      "cellSharingAlertFlag",
      "assessStatus",
    ).containsExactlyInAnyOrder(
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 4, 4),
        3,
        LocalDate.of(2016, 8, 8),
        "A",
        "Cat A",
        "LEI",
        LocalDate.of(2016, 7, 7),
        "D",
        "Cat D",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 5, 4),
        2,
        LocalDate.of(2018, 5, 8),
        "B",
        "Cat B",
        "MDI",
        LocalDate.of(2016, 5, 9),
        "B",
        "Cat B",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -6L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2017, 4, 4),
        2,
        LocalDate.of(2018, 6, 7),
        "C",
        "Cat C",
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        "A",
      ),
    )
  }

  @Test
  fun testGetAssessmentsIncludingHistoricalAndInactive() {
    val list = repository.findAssessmentsByOffenderNo(listOf("A1234AF"), "CATEGORY", emptySet(), false, false)
    list.sortedWith(
      Comparator.comparing { obj: AssessmentDto -> obj.offenderNo }
        .thenComparing { obj: AssessmentDto -> obj.bookingId },
    )
    assertThat(list).extracting(
      "offenderNo",
      "bookingId",
      "assessmentCode",
      "assessmentDescription",
      "assessmentDate",
      "assessmentSeq",
      "nextReviewDate",
      "reviewSupLevelType",
      "reviewSupLevelTypeDesc",
      "assessmentCreateLocation",
      "approvalDate",
      "overridedSupLevelType",
      "overridedSupLevelTypeDesc",
      "calcSupLevelType",
      "calcSupLevelTypeDesc",
      "cellSharingAlertFlag",
      "assessStatus",
    ).containsExactlyInAnyOrder(
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 4, 4),
        3,
        LocalDate.of(2016, 8, 8),
        "A",
        "Cat A",
        "LEI",
        LocalDate.of(2016, 7, 7),
        "D",
        "Cat D",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 5, 4),
        2,
        LocalDate.of(2018, 5, 8),
        "B",
        "Cat B",
        "MDI",
        LocalDate.of(2016, 5, 9),
        "B",
        "Cat B",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 3, 4),
        1,
        LocalDate.of(2016, 3, 8),
        "B",
        "Cat B",
        "MDI",
        LocalDate.of(2016, 3, 9),
        "B",
        "Cat B",
        "B",
        "Cat B",
        false,
        "I",
      ),
      tuple(
        "A1234AF",
        -6L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2017, 4, 4),
        2,
        LocalDate.of(2018, 6, 7),
        "C",
        "Cat C",
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        "A",
      ),
    )
  }

  @Test
  fun testGetAssessmentsIncludingCsra() {
    val list = repository.findAssessmentsByOffenderNo(listOf("A1234AF"), null, emptySet(), false, false)
    list.sortedWith(
      Comparator.comparing { obj: AssessmentDto -> obj.offenderNo }
        .thenComparing { obj: AssessmentDto -> obj.bookingId },
    )
    assertThat(list).extracting(
      "offenderNo",
      "bookingId",
      "assessmentCode",
      "assessmentDescription",
      "assessmentDate",
      "assessmentSeq",
      "nextReviewDate",
      "reviewSupLevelType",
      "reviewSupLevelTypeDesc",
      "assessmentCreateLocation",
      "approvalDate",
      "overridedSupLevelType",
      "overridedSupLevelTypeDesc",
      "calcSupLevelType",
      "calcSupLevelTypeDesc",
      "cellSharingAlertFlag",
      "assessStatus",
    ).containsExactlyInAnyOrder(
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 4, 4),
        3,
        LocalDate.of(2016, 8, 8),
        "A",
        "Cat A",
        "LEI",
        LocalDate.of(2016, 7, 7),
        "D",
        "Cat D",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 5, 4),
        2,
        LocalDate.of(2018, 5, 8),
        "B",
        "Cat B",
        "MDI",
        LocalDate.of(2016, 5, 9),
        "B",
        "Cat B",
        "B",
        "Cat B",
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -48L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2016, 3, 4),
        1,
        LocalDate.of(2016, 3, 8),
        "B",
        "Cat B",
        "MDI",
        LocalDate.of(2016, 3, 9),
        "B",
        "Cat B",
        "B",
        "Cat B",
        false,
        "I",
      ),
      tuple(
        "A1234AF",
        -6L,
        "CSR",
        "CSR Rating",
        LocalDate.of(2017, 4, 4),
        1,
        LocalDate.of(2018, 6, 6),
        "STANDARD",
        "Standard",
        null,
        null,
        null,
        null,
        "MED",
        "Medium",
        true,
        "A",
      ),
      tuple(
        "A1234AF",
        -6L,
        "PAROLE",
        "Parole",
        LocalDate.of(2017, 4, 4),
        3,
        LocalDate.of(2018, 6, 8),
        null,
        null,
        null,
        null,
        "HI",
        "High",
        null,
        null,
        false,
        "A",
      ),
      tuple(
        "A1234AF",
        -6L,
        "CATEGORY",
        "Categorisation",
        LocalDate.of(2017, 4, 4),
        2,
        LocalDate.of(2018, 6, 7),
        "C",
        "Cat C",
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        "A",
      ),
    )
  }

  @Test
  @Transactional
  fun testInsertCategory() {
    val uncat = repository.getUncategorised("LEI")
    assertThat(uncat).extracting("offenderNo", "bookingId", "firstName", "lastName", "status")
      .doesNotContain(
        tuple("A1234AE", -5L, "DONALD", "MATTHEWS", CategorisationStatus.AWAITING_APPROVAL),
      )
    val catDetail = CategorisationDetail.builder()
      .bookingId(-5L)
      .category("D")
      .committee("GOV")
      .comment("init cat")
      .nextReviewDate(LocalDate.of(2019, 6, 1))
      .placementAgencyId("BMI")
      .build()
    val responseMap = repository.insertCategory(catDetail, "LEI", -11L, "JDOG")
    assertThat(responseMap).contains(
      Assertions.entry("bookingId", -5L),
      Assertions.entry("sequenceNumber", 3L),
    ) // 2 previous category records for A1234AE
    val list = repository.getUncategorised("LEI")
    assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
      tuple("A1234AE", -5L, "DONALD", "MATTHEWS", CategorisationStatus.AWAITING_APPROVAL),
    )
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -5 AND ASSESSMENT_SEQ = 3")
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("CALC_SUP_LEVEL_TYPE"),
        extractInteger("ASSESSMENT_TYPE_ID"),
        extractInteger("SCORE"),
        extractString("ASSESS_STATUS"),
        extractInteger("ASSESS_STAFF_ID"),
        extractInteger("ASSESSOR_STAFF_ID"),
        extractString("ASSESS_COMMENT_TEXT"),
        extractString("ASSESSMENT_CREATE_LOCATION"),
        extractString("ASSESS_COMMITTE_CODE"),
        extractString("PLACE_AGY_LOC_ID"),
      )
      .contains(tuple(3, "D", -2, 1006, "P", -11, -11, "init cat", "LEI", "GOV", "BMI"))
    assertThat(results[0]["ASSESSMENT_DATE"] as Date?).isToday()
    assertThat(results[0]["CREATION_DATE"] as Date?).isToday()
    assertThat(results[0]["NEXT_REVIEW_DATE"] as Timestamp?).isCloseTo("2019-06-01T00:00:00.000", 1000)
  }

  @Test
  @Transactional
  fun testUpdateCategory() {
    val catDetail = CategorisationUpdateDetail.builder()
      .bookingId(-32L)
      .assessmentSeq(4)
      .category("C")
      .committee("GOV")
      .comment("updated cat")
      .nextReviewDate(LocalDate.of(2019, 12, 1))
      .build()
    repository.updateCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 AND ASSESSMENT_SEQ = 4")
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("CALC_SUP_LEVEL_TYPE"),
        extractString("ASSESS_STATUS"),
        extractString("ASSESS_COMMENT_TEXT"),
        extractString("ASSESS_COMMITTE_CODE"),
      )
      .containsExactly(tuple(4, "C", "P", "updated cat", "GOV"))
    assertThat(results[0]["ASSESSMENT_DATE"] as Date?).isToday()
    assertThat(results[0]["NEXT_REVIEW_DATE"] as Timestamp?).isCloseTo("2019-12-01T00:00:00.000", 1000)
  }

  @Test
  @Transactional
  fun testUpdateCategoryMinimalFields() {
    val catDetail = CategorisationUpdateDetail.builder()
      .bookingId(-37L)
      .assessmentSeq(3)
      .build()
    repository.updateCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -37 AND ASSESSMENT_SEQ = 3")
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("CALC_SUP_LEVEL_TYPE"),
      )
      .containsExactly(tuple(3, "B"))
    assertThat(results[0]["NEXT_REVIEW_DATE"] as Date?).isCloseTo("2016-08-08", 1000L)
  }

  @Test
  @Transactional
  fun testApproveCategoryAllFields() {
    val catDetail = CategoryApprovalDetail.builder()
      .bookingId(-1L)
      .category("C")
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .approvedCategoryComment("My comment")
      .reviewCommitteeCode("REVIEW")
      .committeeCommentText("committeeCommentText")
      .nextReviewDate(LocalDate.of(2019, 7, 24))
      .approvedPlacementAgencyId("BXI")
      .approvedPlacementText("approvedPlacementText")
      .build()
    repository.approveCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)")
    assertThat(results)
      .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
      .contains(
        tuple(6, "I"),
      )
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("REVIEW_SUP_LEVEL_TYPE"),
        extractString("REVIEW_COMMITTE_CODE"),
        extractString("EVALUATION_RESULT_CODE"),
        extractString("ASSESS_STATUS"),
        extractString("REVIEW_SUP_LEVEL_TEXT"),
        extractString("COMMITTE_COMMENT_TEXT"),
        extractString("REVIEW_PLACE_AGY_LOC_ID"),
        extractString("REVIEW_PLACEMENT_TEXT"),
      )
      .contains(
        tuple(8, "C", "REVIEW", "APP", "A", "My comment", "committeeCommentText", "BXI", "approvedPlacementText"),
      )
    assertThat(results[0]["EVALUATION_DATE"] as Timestamp?).isNull()
    assertThat(results[1]["EVALUATION_DATE"] as Timestamp?).isCloseTo("2019-02-27T00:00:00.000", 1000)
    assertThat(results[1]["NEXT_REVIEW_DATE"] as Timestamp?).isCloseTo("2019-07-24T00:00:00.000", 1000)
  }

  @Test
  @Transactional
  fun testApproveCategoryHandlesMultipleActiveCategorisations() {
    val catDetail = CategoryApprovalDetail.builder()
      .bookingId(-32L)
      .category("C")
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .approvedCategoryComment("My comment")
      .reviewCommitteeCode("REVIEW")
      .committeeCommentText("committeeCommentText")
      .nextReviewDate(LocalDate.of(2019, 7, 24))
      .build()

    // 4 categorisation records with status Inactive, Active, Inactive, Pending
    repository.approveCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 order by ASSESSMENT_SEQ")

    // after making the pending cat active should make any earlier categorisation inactive (regardless of order)
    assertThat(results)
      .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
      .contains(
        tuple(1, "I"),
        tuple(2, "I"),
        tuple(3, "I"),
        tuple(4, "A"),
      )
    assertThat(results[3]["EVALUATION_DATE"] as Timestamp?).isCloseTo("2019-02-27", 1000L)
  }

  @Test
  @Transactional
  fun testApproveCategoryHandlesNoPreviousCategorisation() {
    val catDetail = CategoryApprovalDetail.builder()
      .bookingId(-36L)
      .category("C")
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .approvedCategoryComment("My comment")
      .reviewCommitteeCode("REVIEW")
      .committeeCommentText("committeeCommentText")
      .nextReviewDate(LocalDate.of(2019, 7, 24))
      .build()

    // 1 pending cateorisation record
    repository.approveCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -36 order by ASSESSMENT_SEQ")

    // confirm single categorisation is active
    assertThat(results)
      .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
      .contains(tuple(1, "A"))
  }

  @Test
  @Transactional
  fun testApproveCategoryMinimalFields() {
    val catDetail = CategoryApprovalDetail.builder()
      .bookingId(-1L)
      .category("C")
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .reviewCommitteeCode("REVIEW")
      .build()
    repository.approveCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)")
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("REVIEW_SUP_LEVEL_TYPE"),
        extractString("REVIEW_COMMITTE_CODE"),
        extractString("EVALUATION_RESULT_CODE"),
        extractString("ASSESS_STATUS"),
        extractString("REVIEW_SUP_LEVEL_TEXT"),
        extractString("COMMITTE_COMMENT_TEXT"),
        extractString("REVIEW_PLACE_AGY_LOC_ID"),
        extractString("REVIEW_PLACEMENT_TEXT"),
      )
      .contains(
        tuple(8, "C", "REVIEW", "APP", "A", null, null, null, null),
      )
    assertThat(results[1]["NEXT_REVIEW_DATE"] as Timestamp?).isCloseTo("2018-06-01T00:00:00.000", 1000)
  }

  @Test
  @Transactional
  fun testApproveCategoryUsingSeq() {
    val catDetail = CategoryApprovalDetail.builder()
      .bookingId(-1L)
      .assessmentSeq(8)
      .category("C")
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .approvedCategoryComment("My comment")
      .reviewCommitteeCode("REVIEW")
      .committeeCommentText("committeeCommentText")
      .nextReviewDate(LocalDate.of(2019, 7, 24))
      .build()
    repository.approveCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)")
    assertThat(results)
      .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
      .contains(
        tuple(6, "I"),
      )
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("REVIEW_SUP_LEVEL_TYPE"),
        extractString("REVIEW_COMMITTE_CODE"),
        extractString("EVALUATION_RESULT_CODE"),
        extractString("ASSESS_STATUS"),
        extractString("REVIEW_SUP_LEVEL_TEXT"),
        extractString("COMMITTE_COMMENT_TEXT"),
      )
      .contains(
        tuple(8, "C", "REVIEW", "APP", "A", "My comment", "committeeCommentText"),
      )
    assertThat(results[0]["EVALUATION_DATE"] as Timestamp?).isNull()
    assertThat(results[1]["EVALUATION_DATE"] as Timestamp?).isCloseTo("2019-02-27T00:00:00.000", 1000)
    assertThat(results[1]["NEXT_REVIEW_DATE"] as Timestamp?).isCloseTo("2019-07-24T00:00:00.000", 1000)
  }

  @Test
  @Transactional
  fun testRejectCategory() {
    val catDetail = CategoryRejectionDetail.builder()
      .bookingId(-32L)
      .assessmentSeq(4)
      .evaluationDate(LocalDate.of(2019, 2, 27))
      .reviewCommitteeCode("REVIEW")
      .committeeCommentText("committeeCommentText")
      .build()
    repository.rejectCategory(catDetail)
    val results =
      jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 AND ASSESSMENT_SEQ = 4")
    assertThat(results)
      .extracting(
        extractInteger("ASSESSMENT_SEQ"),
        extractString("EVALUATION_RESULT_CODE"),
        extractString("REVIEW_COMMITTE_CODE"),
        extractString("COMMITTE_COMMENT_TEXT"),
      )
      .containsExactly(tuple(4, "REJ", "REVIEW", "committeeCommentText"))
    assertThat(results[0]["EVALUATION_DATE"] as Date?).isCloseTo("2019-02-27", 1000)
  }

  @Test
  @Transactional
  fun testRejectCategoryNotFound() {
    val catDetail = CategoryRejectionDetail.builder()
      .bookingId(-32L)
      .assessmentSeq(99)
      .build()
    assertThatThrownBy { repository.rejectCategory(catDetail) }.isInstanceOf(
      HttpClientErrorException::class.java,
    )
  }

  @Test
  @Transactional
  fun testUpdateCategoryNextReviewDateForUnknownOffender() {
    val newNextReviewDate = LocalDate.of(2019, 2, 27)
    try {
      repository.updateActiveCategoryNextReviewDate(-15655L, newNextReviewDate)
      Assertions.fail<Any>("Should have thrown an EntityNotFoundException")
    } catch (e: EntityNotFoundException) {
      assertThat(e.message)
        .isEqualTo("Unable to update next review date, could not find latest, active categorisation for booking id -15655, result count = 0")
    }
  }

  @Test
  fun testThatActiveOffendersAreReturnedMatchingNumberAndCaseLoad() {
    val offenders =
      repository.getBasicInmateDetailsForOffenders(setOf("A1234AI", "A1183SH"), false, setOf("LEI"), true)
    assertThat(offenders).hasSize(1)
    assertThat(offenders).extracting(
      "offenderNo",
      "bookingId",
      "agencyId",
      "firstName",
      "lastName",
      "middleName",
      "dateOfBirth",
      "assignedLivingUnitId",
      "assignedLivingUnitDesc",
    ).contains(
      tuple(
        "A1234AI",
        -9L,
        "LEI",
        "CHESTER",
        "THOMPSON",
        "JAMES",
        LocalDate.parse("1970-03-01"),
        -7L,
        "LEI-A-1-5",
      ),
    )
  }

  @Test
  fun testAccessToAllData_whenTrue() {
    val offenders = repository.getBasicInmateDetailsForOffenders(setOf("A1234AI"), true, emptySet(), false)
    assertThat(offenders).containsExactly(
      InmateBasicDetails(
        -9L,
        "A00119",
        "A1234AI",
        "CHESTER",
        "JAMES",
        "THOMPSON",
        "LEI",
        -7L,
        "LEI-A-1-5",
        LocalDate.parse("1970-03-01"),
      ),
    )
  }

  @Test
  fun testAccessToAllData_whenFalse() {
    val offenders = repository.getBasicInmateDetailsForOffenders(setOf("A1234AI"), false, setOf("HLI"), false)
    assertThat(offenders).isEmpty()
  }

  @Test
  fun testAccessToAllData_WithActiveOnlyTrue() {
    // Offender without an active booking
    val offenders = repository.getBasicInmateDetailsForOffenders(setOf("Z0020ZZ"), true, emptySet(), true)
    assertThat(offenders).isEmpty()
  }

  @Test
  fun testAccessToAllData_WithActiveOnlyFalse() {
    // Offender without an active booking
    val offenders = repository.getBasicInmateDetailsForOffenders(setOf("Z0020ZZ"), true, emptySet(), false)
    assertThat(offenders).hasSize(1)
  }

  @Test
  fun findPhysicalAttributes() {
    val physicalAttributes = repository.findPhysicalAttributes(-1)
    assertThat(physicalAttributes.orElseThrow()).isEqualTo(
      PhysicalAttributes.builder()
        .gender("Male")
        .sexCode("M")
        .raceCode("W1")
        .ethnicity("White: British")
        .heightFeet(5)
        .heightInches(6)
        .heightMetres(null)
        .heightCentimetres(168)
        .weightPounds(165)
        .weightKilograms(75)
        .build(),
    )
  }

  @get:Test
  val personalCareNeeds: Unit
    get() {
      val info = repository.findPersonalCareNeeds(-1, setOf("DISAB", "MATSTAT"))
      assertThat(info).containsExactly(
        PersonalCareNeed.builder().personalCareNeedId(-201L).problemType("DISAB").problemCode("ND").problemStatus("ON")
          .problemDescription("No Disability").commentText("Some Description Text 1")
          .startDate(LocalDate.parse("2010-06-21")).build(),
        PersonalCareNeed.builder().personalCareNeedId(-206L).problemType("MATSTAT").problemCode("ACCU9")
          .problemStatus("ON")
          .problemDescription("Preg, acc under 9mths").commentText("P1")
          .startDate(LocalDate.parse("2010-06-21")).build(),
      )
    }

  @get:Test
  val personalCareNeedsForOffenderNos: Unit
    get() {
      val info = repository.findPersonalCareNeeds(
        listOf("A1234AA", "A1234AB", "A1234AC", "A1234AD"),
        setOf("DISAB", "MATSTAT"),
      )
      assertThat(info).containsExactly(
        PersonalCareNeed.builder().personalCareNeedId(-206L).problemType("MATSTAT").problemCode("ACCU9")
          .problemStatus("ON")
          .problemDescription("Preg, acc under 9mths").commentText("P1")
          .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
        PersonalCareNeed.builder().personalCareNeedId(-201L).problemType("DISAB").problemCode("ND").problemStatus("ON")
          .problemDescription("No Disability").commentText("Some Description Text 1")
          .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
        PersonalCareNeed.builder().personalCareNeedId(-202L).problemType("DISAB").problemCode("ND").problemStatus("ON")
          .problemDescription("No Disability").commentText(null)
          .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AB").build(),
        PersonalCareNeed.builder().personalCareNeedId(-203L).problemType("DISAB").problemCode("ND").problemStatus("ON")
          .problemDescription("No Disability").commentText(null)
          .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AC").build(),
        PersonalCareNeed.builder().personalCareNeedId(-204L).problemType("DISAB").problemCode("ND").problemStatus("ON")
          .problemDescription("No Disability").commentText("Some Description Text 2")
          .startDate(LocalDate.parse("2010-06-24")).endDate(null).offenderNo("A1234AD").build(),
      )
    }

  @get:Test
  val reasonableAdjustment: Unit
    get() {
      val expectedInfo = listOf(
        ReasonableAdjustment.builder()
          .treatmentCode("COMP SOFT")
          .treatmentDescription("Computer software")
          .commentText("EFGH")
          .startDate(LocalDate.of(2010, 6, 21))
          .agencyId("LEI")
          .agencyDescription("Leeds")
          .personalCareNeedId(-206L)
          .build(),
        ReasonableAdjustment.builder()
          .treatmentCode("WHEELCHR_ACC")
          .treatmentDescription("Wheelchair accessibility")
          .commentText("Some Comment Text")
          .startDate(LocalDate.of(2010, 6, 21))
          .personalCareNeedId(-206L)
          .build(),
      )
      val treatmentCodes = listOf("WHEELCHR_ACC", "COMP SOFT")
      val info = repository.findReasonableAdjustments(-1, treatmentCodes)
      assertThat(info).isEqualTo(expectedInfo)
    }

  @get:Test
  val offenderDetailsContainsReceptionDate: Unit
    get() {
      val offender = repository.findOffender("A1234AA")
      assertThat(offender.orElseThrow().receptionDate).isEqualTo(LocalDate.now())
    }

  @Test
  fun testSearchForInmatesByWingLocation() {
    val expectedDob = LocalDate.of(1990, Month.DECEMBER, 30)
    val expectedAge = Period.between(expectedDob, LocalDate.now()).years
    val expectedInfo = listOf(
      OffenderBooking.builder()
        .bookingId(-40L)
        .bookingNo("SAME_NO")
        .offenderNo("A1184JR")
        .firstName("JOE")
        .lastName("ROOT")
        .dateOfBirth(expectedDob)
        .age(expectedAge)
        .agencyId("SYI")
        .assignedLivingUnitId(-204L)
        .build(),
    )
    val results = repository.findInmatesByLocation(-200L, "SYI", "lastName,firstName,offenderNo", Order.DESC, 0, 10)
    assertThat(results.items).hasSize(1)
    assertThat(results.items[0]).isEqualTo(
      expectedInfo[0],
    )
  }

  @Test
  fun testSearchForInmatesByCellLocation() {
    val expectedDob = LocalDate.of(1990, Month.DECEMBER, 30)
    val expectedAge = Period.between(expectedDob, LocalDate.now()).years
    val expectedInfo = listOf(
      OffenderBooking.builder()
        .bookingId(-40L)
        .bookingNo("SAME_NO")
        .offenderNo("A1184JR")
        .firstName("JOE")
        .lastName("ROOT")
        .dateOfBirth(expectedDob)
        .age(expectedAge)
        .agencyId("SYI")
        .assignedLivingUnitId(-204L)
        .build(),
    )
    val results = repository.findInmatesByLocation(-204L, "SYI", "lastName,firstName,offenderNo", Order.DESC, 0, 10)
    assertThat(results.items).hasSize(1)
    assertThat(results.items[0]).isEqualTo(
      expectedInfo[0],
    )
  }

  /** */
  private fun criteriaForOffenderNo(offenderNos: List<String>): PrisonerDetailSearchCriteria =
    PrisonerDetailSearchCriteria.builder()
      .offenderNos(offenderNos)
      .build()

  private fun criteriaForPNCNumber(pncNumber: String): PrisonerDetailSearchCriteria =
    PrisonerDetailSearchCriteria.builder()
      .pncNumber(pncNumber)
      .build()

  fun criteriaForCRONumber(croNumber: String): PrisonerDetailSearchCriteria {
    return PrisonerDetailSearchCriteria.builder()
      .croNumber(croNumber)
      .build()
  }

  private fun criteriaForPersonalAttrs(
    offenderNos: List<String>?,
    lastName: String?,
    firstName: String?,
  ): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .offenderNos(offenderNos)
    .lastName(lastName)
    .firstName(firstName)
    .build()

  private fun criteriaForPartialPersonalAttrs(
    offenderNos: List<String>?,
    lastName: String?,
    firstName: String?,
  ): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .offenderNos(offenderNos)
    .lastName(lastName)
    .firstName(firstName)
    .partialNameMatch(true)
    .build()

  private fun criteriaForAnyPersonalAttrs(
    offenderNos: List<String>?,
    lastName: String,
    firstName: String?,
  ): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .offenderNos(offenderNos)
    .lastName(lastName)
    .firstName(firstName)
    .anyMatch(true)
    .build()

  private fun criteriaForDOBRange(
    dob: LocalDate?,
    dobFrom: LocalDate?,
    dobTo: LocalDate?,
  ): PrisonerDetailSearchCriteria = PrisonerDetailSearchCriteria.builder()
    .dob(dob)
    .dobFrom(dobFrom)
    .dobTo(dobTo)
    .maxYearsRange(10)
    .build()

  private fun criteriaForLocationFilter(location: String): PrisonerDetailSearchCriteria =
    PrisonerDetailSearchCriteria.builder()
      .location(location)
      .build()

  private fun criteriaForGenderFilter(gender: String): PrisonerDetailSearchCriteria =
    PrisonerDetailSearchCriteria.builder()
      .gender(gender)
      .build()

  private fun addDOBRangeCriteria(
    criteria: PrisonerDetailSearchCriteria,
    dob: LocalDate,
    dobFrom: LocalDate?,
    dobTo: LocalDate?,
  ): PrisonerDetailSearchCriteria = criteria.withDob(dob).withDobFrom(dobFrom).withDobTo(dobTo).withMaxYearsRange(10)

  private fun buildQuery(criteria: PrisonerDetailSearchCriteria): String? =
    repository.generateFindOffendersQuery(criteria)

  private fun findOffender(query: String?): PrisonerDetail {
    val page = repository.findOffenders(query, PageRequest())
    assertThat(page.items).hasSize(1)
    return page.items[0]
  }

  private fun findOffenderWithAliases(query: String?): PrisonerDetail {
    val page = repository.findOffendersWithAliases(query, PageRequest())
    assertThat(page.items).hasSize(1)
    return page.items[0]
  }

  private fun findOffenders(query: String?): List<PrisonerDetail> {
    val page = repository.findOffenders(query, PageRequest())
    return page.items
  }

  private fun findOffendersWithAliases(query: String?): List<PrisonerDetail> {
    val page = repository.findOffendersWithAliases(query, PageRequest())
    return page.items
  }

  private fun findOffendersWithAliasesFullResults(query: String?): List<PrisonerDetail> {
    val page = repository.findOffendersWithAliases(query, PageRequest(0L, 1000L))
    return page.items
  }
}
