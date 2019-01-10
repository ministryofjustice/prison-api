package net.syscon.elite.repository;

import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.OffenderCategorise;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testFindAllImates() {
        final PageRequest pageRequest = new PageRequest("lastName, firstName");
        final HashSet<String> caseloads = new HashSet<>(Arrays.asList("LEI", "BXI"));
        Page<OffenderBooking> foundInmates = repository.findAllInmates(caseloads, "WING", "", pageRequest);

        assertThat(foundInmates.getItems()).isNotEmpty();
    }

    @Test
    public void testSearchForOffenderBookings() {
        final PageRequest pageRequest = new PageRequest("lastName, firstName");
        final HashSet<String> caseloads = new HashSet<>(Arrays.asList("LEI", "BXI"));
        List<String> alertFilter = Arrays.asList("XA", "HC");

        Page<OffenderBooking> foundInmates = repository.searchForOffenderBookings(caseloads, "A1234AA", "A", "A", "LEI",
                alertFilter, "WING", pageRequest);

        final List<OffenderBooking> results = foundInmates.getItems();
        assertThat(results).asList().hasSize(1);
        assertThat(results).asList().extracting("bookingId", "offenderNo", "dateOfBirth", "assignedLivingUnitDesc").contains(
                Tuple.tuple(-1L, "A1234AA", LocalDate.of(1969, Month.DECEMBER, 30), "A-1-1"));
    }

    @Test
    public void testGetOffender() {
        Optional<InmateDetail> inmate = repository.findInmate(-1L);
        assertThat(inmate).isPresent();
        final InmateDetail result = inmate.get();
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
        Optional<InmateDetail> inmate = repository.getBasicInmateDetail(-1L);
        assertThat(inmate).isPresent();
    }

    @Test
    public void testfindOffendersWithValidOffenderNoOnly() {
        final String TEST_OFFENDER_NO = "A1234AP";

        String query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testfindOffendersWithLocationFilterIN() {

        String query = buildQuery(criteriaForLocationFilter("IN"));

        final List<PrisonerDetail> offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(45);
    }

    @Test
    public void testfindOffendersWithLocationFilterOut() {

        String query = buildQuery(criteriaForLocationFilter("OUT"));

        final List<PrisonerDetail> offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(1);
    }

    @Test
    public void testfindOffendersWithLocationFilterALL() {

        String query = buildQuery(criteriaForLocationFilter("ALL"));

        final List<PrisonerDetail> offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(46);
    }

    @Test
    public void testfindOffendersWithGenderFilterMale() {

        String query = buildQuery(criteriaForGenderFilter("M"));

        final List<PrisonerDetail> offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(44);
    }

    @Test
    public void testfindOffendersWithGenderFilterALL() {

        String query = buildQuery(criteriaForGenderFilter("ALL"));

        final List<PrisonerDetail> offenders = findOffendersWithAliasesFullResults(query);

        assertThat(offenders.size()).isEqualTo(46);
    }

    @Test
    public void testfindOffendersWithInvalidOffenderNoOnly() {
        final String TEST_OFFENDER_NO = "X9999XX";

        String query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        assertThat(findOffenders(query)).isEmpty();
    }

    @Test
    public void testfindOffendersWithValidPNCNumberOnly() {
        final String TEST_PNC_NUMBER = "14/12345F";

        String query = buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffendersWithInvalidPNCNumberOnly() {
        final String TEST_PNC_NUMBER = "PNC0193032";

        buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));
    }

    @Test
    public void testfindOffendersWithValidCRONumberOnly() {
        final String TEST_CRO_NUMBER = "CRO112233";

        String query = buildQuery(criteriaForCRONumber(TEST_CRO_NUMBER));

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    @Test
    public void testFindOffendersWithLastName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null));

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithLastNameLowerCase() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffendersWithFirstName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffendersWithFirstNameLowerCase() {
        String query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffendersWithFirstNameAndLastName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "TRUMP", "DONALD"));

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AH");
    }

    @Test
    public void testFindOffendersWithDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForDOBRange(
                LocalDate.of(1964, 1, 1), null, null);

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("Z0021ZZ");
    }

    @Test
    public void testFindOffendersWithDateOfBirthRange() {
        PrisonerDetailSearchCriteria criteria = criteriaForDOBRange(
                null, LocalDate.of(1960, 1, 1), LocalDate.of(1969, 12, 31));

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(9);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo)
                .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA");
    }

    @Test
    public void testFindOffendersWithLastNameAndDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForPersonalAttrs(null, "QUIMBY", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null);

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1178RS");
    }

    @Test
    public void testFindOffendersWithPartialLastName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, "ST", null);

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffendersWithPartialFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0017ZZ", "A1180MA", "A1181MV");
    }

    @Test
    public void testFindOffendersWithPartialLastNameAndFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA");

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffender(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1179MT");
    }

    @Test
    public void testFindOffendersWithLastNameOrFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS");

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffendersWithLastNameOrDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffenders(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0021ZZ", "A1183CW");
    }

    /********************/
    @Test
    public void testfindOffenderAliasesWithValidOffenderNoOnly() {
        final String TEST_OFFENDER_NO = "A1234AP";

        String query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo(TEST_OFFENDER_NO);
        assertThat(offender.getLastName()).isEqualTo("SCISSORHANDS");
    }

    @Test
    public void testfindOffenderAliasesWithInvalidOffenderNoOnly() {
        final String TEST_OFFENDER_NO = "X9999XX";

        String query = buildQuery(criteriaForOffenderNo(TEST_OFFENDER_NO));

        assertThat(findOffendersWithAliases(query)).isEmpty();
    }

    @Test
    public void testfindOffenderAliasesWithValidPNCNumberOnly() {
        final String TEST_PNC_NUMBER = "14/12345F";

        String query = buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffenderAliasesWithInvalidPNCNumberOnly() {
        final String TEST_PNC_NUMBER = "PNC0193032";

        buildQuery(criteriaForPNCNumber(TEST_PNC_NUMBER));
    }

    @Test
    public void testfindOffenderAliasesWithValidCRONumberOnly() {
        final String TEST_CRO_NUMBER = "CRO112233";

        String query = buildQuery(criteriaForCRONumber(TEST_CRO_NUMBER));

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    @Test
    public void testFindOffenderAliasesWithLastName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "SMITH", null));

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameLowerCase() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "smith", null));

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(4);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AG", "A1234AJ", "A1234AK", "Z0025ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithFirstName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, null, "DANIEL"));

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffenderAliasesWithFirstNameLowerCase() {
        String query = buildQuery(criteriaForPersonalAttrs(null, null, "daniel"));

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1234AJ", "A1234AL");
    }

    @Test
    public void testFindOffenderAliasesWithFirstNameAndLastName() {
        String query = buildQuery(criteriaForPersonalAttrs(null, "TRUMP", "DONALD"));

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AH");
    }

    @Test
    public void testFindOffenderAliasesWithDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForDOBRange(
                LocalDate.of(1964, 1, 1), null, null);

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("Z0021ZZ");
    }

    @Test
    public void testFindOffenderAliasesWithDateOfBirthRange() {
        PrisonerDetailSearchCriteria criteria = criteriaForDOBRange(
                null, LocalDate.of(1960, 1, 1), LocalDate.of(1969, 12, 31));

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(9);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo)
                .contains("A1234AA", "A1234AF", "A1234AL", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ", "A1180MA");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameAndDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForPersonalAttrs(null, "QUIMBY", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1945, 1, 10), null, null);

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1178RS");
    }

    @Test
    public void testFindOffenderAliasesWithPartialLastName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, "ST", null);

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0019ZZ", "A9876RS", "A1182BS");
    }

    @Test
    public void testFindOffenderAliasesWithPartialFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, null, "MIC");

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(3);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0017ZZ", "A1180MA", "A1181MV");
    }

    @Test
    public void testFindOffenderAliasesWithPartialLastNameAndFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForPartialPersonalAttrs(null, "TR", "MA");

        String query = buildQuery(criteria);

        PrisonerDetail offender = findOffenderWithAliases(query);

        assertThat(offender.getOffenderNo()).isEqualTo("A1179MT");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameOrFirstName() {
        PrisonerDetailSearchCriteria criteria = criteriaForAnyPersonalAttrs(null, "QUIMBY", "MARCUS");

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("A1178RS", "A1179MT");
    }

    @Test
    public void testFindOffenderAliasesWithLastNameOrDateOfBirth() {
        PrisonerDetailSearchCriteria criteria = criteriaForAnyPersonalAttrs(null, "WOAKES", null);

        criteria = addDOBRangeCriteria(criteria, LocalDate.of(1964, 1, 1), null, null);

        String query = buildQuery(criteria);

        List<PrisonerDetail> offenders = findOffendersWithAliases(query);

        assertThat(offenders.size()).isEqualTo(2);
        assertThat(offenders).extracting(PrisonerDetail::getOffenderNo).contains("Z0021ZZ", "A1183CW");
    }

    @Test
    public void testGetUncategorisedGeneral() {
        final List<OffenderCategorise> list = repository.getUncategorised("LEI");

        //A1234AA does have a category so should not be present
        assertThat(list.stream().filter(a -> a.getOffenderNo().equals("A1234AA")).findAny().isPresent()).isFalse();
        list.sort(Comparator.comparing(OffenderCategorise::getOffenderNo));
        assertThat(list).asList().extracting("offenderNo", "bookingId", "firstName", "lastName", "status").contains(
                Tuple.tuple("A1234AB", -2L, "GILLIAN", "ANDERSON", UNCATEGORISED),
                Tuple.tuple("A1176RS", -32L, "FRED", "JAMES", UNCATEGORISED));
        assertThat(list).asList().hasSize(23);
        // TODO test for status pending
    }

    /*****************************************************************************************/

    private PrisonerDetailSearchCriteria criteriaForOffenderNo(String offenderNo) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPNCNumber(String pncNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .pncNumber(pncNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForCRONumber(String croNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .croNumber(croNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPersonalAttrs(String offenderNo, String lastName, String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForPartialPersonalAttrs(String offenderNo, String lastName, String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .partialNameMatch(true)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForAnyPersonalAttrs(String offenderNo, String lastName, String firstName) {
        return PrisonerDetailSearchCriteria.builder()
                .offenderNo(offenderNo)
                .lastName(lastName)
                .firstName(firstName)
                .anyMatch(true)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForDOBRange(LocalDate dob, LocalDate dobFrom, LocalDate dobTo) {
        return PrisonerDetailSearchCriteria.builder()
                .dob(dob)
                .dobFrom(dobFrom)
                .dobTo(dobTo)
                .maxYearsRange(10)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForLocationFilter(String location) {
        return PrisonerDetailSearchCriteria.builder()
                .latestLocationId(location)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForGenderFilter(String gender) {
        return PrisonerDetailSearchCriteria.builder()
                .sexCode(gender)
                .build();
    }

    private PrisonerDetailSearchCriteria addDOBRangeCriteria(PrisonerDetailSearchCriteria criteria,
                                                             LocalDate dob, LocalDate dobFrom, LocalDate dobTo) {
        return criteria.withDob(dob).withDobFrom(dobFrom).withDobTo(dobTo).withMaxYearsRange(10);
    }

    private String buildQuery(PrisonerDetailSearchCriteria criteria) {
        return InmateRepository.generateFindOffendersQuery(criteria);
    }

    private PrisonerDetail findOffender(String query) {
        Page<PrisonerDetail> page = repository.findOffenders(query, new PageRequest());

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }

    private PrisonerDetail findOffenderWithAliases(String query) {
        Page<PrisonerDetail> page = repository.findOffendersWithAliases(query, new PageRequest());

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }

    private List<PrisonerDetail> findOffenders(String query) {
        Page<PrisonerDetail> page = repository.findOffenders(query, new PageRequest());

        return page.getItems();
    }

    private List<PrisonerDetail> findOffendersWithAliases(String query) {
        Page<PrisonerDetail> page = repository.findOffendersWithAliases(query, new PageRequest());

        return page.getItems();
    }

    private List<PrisonerDetail> findOffendersWithAliasesFullResults(String query) {
        Page<PrisonerDetail> page = repository.findOffendersWithAliases(query, new PageRequest(0L, 1000L));

        return page.getItems();
    }

}
