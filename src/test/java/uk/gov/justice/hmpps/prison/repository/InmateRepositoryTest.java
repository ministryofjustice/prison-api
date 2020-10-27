package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.AssessmentDto;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.time.LocalDate.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.api.support.CategorisationStatus.AWAITING_APPROVAL;
import static uk.gov.justice.hmpps.prison.api.support.CategorisationStatus.UNCATEGORISED;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractInteger;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractString;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class InmateRepositoryTest {

    @Autowired
    private InmateRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
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
        assertThat(foundInmates.getItems()).extracting(OffenderBooking::getLastName).contains("FOX", "BATES");
    }

    @Test
    public void testSearchForOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI", "BXI");
        final var alertFilter = List.of("XA", "HC");

        final var foundInmates = repository.searchForOffenderBookings(OffenderBookingSearchRequest.builder()
                .caseloads(caseloads)
                .offenderNo("A1234AA")
                .searchTerm1("A")
                .searchTerm2("A")
                .locationPrefix("LEI")
                .alerts(alertFilter)
                .convictedStatus("All")
                .pageRequest(pageRequest)
                .build());

        final var results = foundInmates.getItems();
        assertThat(results).extracting("bookingId", "offenderNo", "dateOfBirth", "assignedLivingUnitDesc").containsExactly(
                Tuple.tuple(-1L, "A1234AA", LocalDate.of(1969, Month.DECEMBER, 30), "A-1-1"));
    }

    @Test
    public void testSearchForOffenderBookingsSpaceInSurname() {
        final var foundInmates = repository.searchForOffenderBookings(OffenderBookingSearchRequest.builder()
                .caseloads(Set.of("LEI", "MDI"))
                .searchTerm1("HAR")
                .searchTerm2("JO")
                .locationPrefix("MDI")
                .pageRequest(new PageRequest("lastName, firstName"))
                .build());

        assertThat(foundInmates.getItems())
                .extracting(OffenderBooking::getBookingId, OffenderBooking::getOffenderNo, OffenderBooking::getDateOfBirth, OffenderBooking::getAssignedLivingUnitDesc)
                .containsExactly(Tuple.tuple(-55L, "A1180HL", parse("1980-05-21"), "1-2-014"));
    }

    @Test
    public void testSearchForOffenderBookingsSpaceInForename() {
        final var foundInmates = repository.searchForOffenderBookings(OffenderBookingSearchRequest.builder()
                .caseloads(Set.of("LEI", "MDI"))
                .searchTerm1("JO")
                .searchTerm2("JAM")
                .locationPrefix("MDI")
                .pageRequest(new PageRequest("lastName, firstName"))
                .build());

        assertThat(foundInmates.getItems())
                .extracting(OffenderBooking::getBookingId, OffenderBooking::getOffenderNo, OffenderBooking::getDateOfBirth, OffenderBooking::getAssignedLivingUnitDesc)
                .containsExactly(Tuple.tuple(-55L, "A1180HL", parse("1980-05-21"), "1-2-014"));
    }

    @Test
    public void testSearchForConvictedOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(
                OffenderBookingSearchRequest.builder()
                        .caseloads(caseloads)
                        .locationPrefix("LEI")
                        .convictedStatus("Convicted")
                        .pageRequest(pageRequest)
                        .build());

        final var results = foundInmates.getItems();

        assertThat(results).hasSize(8);
        assertThat(results).extracting("convictedStatus").containsOnlyElementsOf(List.of("Convicted"));
        assertThat(results).extracting("imprisonmentStatus").containsOnlyElementsOf(List.of("SENT", "DEPORT"));
    }

    @Test
    public void testSearchForRemandOffenderBookings() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(OffenderBookingSearchRequest.builder()
                .caseloads(caseloads)
                .locationPrefix("LEI")
                .convictedStatus("Remand")
                .pageRequest(pageRequest)
                .build());

        final var results = foundInmates.getItems();

        assertThat(results).hasSize(3);
        assertThat(results).extracting("convictedStatus").containsOnlyElementsOf(List.of("Remand"));
        assertThat(results).extracting("imprisonmentStatus").containsOnlyElementsOf(List.of("TRL"));
    }

    @Test
    public void testSearchForAllConvictedStatus() {
        final var pageRequest = new PageRequest("lastName, firstName");
        final var caseloads = Set.of("LEI");

        final var foundInmates = repository.searchForOffenderBookings(OffenderBookingSearchRequest.builder()
                .caseloads(caseloads)
                .locationPrefix("LEI")
                .convictedStatus("All")
                .pageRequest(pageRequest)
                .build());

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
        assertThat(result.getBirthPlace()).isEqualTo("WALES");
        assertThat(result.getBirthCountryCode()).isEqualTo("UK");
    }

    @Test
    public void testGetBasicOffenderDetails() {
        final var inmate = repository.getBasicInmateDetail(-1L);
        assertThat(inmate).isPresent();
    }

    @Test
    public void testFindOffendersWithValidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = "A1234AP";

        final var query = buildQuery(criteriaForOffenderNo(List.of(TEST_OFFENDER_NO)));

        final var offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testFindOffendersWithLocationFilterIN() {

        final var query = buildQuery(criteriaForLocationFilter("IN"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(49);
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

        assertThat(offenders).hasSize(54);
    }

    @Test
    public void testFindOffendersWithGenderFilterMale() {

        final var query = buildQuery(criteriaForGenderFilter("M"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(50);
    }

    @Test
    public void testFindOffendersWithGenderFilterALL() {

        final var query = buildQuery(criteriaForGenderFilter("ALL"));

        final var offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders).hasSize(54);
    }

    @Test
    public void testFindOffendersWithInvalidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = List.of("X9999XX");

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

    @Test
    public void testFindOffendersWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        assertThatThrownBy(() -> buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER))).isInstanceOf(IllegalArgumentException.class);
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
        final var query = buildQuery(criteriaForPersonalAttrs(null, "JONES", "HARRY"));

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

        assertThat(offenders).hasSize(10);
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

        final var query = buildQuery(criteriaForOffenderNo(List.of(TEST_OFFENDER_NO)));

        final var offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testfindOffenderAliasesWithInvalidOffenderNoOnly() {
        final var TEST_OFFENDER_NO = List.of("X9999XX");

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

    @Test
    public void testfindOffenderAliasesWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        assertThatThrownBy(() -> buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER))).isInstanceOf(IllegalArgumentException.class);
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
        final var query = buildQuery(criteriaForPersonalAttrs(null, "JONES", "HARRY"));

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

        assertThat(offenders).hasSize(10);
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
                Tuple.tuple("A1234AE", -5L, "DONALD", "MATTHEWS", UNCATEGORISED, "Z"));

        assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status",
                "categoriserFirstName", "categoriserLastName", "category").contains(
                Tuple.tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", AWAITING_APPROVAL, "Prison", "User", "B"));

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
    public void testGetOffenderCategorisationsLatest() {
        final var list = repository.getOffenderCategorisations(Arrays.asList(-1L, -31L), "LEI", true);

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list)
                .extracting("offenderNo", "bookingId", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category", "assessStatus")
                .containsExactly(
                        Tuple.tuple("A1234AA", -1L, "API", "User", "Prison", "User", "B", "P"),
                        Tuple.tuple("A5576RS", -31L, "API", "User", "CA", "User", "A", "A"));
    }

    @Test
    public void testGetOffenderCategorisationsAll() {
        final var list = repository.getOffenderCategorisations(Arrays.asList(-1L, -31L), "LEI", false);
        assertThat(list)
                .extracting("offenderNo", "bookingId", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category", "assessStatus")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("A1234AA", -1L, "API", "User", "Prison", "User", "LOW", "A"),
                        Tuple.tuple("A1234AA", -1L, "API", "User", "Prison", "User", "B", "P"),
                        Tuple.tuple("A5576RS", -31L, "API", "User", "CA", "User", "A", "A"),
                        Tuple.tuple("A5576RS", -31L, "API", "User", "API", "User", "C", "A"));
    }

    @Test
    public void testGetOffenderCategorisationsNoApprover() {
        final var list = repository.getOffenderCategorisations(List.of(-41L), "SYI", false);
        assertThat(list)
                .extracting("offenderNo", "bookingId", "lastName", "approverFirstName", "approverLastName", "categoriserFirstName", "categoriserLastName", "category", "assessStatus")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("A1184MA", -41L, "ALI", null, null, "Prison", "User", "B", "A"));
    }

    @Test
    public void testGetApprovedCategorisedNoResults() {
        final var list = repository.getApprovedCategorised("MDI", LocalDate.of(2022, 5, 5));
        assertThat(list).hasSize(0);
    }

    @Test
    public void testGetRecategoriseNoResults() {
        final var list = repository.getRecategorise("BMI", LocalDate.of(2003, 5, 5));

        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).hasSize(0);
    }

    @Test
    public void testGetRecategoriseRemovesNonStandardCategoryResults() {
        final var list = repository.getRecategorise("LEI", LocalDate.of(2018, 6, 7));

        assertThat(list)
                .extracting("offenderNo", "bookingId", "firstName", "lastName", "category", "nextReviewDate", "assessmentSeq", "assessStatus")
                .contains( //-34 pending may or may not be present during the build as the feature tests approve it
                        Tuple.tuple("A1234AA", -1L, "ARTHUR", "ANDERSON", "B", LocalDate.of(2018, 6, 1), 8, "P"),
                        Tuple.tuple("A1234AF", -6L, "ANTHONY", "ANDREWS", "C", LocalDate.of(2018, 6, 7), 2, "A"),
                        Tuple.tuple("A1234AG", -7L, "GILES", "SMITH", "C", LocalDate.of(2018, 6, 7), 1, "A")
                );
    }

    @Test
    @Transactional
    public void testGetRecategoriseRemovesNonStandardCatA() {
        final var possibly_38_39_40_41 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 30));
        assertThat(possibly_38_39_40_41).hasSize(2);

        // -40 is a cat A but was a B earlier.
        assertThat(possibly_38_39_40_41).extracting("bookingId").doesNotContain(Tuple.tuple(-40L));
    }

    @Test
    public void testGetRecategoriseIgnoresEarlierPendingOrActive() {
        // booking id -37 has 3 active or pending categorisation records
        final var list = repository.getRecategorise("WAI", LocalDate.of(2019, 6, 7));

        assertThat(list)
                .extracting("offenderNo", "bookingId", "firstName", "lastName", "category", "nextReviewDate", "assessmentSeq")
                .containsExactly(
                        Tuple.tuple("A1181MV", -37L, "MICHAEL", "O'VAUGHAN", "B", LocalDate.of(2016, 8, 8), 3)
                );
    }

    @Test
    public void testGetRecategorisePendingLatestAfterCutoff() {
        final var list1 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 30));
        assertThat(list1).hasSize(2);

        // -38 and -39 within the cutoff
        assertThat(list1).extracting("bookingId", "assessmentSeq", "nextReviewDate", "assessStatus"
        ).containsExactlyInAnyOrder(Tuple.tuple(-38L, 3, LocalDate.of(2019, 6, 8), "P"),
                Tuple.tuple(-39L, 2, LocalDate.of(2019, 6, 8), "A"));

        // The latest seq of booking id -38 is now after the cutoff but is pending - so should be selected, -39 is active and after cutoff:
        final var list2 = repository.getRecategorise("SYI", LocalDate.of(2019, 6, 1));

        assertThat(list2).extracting("bookingId", "assessmentSeq", "nextReviewDate", "assessStatus"
        ).containsExactly(Tuple.tuple(-38L, 3, LocalDate.of(2019, 6, 8), "P"));
    }

    @Test
    public void testGetALLActiveAssessments() {
        final var list = repository.findAssessmentsByOffenderNo(
                List.of("A1234AF"), "CATEGORY", Collections.emptySet(), false, true);

        list.sort(Comparator.comparing(AssessmentDto::getOffenderNo).thenComparing(AssessmentDto::getBookingId));
        assertThat(list).extracting("offenderNo", "bookingId", "assessmentCode",
                "assessmentDescription", "assessmentDate", "assessmentSeq", "nextReviewDate",
                "reviewSupLevelType", "reviewSupLevelTypeDesc", "assessmentCreateLocation", "approvalDate", "overridedSupLevelType", "overridedSupLevelTypeDesc",
                "calcSupLevelType", "calcSupLevelTypeDesc", "cellSharingAlertFlag", "assessStatus"

        ).containsExactlyInAnyOrder(
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 4, 4), 3, LocalDate.of(2016, 8, 8), "A", "Cat A", "LEI", LocalDate.of(2016, 7, 7), "D", "Cat D", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 5, 4), 2, LocalDate.of(2018, 5, 8), "B", "Cat B", "MDI", LocalDate.of(2016, 5, 9), "B", "Cat B", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -6L, "CATEGORY", "Categorisation", LocalDate.of(2017, 4, 4), 2, LocalDate.of(2018, 6, 7), "C", "Cat C", null, null, null, null, null, null, false, "A")
        );
    }

    @Test
    public void testGetAssessmentsIncludingHistoricalAndInactive() {
        final var list = repository.findAssessmentsByOffenderNo(
                List.of("A1234AF"), "CATEGORY", Collections.emptySet(), false, false);

        list.sort(Comparator.comparing(AssessmentDto::getOffenderNo).thenComparing(AssessmentDto::getBookingId));
        assertThat(list).extracting("offenderNo", "bookingId", "assessmentCode",
                "assessmentDescription", "assessmentDate", "assessmentSeq", "nextReviewDate",
                "reviewSupLevelType", "reviewSupLevelTypeDesc", "assessmentCreateLocation", "approvalDate", "overridedSupLevelType", "overridedSupLevelTypeDesc",
                "calcSupLevelType", "calcSupLevelTypeDesc", "cellSharingAlertFlag", "assessStatus"

        ).containsExactlyInAnyOrder(
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 4, 4), 3, LocalDate.of(2016, 8, 8), "A", "Cat A", "LEI", LocalDate.of(2016, 7, 7), "D", "Cat D", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 5, 4), 2, LocalDate.of(2018, 5, 8), "B", "Cat B", "MDI", LocalDate.of(2016, 5, 9), "B", "Cat B", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 3, 4), 1, LocalDate.of(2016, 3, 8), "B", "Cat B", "MDI", LocalDate.of(2016, 3, 9), "B", "Cat B", "B", "Cat B", false, "I"),
                Tuple.tuple("A1234AF", -6L, "CATEGORY", "Categorisation", LocalDate.of(2017, 4, 4), 2, LocalDate.of(2018, 6, 7), "C", "Cat C", null, null, null, null, null, null, false, "A")
        );
    }

    @Test
    public void testGetAssessmentsIncludingCsra() {
        final var list = repository.findAssessmentsByOffenderNo(
                List.of("A1234AF"), null, Collections.emptySet(), false, false);

        list.sort(Comparator.comparing(AssessmentDto::getOffenderNo).thenComparing(AssessmentDto::getBookingId));
        assertThat(list).extracting("offenderNo", "bookingId", "assessmentCode",
                "assessmentDescription", "assessmentDate", "assessmentSeq", "nextReviewDate",
                "reviewSupLevelType", "reviewSupLevelTypeDesc", "assessmentCreateLocation", "approvalDate", "overridedSupLevelType", "overridedSupLevelTypeDesc",
                "calcSupLevelType", "calcSupLevelTypeDesc", "cellSharingAlertFlag", "assessStatus"

        ).containsExactlyInAnyOrder(
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 4, 4), 3, LocalDate.of(2016, 8, 8), "A", "Cat A", "LEI", LocalDate.of(2016, 7, 7), "D", "Cat D", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 5, 4), 2, LocalDate.of(2018, 5, 8), "B", "Cat B", "MDI", LocalDate.of(2016, 5, 9), "B", "Cat B", "B", "Cat B", false, "A"),
                Tuple.tuple("A1234AF", -48L, "CATEGORY", "Categorisation", LocalDate.of(2016, 3, 4), 1, LocalDate.of(2016, 3, 8), "B", "Cat B", "MDI", LocalDate.of(2016, 3, 9), "B", "Cat B", "B", "Cat B", false, "I"),
                Tuple.tuple("A1234AF", -6L, "CSR", "CSR Rating", LocalDate.of(2017, 4, 4), 1, LocalDate.of(2018, 6, 6), "STANDARD", "Standard", null, null, null, null, "MED", "Medium", true, "A"),
                Tuple.tuple("A1234AF", -6L, "PAROLE", "Parole", LocalDate.of(2017, 4, 4), 3, LocalDate.of(2018, 6, 8), null, null, null, null, "HI", "High", null, null, false, "A"),
                Tuple.tuple("A1234AF", -6L, "CATEGORY", "Categorisation", LocalDate.of(2017, 4, 4), 2, LocalDate.of(2018, 6, 7), "C", "Cat C", null, null, null, null, null, null, false, "A")
        );
    }

    @Test
    @Transactional
    public void testInsertCategory() {
        final var uncat = repository.getUncategorised("LEI");

        assertThat(uncat).extracting("offenderNo", "bookingId", "firstName", "lastName", "status").doesNotContain(
                Tuple.tuple("A1234AE", -5L, "DONALD", "MATTHEWS", AWAITING_APPROVAL));

        final var catDetail = CategorisationDetail.builder()
                .bookingId(-5L)
                .category("D")
                .committee("GOV")
                .comment("init cat")
                .nextReviewDate(LocalDate.of(2019, 6, 1))
                .placementAgencyId("BMI")
                .build();

        final var responseMap = repository.insertCategory(catDetail, "LEI", -11L, "JDOG");

        assertThat(responseMap).contains(entry("bookingId", -5L), entry("sequenceNumber", 3L)); // 2 previous category records for A1234AE


        final var list = repository.getUncategorised("LEI");

        assertThat(list).extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
                Tuple.tuple("A1234AE", -5L, "DONALD", "MATTHEWS", AWAITING_APPROVAL));

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -5 AND ASSESSMENT_SEQ = 3");
        assertThat(results)
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
                        extractString("PLACE_AGY_LOC_ID"))
                .contains(Tuple.tuple(3, "D", -2, 1006, "P", -11, -11, "init cat", "LEI", "GOV", "BMI"));

        assertThat((Date) results.get(0).get("ASSESSMENT_DATE")).isToday();
        assertThat((Date) results.get(0).get("CREATION_DATE")).isToday();
        assertThat((Timestamp) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2019-06-01T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testUpdateCategory() {

        final var catDetail = CategorisationUpdateDetail.builder()
                .bookingId(-32L)
                .assessmentSeq(4)
                .category("C")
                .committee("GOV")
                .comment("updated cat")
                .nextReviewDate(LocalDate.of(2019, 12, 1))
                .build();

        repository.updateCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 AND ASSESSMENT_SEQ = 4");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("CALC_SUP_LEVEL_TYPE"),
                        extractString("ASSESS_STATUS"),
                        extractString("ASSESS_COMMENT_TEXT"),
                        extractString("ASSESS_COMMITTE_CODE"))
                .containsExactly(Tuple.tuple(4, "C", "P", "updated cat", "GOV"));

        assertThat((Date) results.get(0).get("ASSESSMENT_DATE")).isToday();
        assertThat((Timestamp) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2019-12-01T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testUpdateCategoryMinimalFields() {
        final var catDetail = CategorisationUpdateDetail.builder()
                .bookingId(-37L)
                .assessmentSeq(3)
                .build();

        repository.updateCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -37 AND ASSESSMENT_SEQ = 3");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("CALC_SUP_LEVEL_TYPE"))
                .containsExactly(Tuple.tuple(3, "B"));

        assertThat((Date) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2016-08-08", 1000L);
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
                .nextReviewDate(LocalDate.of(2019, 7, 24))
                .approvedPlacementAgencyId("BXI")
                .approvedPlacementText("approvedPlacementText")
                .build();

        repository.approveCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
                .contains(Tuple.tuple(6, "I")
                );
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("REVIEW_SUP_LEVEL_TYPE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("ASSESS_STATUS"),
                        extractString("REVIEW_SUP_LEVEL_TEXT"),
                        extractString("COMMITTE_COMMENT_TEXT"),
                        extractString("REVIEW_PLACE_AGY_LOC_ID"),
                        extractString("REVIEW_PLACEMENT_TEXT"))

                .contains(Tuple.tuple(8, "C", "REVIEW", "APP", "A", "My comment", "committeeCommentText", "BXI", "approvedPlacementText")
                );
        assertThat((Timestamp) results.get(0).get("EVALUATION_DATE")).isNull();
        assertThat((Timestamp) results.get(1).get("EVALUATION_DATE")).isCloseTo("2019-02-27T00:00:00.000", 1000);
        assertThat((Timestamp) results.get(1).get("NEXT_REVIEW_DATE")).isCloseTo("2019-07-24T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testApproveCategoryHandlesMultipleActiveCategorisations() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-32L)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .approvedCategoryComment("My comment")
                .reviewCommitteeCode("REVIEW")
                .committeeCommentText("committeeCommentText")
                .nextReviewDate(LocalDate.of(2019, 7, 24))
                .build();


        // 4 categorisation records with status Inactive, Active, Inactive, Pending
        repository.approveCategory(catDetail);


        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 order by ASSESSMENT_SEQ asc");

        // after making the pending cat active should make any earlier categorisation inactive (regardless of order)
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
                .contains(Tuple.tuple(1, "I"), Tuple.tuple(2, "I"), Tuple.tuple(3, "I"), Tuple.tuple(4, "A")
                );
        assertThat((Timestamp) results.get(3).get("EVALUATION_DATE")).isCloseTo("2019-02-27", 1000L);
    }

    @Test
    @Transactional
    public void testApproveCategoryHandlesNoPreviousCategorisation() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-36L)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .approvedCategoryComment("My comment")
                .reviewCommitteeCode("REVIEW")
                .committeeCommentText("committeeCommentText")
                .nextReviewDate(LocalDate.of(2019, 7, 24))
                .build();

        // 1 pending cateorisation record
        repository.approveCategory(catDetail);


        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -36 order by ASSESSMENT_SEQ asc");

        // confirm single categorisation is active
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
                .contains(Tuple.tuple(1, "A"));
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

        repository.approveCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("REVIEW_SUP_LEVEL_TYPE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("ASSESS_STATUS"),
                        extractString("REVIEW_SUP_LEVEL_TEXT"),
                        extractString("COMMITTE_COMMENT_TEXT"),
                        extractString("REVIEW_PLACE_AGY_LOC_ID"),
                        extractString("REVIEW_PLACEMENT_TEXT"))
                .contains(Tuple.tuple(8, "C", "REVIEW", "APP", "A", null, null, null, null)
                );
        assertThat((Timestamp) results.get(1).get("NEXT_REVIEW_DATE")).isCloseTo("2018-06-01T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testApproveCategoryUsingSeq() {
        final var catDetail = CategoryApprovalDetail.builder()
                .bookingId(-1L)
                .assessmentSeq(8)
                .category("C")
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .approvedCategoryComment("My comment")
                .reviewCommitteeCode("REVIEW")
                .committeeCommentText("committeeCommentText")
                .nextReviewDate(LocalDate.of(2019, 7, 24))
                .build();

        repository.approveCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -1 AND ASSESSMENT_SEQ in (6, 8)");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"), extractString("ASSESS_STATUS"))
                .contains(Tuple.tuple(6, "I")
                );
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("REVIEW_SUP_LEVEL_TYPE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("ASSESS_STATUS"),
                        extractString("REVIEW_SUP_LEVEL_TEXT"),
                        extractString("COMMITTE_COMMENT_TEXT"))
                .contains(Tuple.tuple(8, "C", "REVIEW", "APP", "A", "My comment", "committeeCommentText")
                );
        assertThat((Timestamp) results.get(0).get("EVALUATION_DATE")).isNull();
        assertThat((Timestamp) results.get(1).get("EVALUATION_DATE")).isCloseTo("2019-02-27T00:00:00.000", 1000);
        assertThat((Timestamp) results.get(1).get("NEXT_REVIEW_DATE")).isCloseTo("2019-07-24T00:00:00.000", 1000);
    }

    @Test
    @Transactional
    public void testRejectCategory() {
        final var catDetail = CategoryRejectionDetail.builder()
                .bookingId(-32L)
                .assessmentSeq(4)
                .evaluationDate(LocalDate.of(2019, 2, 27))
                .reviewCommitteeCode("REVIEW")
                .committeeCommentText("committeeCommentText")
                .build();

        repository.rejectCategory(catDetail);

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -32 AND ASSESSMENT_SEQ = 4");
        assertThat(results)
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("COMMITTE_COMMENT_TEXT"))
                .containsExactly(Tuple.tuple(4, "REJ", "REVIEW", "committeeCommentText"));

        assertThat((Date) results.get(0).get("EVALUATION_DATE")).isCloseTo("2019-02-27", 1000);
    }

    @Test
    @Transactional
    public void testRejectCategoryNotFound() {
        final var catDetail = CategoryRejectionDetail.builder()
                .bookingId(-32L)
                .assessmentSeq(99)
                .build();

        assertThatThrownBy(() -> repository.rejectCategory(catDetail)).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @Transactional
    public void testUpdateCategorySetInactive() {

        repository.setCategorisationInactive(-38L, null);

        final List<OffenderCategorise> catList = repository.getOffenderCategorisations(List.of(-38L), "BMI", false);
        // should have updated the 2 active and 1 pending record
        assertThat(catList).extracting("assessmentSeq", "assessStatus").containsExactlyInAnyOrder(
                Tuple.tuple(1, "I"),
                Tuple.tuple(2, "I"),
                Tuple.tuple(3, "P"));
    }

    @Test
    @Transactional
    public void testUpdateCategorySetInactivePending() {

        repository.setCategorisationInactive(-38L, AssessmentStatusType.PENDING);

        final List<OffenderCategorise> catList = repository.getOffenderCategorisations(List.of(-38L), "BMI", false);
        // should have updated the 2 active and 1 pending record
        assertThat(catList).extracting("assessmentSeq", "assessStatus").containsExactlyInAnyOrder(
                Tuple.tuple(1, "A"),
                Tuple.tuple(2, "A"),
                Tuple.tuple(3, "I"));
    }

    @Test
    @Transactional
    public void testUpdateCategoryNextReviewDate() {

        final var newNextReviewDate = LocalDate.of(2019, 2, 27);
        final var existingNextReviewDate = LocalDate.of(2018, 6, 1);
        repository.updateActiveCategoryNextReviewDate(-1L, newNextReviewDate);

        final List<OffenderCategorise> catList = repository.getOffenderCategorisations(List.of(-1L), "LEI", false);
        assertThat(catList.get(1).getNextReviewDate()).isEqualTo(newNextReviewDate);
        //should not have updated the later pending record
        assertThat(catList.get(0).getNextReviewDate()).isEqualTo(existingNextReviewDate);
    }

    @Test
    @Transactional
    public void testUpdateCategoryNextReviewDateForUnknownOffender() {

        final var newNextReviewDate = LocalDate.of(2019, 2, 27);

        try {
            repository.updateActiveCategoryNextReviewDate(-15655L, newNextReviewDate);
            fail("Should have thrown an EntityNotFoundException");
        } catch (final EntityNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Unable to update next review date, could not find latest, active categorisation for booking id -15655, result count = 0");
        }
    }

    @Test
    public void testThatActiveOffendersAreReturnedMatchingNumberAndCaseLoad() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI", "A1183SH"), false, Set.of("LEI"), true);
        assertThat(offenders).hasSize(1);
        assertThat(offenders).extracting("offenderNo", "bookingId", "agencyId", "firstName", "lastName", "middleName", "dateOfBirth", "assignedLivingUnitId").contains(
                Tuple.tuple("A1234AI", -9L, "LEI", "CHESTER", "THOMPSON", "JAMES", parse("1970-03-01"), -7L)
        );
    }

    @Test
    public void testAccessToAllData_whenTrue() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI"), true, Collections.emptySet(), false);
        assertThat(offenders).containsExactly(new InmateBasicDetails(-9L, "A00119", "A1234AI", "CHESTER", "JAMES", "THOMPSON", "LEI", -7L, parse("1970-03-01")));
    }

    @Test
    public void testAccessToAllData_whenFalse() {
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("A1234AI"), false, Set.of("HLI"), false);
        assertThat(offenders).isEmpty();
    }

    @Test
    public void testAccessToAllData_WithActiveOnlyTrue() {
        //Offender without an an active booking
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("Z0020ZZ"), true, Collections.emptySet(), true);
        assertThat(offenders).isEmpty();
    }

    @Test
    public void testAccessToAllData_WithActiveOnlyFalse() {
        //Offender without an an active booking
        final var offenders = repository.getBasicInmateDetailsForOffenders(Set.of("Z0020ZZ"), true, Collections.emptySet(), false);
        assertThat(offenders).hasSize(1);
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIds() {
        final var offenders = repository.getBasicInmateDetailsByBookingIds("LEI", List.of(-3L, -4L, -35L));  //-35L ignored as it is MDI agency
        assertThat(offenders).containsExactlyInAnyOrder(new InmateBasicDetails(-3L, "A00113", "A1234AC", "NORMAN", "JOHN", "BATES", "LEI", -3L, parse("1999-10-27"))
                , new InmateBasicDetails(-4L, "A00114", "A1234AD", "CHARLES", "JAMES", "CHAPLIN", "LEI", -2L, parse("1970-01-01")));
    }


    @Test
    public void findPhysicalAttributes() {
        final var physicalAttributes = repository.findPhysicalAttributes(-1);
        assertThat(physicalAttributes).get().isEqualToIgnoringGivenFields(
                new PhysicalAttributes(Collections.emptyMap(), "Male", "W1", "White: British", 5, 6, null, 168, 165, 75),
                "additionalProperties");
    }

    @Test
    public void getPersonalCareNeeds() {
        final var info = repository.findPersonalCareNeeds(-1, Set.of("DISAB", "MATSTAT"));
        assertThat(info).containsExactly(
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText("Some Description Text 1")
                        .startDate(LocalDate.parse("2010-06-21")).build(),
                PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON")
                        .problemDescription("Preg, acc under 9mths").commentText("P1")
                        .startDate(LocalDate.parse("2010-06-21")).build());
    }

    @Test
    public void getPersonalCareNeedsForOffenderNos() {
        final var info = repository.findPersonalCareNeeds(List.of("A1234AA", "A1234AB", "A1234AC", "A1234AD"), Set.of("DISAB", "MATSTAT"));
        assertThat(info).containsExactly(
                PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON")
                        .problemDescription("Preg, acc under 9mths").commentText("P1")
                        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText("Some Description Text 1")
                        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText(null)
                        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AB").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText(null)
                        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AC").build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("ND").problemStatus("ON")
                        .problemDescription("No Disability").commentText("Some Description Text 2")
                        .startDate(LocalDate.parse("2010-06-24")).endDate(null).offenderNo("A1234AD").build());
    }

    @Test
    public void getReasonableAdjustment() {
        final var expectedInfo = List.of(
                ReasonableAdjustment.builder()
                        .treatmentCode("COMP SOFT")
                        .treatmentDescription("Computer software")
                        .commentText("EFGH")
                        .startDate(LocalDate.of(2010, 6, 21))
                        .build(),
                ReasonableAdjustment.builder()
                        .treatmentCode("WHEELCHR_ACC")
                        .treatmentDescription("Wheelchair accessibility")
                        .commentText("Some Comment Text")
                        .startDate(LocalDate.of(2010, 6, 21))
                        .build());
        final var treatmentCodes = List.of("WHEELCHR_ACC", "COMP SOFT");
        final var info = repository.findReasonableAdjustments(-1, treatmentCodes);
        assertThat(info).isEqualTo(expectedInfo);
    }

    @Test
    public void getOffenderDetailsContainsReceptionDate(){
        final var offender = repository.findOffender("A1234AA");
        assertThat(offender.get().getReceptionDate()).isEqualTo(LocalDate.now());

    }

    /*****************************************************************************************/

    private PrisonerDetailSearchCriteria criteriaForOffenderNo(final List<String> offenderNos) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNos(offenderNos)
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

    private PrisonerDetailSearchCriteria criteriaForPersonalAttrs(final List<String> offenderNos, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNos(offenderNos)
                .lastName(lastName)
                .firstName(firstName)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPartialPersonalAttrs(final List<String> offenderNos, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNos(offenderNos)
                .lastName(lastName)
                .firstName(firstName)
                .partialNameMatch(true)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForAnyPersonalAttrs(final List<String> offenderNos, final String lastName, final String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNos(offenderNos)
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
                .location(location)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForGenderFilter(final String gender) {
        return PrisonerDetailSearchCriteria.builder()
                .gender(gender)
                .build();
    }

    private PrisonerDetailSearchCriteria addDOBRangeCriteria(final PrisonerDetailSearchCriteria criteria,
                                                             final LocalDate dob, final LocalDate dobFrom, final LocalDate dobTo) {
        return criteria.withDob(dob).withDobFrom(dobFrom).withDobTo(dobTo).withMaxYearsRange(10);
    }

    private String buildQuery(final PrisonerDetailSearchCriteria criteria) {
        return repository.generateFindOffendersQuery(criteria);
    }

    private PrisonerDetail findOffender(final String query) {
        final var page = repository.findOffenders(query, new PageRequest());

        assertThat(page.getItems()).hasSize(1);

        return page.getItems().get(0);
    }

    private PrisonerDetail findOffenderWithAliases(final String query) {
        final var page = repository.findOffendersWithAliases(query, new PageRequest());

        assertThat(page.getItems()).hasSize(1);

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
