package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.Language;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;

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
        final var caseloads = Set.of("LEI", "BXI");
        final var foundInmates = repository.findAllInmates(caseloads, "WING", "", pageRequest);

        assertThat(foundInmates.getItems()).isNotEmpty();
    }

    @Test
    public void testFindSpecificInmatesAtLocation() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");
        final var foundInmates = repository.findAllInmates(caseloads, "WING", "", pageRequest);

        assertThat(foundInmates.getItems()).isNotEmpty();
        assertThat(foundInmates.getItems()).extracting(OffenderBooking::getLastName).contains("FOX", "DUCK", "BATES");
    }

    @Test
    public void testSearchForOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI", "BXI");
        final var alertFilter = List.of("XA", "HC");

        final var foundInmates = repository.searchForOffenderBookings(caseloads, "A1234AA", "A", "A", "LEI",
                alertFilter, "All", "WING", pageRequest);

        final var results = foundInmates.getItems();
        assertThat(results).hasSize(1);
        assertThat(results).extracting("bookingId", "offenderNo", "dateOfBirth", "assignedLivingUnitDesc").contains(
                Tuple.tuple(-1L, "A1234AA", LocalDate.of(1969, Month.DECEMBER, 30), "A-1-1"));
    }

    @Test
    public void testSearchForConvictedOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(caseloads, null, null, null, "LEI",
                null, "Convicted", "WING", pageRequest);

        final var results = foundInmates.getItems();

        assertThat(results).hasSize(7);
        assertThat(results).extracting("convictedStatus").containsOnlyElementsOf(List.of("Convicted"));
        assertThat(results).extracting("imprisonmentStatus").containsOnlyElementsOf(List.of("SENT"));
    }

    @Test
    public void testSearchForRemandOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(caseloads, null, null, null, "LEI",
                null, "Remand", "WING", pageRequest);

        final var results = foundInmates.getItems();

        assertThat(results).hasSize(3);
        assertThat(results).extracting("convictedStatus").containsOnlyElementsOf(List.of("Remand"));
        assertThat(results).extracting("imprisonmentStatus").containsOnlyElementsOf(List.of("TRL"));
    }

    @Test
    public void testSearchForAllConvictedStatus() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(caseloads, null, null, null, "LEI",
                null, "All", "WING", pageRequest);

        final var results = foundInmates.getItems();

        assertThat(results).hasSize(10);
        assertThat(results).extracting("convictedStatus").containsAll(List.of("Convicted", "Remand"));
        assertThat(results).extracting("imprisonmentStatus").containsAll(List.of("TRL", "SENT"));
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
    }

    @Test
    public void testGetBasicOffenderDetails() {
        final var inmate = repository.getBasicInmateDetail(-1L);
        assertThat(inmate).isPresent();
    }

    @Test
    public void testFindOffendersWithValidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "A1234AP";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testFindOffendersWithLocationFilterIN() {

        final var query = buildQuery(criteriaForLocationFilter("IN"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(47);
    }

    @Test
    public void testFindOffendersWithLocationFilterOut() {

        final var query = buildQuery(criteriaForLocationFilter("OUT"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(5);
    }

    @Test
    public void testFindOffendersWithLocationFilterALL() {

        final var query = buildQuery(criteriaForLocationFilter("ALL"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(52);
    }

    @Test
    public void testFindOffendersWithGenderFilterMale() {

        final var query = buildQuery(criteriaForGenderFilter("M"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(49);
    }

    @Test
    public void testFindOffendersWithGenderFilterALL() {

        final var query = buildQuery(criteriaForGenderFilter("ALL"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(52);
    }

    @Test
    public void testFindOffendersWithInvalidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "X9999XX";

        final var query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        assertThat(findOffenders(query)).isEmpty();
    }

    @Test
    public void testFindOffendersWithValidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "14/12345F";

        final var query = buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindOffendersWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));
    }

    @Test
    public void testFindOffendersWithValidCRONumberOnly() {
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

        assertThat(offenders).hasSize(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithLastNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        final var offenders = findOffenders(query);

        assertThat(offenders).hasSize(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithFirstName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        final var offenders = findOffenders(query);

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffendersWithFirstNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        final var offenders = findOffenders(query);

        assertThat(offenders).hasSize(2);
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

        assertThat(offenders).hasSize(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffendersWithPartialFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders).hasSize(3);
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

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffendersWithLastNameOrDateOfBirth() {
        var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offenders = findOffenders(query);

        assertThat(offenders).hasSize(2);
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

        assertThat(offenders).hasSize(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders).hasSize(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithFirstName() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).containsOnly("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffenderAliasesWithFirstNameLowerCase() {
        final var query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).containsOnly("A1234AJ", "A1234AL");
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

        assertThat(offenders).hasSize(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffenderAliasesWithPartialFirstName() {
        final var criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders).hasSize(3);
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

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameOrDateOfBirth() {
        var criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        final var query = buildQuery(criteria);

        final var offenders = findOffendersWithAliases(query);

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0021ZZ", "A1183CW");
    }

    @Test
    public void testGetUncategorisedGeneral() {
        final var list = repository.getUncategorised("LEI");

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status", "category").contains(
                Tuple.tuple("A1234AB", -2L, "GILLIAN", "ANDERSON", UNCATEGORISED, null),
                Tuple.tuple("A1234AC", -3L, "NORMAN", "BATES", UNCATEGORISED, "X"),
                Tuple.tuple("A1234AD", -4L, "CHARLES", "CHAPLIN", UNCATEGORISED, "U"),
                Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", UNCATEGORISED, "Z"),
                Tuple.tuple("A1176RS", -32L, "FRED", "JAMES", UNCATEGORISED, null));

        assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status",
                "categoriserFirstName", "categoriserLastName", "category").contains(
                Tuple.tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", AWAITING_APPROVAL, "Elite2", "User", "B"));

        assertThat(list).extracting("offenderNo").doesNotContain("A1234AF", "A1234AG"); // "Active" categorisation should be ignored
        // Note that size of list may vary depending on whether feature tests have run, e.g. approving booking id -34
    }

    @Test
    public void testGetApprovedCategorised() {
        final var list = repository.getApprovedCategorised("LEI", LocalDate.of(1976, 5, 5));

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list)
                .extracting("offenderNo", "bookingId", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category")
                .contains(Tuple.tuple("A5576RS", -31L, "API", "User", "CA", "User", "A"));
    }

    @Test
    public void testGetOffenderCategorisations() {
        final var list = repository.getOffenderCategorisations(Arrays.asList(-1L, -31L), "LEI");

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list)
                .extracting("offenderNo", "bookingId", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category")
                .containsExactly(Tuple.tuple("A5576RS", -31L, "API", "User", "CA", "User", "A"));
    }

    @Test
    public void testGetApprovedCategorisedNoResults() {
        final var list = repository.getApprovedCategorised("MDI", LocalDate.of(2022, 5, 5));
        assertThat(list).hasSize(0);
    }

    @Test
    public void testGetRecategoriseNoResults() {
        final var list = repository.getRecategorise("LEI", LocalDate.of(2003, 5, 5));

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).hasSize(0);
    }

    @Test
    public void testGetRecategorise() {
        final var list = repository.getRecategorise("LEI", LocalDate.of(2018, 6, 7));

        assertThat(list)
                .extracting("offenderNo", "bookingId", "firstName", "lastName", "category", "nextReviewDate")
                .containsExactly(
                        Tuple.tuple("A1234AC", -3L, "NORMAN", "BATES", "X", LocalDate.of(2016, 6, 8)),
                        Tuple.tuple("A1234AD", -4L, "CHARLES", "CHAPLIN", "U", LocalDate.of(2016, 6, 8)),
                        Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", "Z", LocalDate.of(2016, 6, 8)),
                        Tuple.tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", "LOW", LocalDate.of(2018, 6, 1)),
                        Tuple.tuple("A1234AF", -6L, "ANTHONY", "ANDREWS", "C", LocalDate.of(2018, 6, 7)),
                        Tuple.tuple("A1234AG", -7L, "GILES", "SMITH", "C", LocalDate.of(2018, 6, 7))
                );
    }

    @Test
    public void testGetAllAssessments() {
        final var list = repository.findAssessmentsByOffenderNo(
                List.of("A1234AF"), "CATEGORY", Collections.emptySet(), false);

        list.sort(Comparator.comparing(AssessmentDto::getOffenderNo).thenComparing(AssessmentDto::getBookingId));
        assertThat(list).extracting("offenderNo", "bookingId", "assessmentCode",
                "assessmentDescription", "assessmentDate", "assessmentSeq", "nextReviewDate",
                "reviewSupLevelType", "reviewSupLevelTypeDesc", "assessmentCreateLocation", "approvalDate", "overridedSupLevelType", "overridedSupLevelTypeDesc",
                "calcSupLevelType", "calcSupLevelTypeDesc", "cellSharingAlertFlag", "assessStatus"

        ).containsExactlyInAnyOrder(
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 4, 4), 1, LocalDate.of(2016, 6, 8), "A", "Cat A", "LEI", LocalDate.of(2016, 6, 6), "D", "Cat D",  "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -6L, "CATEGORY", "Categorisation", LocalDate.of(2017, 4, 4), 2, LocalDate.of(2018, 6, 7), "C", "Cat C", null, null, null, null, null, null, false, "A")
        );
    }

    @Test
    @Transactional
    public void testInsertCategory() {
        final var uncat = repository.getUncategorised("LEI");

        assertThat(uncat).extracting("offenderNo", "bookingId", "firstName", "lastName", "status").doesNotContain(
                Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", AWAITING_APPROVAL));

        final var catDetail = CategorisationDetail.builder()
                .bookingId(-5L)
                .category("D")
                .committee("GOV")
                .comment("init cat")
                .nextReviewDate(LocalDate.of(2019, 6, 1))
                .build();

        repository.insertCategory(catDetail, "LEI", -11L, "JDOG");

        final var list = repository.getUncategorised("LEI");

        assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
                Tuple.tuple("A1234AE", -5L, "DONALD", "DUCK", AWAITING_APPROVAL));

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -5 AND ASSESSMENT_SEQ = 3");
        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("CALC_SUP_LEVEL_TYPE"),
                        extractInteger("ASSESSMENT_TYPE_ID"),
                        extractInteger("SCORE"),
                        extractString("ASSESS_STATUS"),
                        extractInteger("ASSESS_STAFF_ID"),
                        extractInteger("ASSESSOR_STAFF_ID"),
                        extractString("ASSESS_COMMENT_TEXT"),
                        extractString("ASSESSMENT_CREATE_LOCATION"),
                        extractString("ASSESS_COMMITTE_CODE"),
                        extractString("CREATION_USER"),
                        extractString("CREATE_USER_ID"),
                        extractString("MODIFY_USER_ID"))
                .contains(Tuple.tuple(3, "D", -2, 1006, "P", -11, -11, "init cat", "LEI", "GOV", "JDOG", "JDOG", "JDOG"));

        assertThat((Date) results.get(0).get("ASSESSMENT_DATE")).isToday();
        assertThat((Date) results.get(0).get("CREATION_DATE")).isToday();
        assertThat((Date) results.get(0).get("CREATE_DATETIME")).isToday();
        assertThat((Date) results.get(0).get("MODIFY_DATETIME")).isToday();
        assertThat((Timestamp) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2019-06-01T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testApproveCategoryAllFields() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-1L)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .approvedCategoryComment("My comment")
                .reviewCommitteeCode("REVIEW")
                .committeeCommentText("committeeCommentText")
                .reviewPlacementAgencyId("MDI")
                .reviewPlacementText("reviewPlacementText")
                .nextReviewDate(LocalDate.of(2019, 7, 24))
                .build();

        repository.approveCategory(catDetail, UserDetail.builder().staffId(-10L).username("KDOG").build());

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)");
        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"), extractString("MODIFY_USER_ID"))
                .contains(Tuple.tuple(6, "I", "KDOG")
                );
        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("REVIEW_SUP_LEVEL_TYPE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("ASSESS_STATUS"),
                        extractString("REVIEW_SUP_LEVEL_TEXT"),
                        extractString("COMMITTE_COMMENT_TEXT"),
                        extractString("REVIEW_PLACE_AGY_LOC_ID"),
                        extractString("REVIEW_PLACEMENT_TEXT"),
                        extractString("MODIFY_USER_ID"))
                .contains(Tuple.tuple(8, "C", "REVIEW", "APP", "A", "My comment", "committeeCommentText", "MDI", "reviewPlacementText", "KDOG")
                );
        assertThat((Date) results.get(0).get("MODIFY_DATETIME")).isToday();
        assertThat((Timestamp) results.get(0).get("EVALUATION_DATE")).isNull();
        assertThat((Date) results.get(1).get("MODIFY_DATETIME")).isToday();
        assertThat((Timestamp) results.get(1).get("EVALUATION_DATE")).isCloseTo("2019-02-27T00:00:00.000", 1000);
        assertThat((Timestamp) results.get(1).get("NEXT_REVIEW_DATE")).isCloseTo("2019-07-24T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testApproveCategoryMinimalFields() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-1L)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .reviewCommitteeCode("REVIEW")
                .build();

        repository.approveCategory(catDetail, UserDetail.builder().staffId(-10L).username("KDOG").build());

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)");
        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("REVIEW_SUP_LEVEL_TYPE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("ASSESS_STATUS"),
                        extractString("REVIEW_SUP_LEVEL_TEXT"),
                        extractString("COMMITTE_COMMENT_TEXT"),
                        extractString("REVIEW_PLACE_AGY_LOC_ID"),
                        extractString("REVIEW_PLACEMENT_TEXT"),
                        extractString("MODIFY_USER_ID"))
                .contains(Tuple.tuple(8, "C", "REVIEW", "APP", "A", null, null, null, null, "KDOG")
                );
        assertThat((Timestamp) results.get(1).get("NEXT_REVIEW_DATE")).isCloseTo("2018-06-01T00:00:00.000", 1000);
    }

    private static Function<Object, String> extractString(String field) {
        return m -> ((Map<String, String>) m).get(field);
    }

    private static Function<Object, Integer> extractInteger(String field) {
        return m -> ((Map<String, BigDecimal>) m).get(field).intValue();
    }

    @Test
    public void testThatActiveOffendersAreReturnedMatchingNumberAndCaseLoad() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI", "A1183SH"), false, Set.of("LEI"));
        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting("offenderNo", "bookingId", "agencyId", "firstName", "lastName", "middleName", "dateOfBirth", "assignedLivingUnitId").contains(
                Tuple.tuple("A1234AI", -9L, "LEI", "CHESTER", "THOMPSON", "JAMES", LocalDate.parse("1970-03-01"), -7L)
        );
    }

    @Test
    public void testAccessToAllData_whenTrue() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI"), true, Collections.emptySet());
        assertThat(offenders).containsExactly(new InmateBasicDetails(-9L, "A00119", "A1234AI", "CHESTER", "JAMES", "THOMPSON", "LEI", -7L, LocalDate.parse("1970-03-01")));
    }

    @Test
    public void testAccessToAllData_whenFalse() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI"), false, Set.of("HLI"));
        assertThat(offenders).isEmpty();
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIds() {
        final var offenders = repository.getBasicInmateDetailsByBookingIds("LEI", List.of(-3L, -4L, -35L));  //-35L ignored as it is MDI agency
        assertThat(offenders).containsExactlyInAnyOrder(new InmateBasicDetails(-3L, "A00113", "A1234AC", "NORMAN", "JOHN", "BATES", "LEI", -3L, LocalDate.parse("1999-10-27"))
                , new InmateBasicDetails(-4L, "A00114", "A1234AD", "CHARLES", "JAMES", "CHAPLIN", "LEI", -2L, LocalDate.parse("1970-01-01")));
    }

    @Test
    public void testGetLanguages() {
        assertThat(repository.getLanguages(-1))
                .containsExactly(
                        Language.builder().type("PREF_SPEAK").code("POL").description("Polish").build());

        assertThat(repository.getLanguages(-3))
                .containsExactlyInAnyOrder(
                        Language.builder().type("PREF_SPEAK").code("TUR").description("Turkish").build(),
                        Language.builder().type("PREF_SPEAK").code("ENG").description("English").build(),
                        Language.builder().type("SEC").code("ENG").description("English").build(),
                        Language.builder().type("SEC").code("KUR").description("Kurdish").build(),
                        Language.builder().type("SEC").code("SPA").description("Spanish; Castilian").build(),
                        Language.builder().type("PREF_WRITE").code("TUR").description("Turkish").build()
                );
    }

    @Test
    public void findPhysicalAttributes() {
        final var physicalAttributes = repository.findPhysicalAttributes(-1);
        assertThat(physicalAttributes).get().isEqualToIgnoringGivenFields(
                new PhysicalAttributes(Collections.emptyMap(), "Male", "W1", "White: British", 5, 6, null, 168, 165, 75),
                "additionalProperties");
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
