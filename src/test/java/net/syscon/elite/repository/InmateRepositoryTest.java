package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static net.syscon.elite.api.support.CategorisationStatus.AWAITING_APPROVAL;
import static net.syscon.elite.api.support.CategorisationStatus.UNCATEGORISED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class InmateRepositoryTest {

    @Autowired
    private InmateRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testFindAllImates() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = new HashSet<String>(Arrays.asList("LEI", "BXI"));
        final var foundInmates = repository.findAllInmates(caseloads, "WING", "", pageRequest);

        assertThat(foundInmates.getItems()).isNotEmpty();
    }

    @Test
    public void testSearchForOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = new HashSet<String>(Arrays.asList("LEI", "BXI"));
        final var alertFilter = Arrays.asList("XA", "HC");

        final var foundInmates = repository.searchForOffenderBookings(caseloads, "A1234AA", "A", "A", "LEI",
                alertFilter, "WING", pageRequest);

        final var results = foundInmates.getItems();
        assertThat(results).asList().hasSize(1);
        assertThat(results).asList().extracting("bookingId", "offenderNo", "dateOfBirth", "assignedLivingUnitDesc").contains(
                Tuple.tuple(-1L, "A1234AA", LocalDate.of(1969, Month.DECEMBER, 30), "A-1-1"));
    }

    @Test
    public void testGetOffender() {
        final var inmate = repository.findInmate(-1L);
        assertThat(inmate).isPresent();
        final var result = inmate.get();
        assertThat(result.getBookingNo()).isEqualTo("A00111");
        assertThat(result.getBookingId()).isEqualTo(-1L);
        assertThat(result.getOffenderNo()).isEqualTo("A1234AA");
        assertThat(result.getFirstName()).isEqualTo("ARTHUR");
        assertThat(result.getMiddleName()).isEqualTo("BORIS");
        assertThat(result.getLastName()).isEqualTo("ANDERSON");
        assertThat(result.getAgencyId()).isEqualTo("LEI");
        assertThat(result.getAssignedLivingUnitId()).isEqualTo(-3L);
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1969, 12, 30));
        assertThat(result.getFacialImageId()).isEqualTo(-1L);
        assertThat(result.getLanguage()).isEqualTo("Polish");
    }

    @Test
    public void testGetBasicOffenderDetails() {
        final var inmate = repository.getBasicInmateDetail(-1L);
        assertThat(inmate).isPresent();
    }

    @Test
    public void testfindOffendersWithValidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "A1234AP";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testfindOffendersWithLocationFilterIN() {

        final var query = buildQuery(criteriaForLocationFilter("IN"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(42);
    }

    @Test
    public void testfindOffendersWithLocationFilterOut() {

        final var query = buildQuery(criteriaForLocationFilter("OUT"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(5);
    }

    @Test
    public void testfindOffendersWithLocationFilterALL() {

        final var query = buildQuery(criteriaForLocationFilter("ALL"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(47);
    }

    @Test
    public void testfindOffendersWithGenderFilterMale() {

        final var query = buildQuery(criteriaForGenderFilter("M"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(45);
    }

    @Test
    public void testfindOffendersWithGenderFilterALL() {

        final var query = buildQuery(criteriaForGenderFilter("ALL"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(47);
    }

    @Test
    public void testfindOffendersWithInvalidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "X9999XX";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        assertThat(findOffenders(query)).isEmpty();
    }

    @Test
    public void testfindOffendersWithValidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "14/12345F";

        final var query = buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffendersWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));
    }

    @Test
    public void testfindOffendersWithValidCRONumberOnly() {
        final var TEST_CRO_NUMBER = "CRO112233";

        final var query = buildQuery(criteriaForCRONumber(TEST_CRO_NUMBER));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    @Test
    public void testFindOffendersWithLastName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null));

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithLastNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithFirstName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffendersWithFirstNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffendersWithFirstNameAndLastName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "TRUMP", "DONALD"));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AH");
    }

    @Test
    public void testFindOffendersWithDateOfBirth() {
        final var criteria = criteriaForDOBRange(
                LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("Z0021ZZ");
    }

    @Test
    public void testFindOffendersWithDateOfBirthRange() {
        final var criteria = criteriaForDOBRange(
                null, LocalDate.of(1960, 1, 1), LocalDate.of(1969, 12, 31));

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(9);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo)
                .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA");
    }

    @Test
    public void testFindOffendersWithLastNameAndDateOfBirth() {
        var criteria = criteriaForPersonalAttrs(null, "QUIMBY", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null);

        final var query = buildQuery(criteria);

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1178RS");
    }

    @Test
    public void testFindOffendersWithPartialLastName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, "ST", null);

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffendersWithPartialFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0017ZZ", "A1180MA", "A1181MV");
    }

    @Test
    public void testFindOffendersWithPartialLastNameAndFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA");

        final var query = buildQuery(criteria);

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1179MT");
    }

    @Test
    public void testFindOffendersWithLastNameOrFirstName() {
        final var criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS");

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffendersWithLastNameOrDateOfBirth() {
        var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0021ZZ", "A1183CW");
    }

    /********************/
    @Test
    public void testfindOffenderAliasesWithValidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "A1234AP";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testfindOffenderAliasesWithInvalidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "X9999XX";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        assertThat(findOffendersWithAliases(query)).isEmpty();
    }

    @Test
    public void testfindOffenderAliasesWithValidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "14/12345F";

        final var query = buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffenderAliasesWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));
    }

    @Test
    public void testfindOffenderAliasesWithValidCRONumberOnly() {
        final var TEST_CRO_NUMBER = "CRO112233";

        final var query = buildQuery(criteriaForCRONumber(TEST_CRO_NUMBER));

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    @Test
    public void testFindOffenderAliasesWithLastName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithFirstName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffenderAliasesWithFirstNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffenderAliasesWithFirstNameAndLastName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "TRUMP", "DONALD"));

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AH");
    }

    @Test
    public void testFindOffenderAliasesWithDateOfBirth() {
        final var criteria = criteriaForDOBRange(
                LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("Z0021ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithDateOfBirthRange() {
        final var criteria = criteriaForDOBRange(
                null, LocalDate.of(1960, 1, 1), LocalDate.of(1969, 12, 31));

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(9);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo)
                .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameAndDateOfBirth() {
        var criteria = criteriaForPersonalAttrs(null, "QUIMBY", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null);

        final var query = buildQuery(criteria);

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1178RS");
    }

    @Test
    public void testFindOffenderAliasesWithPartialLastName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, "ST", null);

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffenderAliasesWithPartialFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0017ZZ", "A1180MA", "A1181MV");
    }

    @Test
    public void testFindOffenderAliasesWithPartialLastNameAndFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA");

        final var query = buildQuery(criteria);

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1179MT");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameOrFirstName() {
        final var criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS");

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameOrDateOfBirth() {
        var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0021ZZ", "A1183CW");
    }

    @Test
    public void testGetUncategorisedGeneral() {
        final var list = repository.getUncategorised("LEI");

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).asList().extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
                Tuple.tuple("A1234AB", -2L, "GILLIAN", "ANDERSON", UNCATEGORISED),
                Tuple.tuple("A1234AB", -2L, "GILLIAN", "ANDERSON", UNCATEGORISED),
                Tuple.tuple("A1176RS", -32L, "FRED", "JAMES", UNCATEGORISED));

        assertThat(list).asList().extracting("offenderNo", "bookingId", "firstName", "lastName", "status",
                "categoriserFirstName", "categoriserLastName", "category").contains(
                Tuple.tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", AWAITING_APPROVAL, "Elite2", "User", "B"));

        assertThat(list).asList().extracting("offenderNo").doesNotContain("A1234AG");  // "Active" categorisation should be ignored
        // Note that size of list may vary depending on whether feature tests have run, e.g. approving booking id -34
    }


    @Test
    public void testGetApprovedCategorised() {
        final var list = repository.getApprovedCategorised("LEI", LocalDate.of(1976, 5, 5));

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).asList().extracting("offenderNo", "bookingId", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category").contains(
                Tuple.tuple("A5576RS", -31L, "API", "User", "CA", "User", "C"));
    }

    @Test
    public void testGetApprovedCategorisedNoResults() {
        final var list = repository.getApprovedCategorised("MDI", LocalDate.of(2022, 5, 5));
        assertThat(list).hasSize(0);
    }

    @Test
    public void testGetAllAssessments() {
        final var list = repository.findAssessmentsByOffenderNo(
                Arrays.asList("A1234AF"), "CATEGORY", Collections.emptySet(),false);

        list.sort(Comparator.comparing(AssessmentDto::getOffenderNo).thenComparing(AssessmentDto::getBookingId));
        assertThat(list).asList().extracting("offenderNo", "bookingId", "assessmentCode",
                "assessmentDescription", "assessmentDate", "assessmentSeq", "nextReviewDate",
                "reviewSupLevelType", "reviewSupLevelTypeDesc", "overridedSupLevelType", "overridedSupLevelTypeDesc",
                "calcSupLevelType", "calcSupLevelTypeDesc", "cellSharingAlertFlag", "assessStatus"

        ).containsExactlyInAnyOrder(
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 4, 4), 1, LocalDate.of(2016, 6, 8), "A", "Cat A", "D", "Cat D", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -6L, "CATEGORY", "Categorisation", LocalDate.of(2017, 4, 4), 2, LocalDate.of(2018, 6, 7), "C", "Cat C", null, null, null, null, false, "A")
        );
    }

    @Test
    @Transactional
    public void testInsertCategory() {
        final var uncat = repository.getUncategorised("LEI");

        assertThat(uncat).asList().extracting("offenderNo", "bookingId", "firstName", "lastName", "status").doesNotContain(
                Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", AWAITING_APPROVAL));

        final var catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").build();

        repository.insertCategory(catDetail, "LEI", -11L, "JDOG");

        final var list = repository.getUncategorised("LEI");

        assertThat(list).asList().extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
                Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", AWAITING_APPROVAL));
    }

    @Test
    @Transactional
    public void testApproveCategory() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-1L)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .reviewSupLevelText("My comment").build();

        repository.approveCategory(catDetail, UserDetail.builder().staffId(-10L).username("KDOG").build());

        final var list = repository.findAssessments(Arrays.asList(-1L), "CATEGORY", Collections.emptySet());

        assertThat(list).asList().extracting("reviewSupLevelType", "assessStatus").contains(Tuple.tuple("C", "A"));

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ = 8");
        assertThat(results).asList().hasSize(1);
        assertThat(results.get(0).get("REVIEW_SUP_LEVEL_TYPE")).isEqualTo("C");
        assertThat(results.get(0).get("REVIEW_COMMITTE_CODE")).isEqualTo("REVIEW");
        assertThat(results.get(0).get("EVALUATION_RESULT_CODE")).isEqualTo("APP");
        assertThat(results.get(0).get("ASSESS_STATUS")).isEqualTo("A");
        assertThat((Timestamp) results.get(0).get("EVALUATION_DATE")).isCloseTo("2019-02-27T00:00:00.000", 1000);
        assertThat(results.get(0).get("REVIEW_SUP_LEVEL_TEXT")).isEqualTo("My comment");
        assertThat(results.get(0).get("MODIFY_USER_ID")).isEqualTo("KDOG");
        assertThat((Date) results.get(0).get("MODIFY_DATETIME")).isToday();
    }

    @Test
    public void testThatActiveOffendersAreReturnedMatchingNumberAndCaseLoad() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI", "A1183SH"),false, Set.of("LEI"));
        assertThat(offenders).hasSize(1);
        assertThat(offenders).asList().extracting("offenderNo", "bookingId", "agencyId", "firstName", "lastName", "middleName" , "dateOfBirth", "assignedLivingUnitId").contains(
                Tuple.tuple("A1234AI", -9L, "LEI", "CHESTER", "THOMPSON", "JAMES", LocalDate.parse("1970-03-01"), -7L)
        );
    }

    /*****************************************************************************************/

    private PrisonerDetailSearchCriteria criteriaForOffenderNo(final String offenderNo) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPNCNumber(final String pncNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .pncNumber(pncNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForCRONumber(final String croNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .croNumber(croNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPersonalAttrs(final String offenderNo, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPartialPersonalAttrs(final String offenderNo, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .partialNameMatch(true)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForAnyPersonalAttrs(final String offenderNo, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .anyMatch(true)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForDOBRange(final LocalDate dob, final LocalDate dobFrom, final LocalDate dobTo) {
        return PrisonerDetailSearchCriteria.builder()
                .dob(dob)
                .dobFrom(dobFrom)
                .dobTo(dobTo)
                .maxYearsRange(10)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForLocationFilter(final String location) {
        return PrisonerDetailSearchCriteria.builder()
                .latestLocationId(location)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForGenderFilter(final String gender) {
        return PrisonerDetailSearchCriteria.builder()
                .sexCode(gender)
                .build();
    }

    private PrisonerDetailSearchCriteria addDOBRangeCriteria(final PrisonerDetailSearchCriteria criteria,
                                                             final LocalDate dob, final LocalDate dobFrom, final LocalDate dobTo) {
        return criteria.withDob(dob).withDobFrom(dobFrom).withDobTo(dobTo).withMaxYearsRange(10);
    }

    private String buildQuery(final PrisonerDetailSearchCriteria criteria) {
        return InmateRepository.generateFindOffendersQuery(criteria);
    }

    private PrisonerDetail findOffender(final String query) {
        final var page = repository.findOffenders(query, new PageRequest());

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }

    private PrisonerDetail findOffenderWithAliases(final String query) {
        final var page = repository.findOffendersWithAliases(query, new PageRequest());

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }

    private List<PrisonerDetail> findOffenders(final String query) {
        final var page = repository.findOffenders(query, new PageRequest());

        return page.getItems();
    }

    private List<PrisonerDetail> findOffendersWithAliases(final String query) {
        final var page = repository.findOffendersWithAliases(query, new PageRequest());

        return page.getItems();
    }

    private List<PrisonerDetail> findOffendersWithAliasesFullResults(final String query) {
        final var page = repository.findOffendersWithAliases(query, new PageRequest(0L, 1000L));

        return page.getItems();
    }

}
