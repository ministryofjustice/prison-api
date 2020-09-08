package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.service.OffenderIepReview;
import uk.gov.justice.hmpps.prison.service.OffenderIepReviewSearchCriteria;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AgencyRepositoryTest {

    @Autowired
    private AgencyRepository repository;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetEnabledAgencyWhenActiveOnly() {
        final var agency = repository.findAgency("LEI", ACTIVE_ONLY, "INST");
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetEnabledAgencyWithInactive() {
        final var agency = repository.findAgency("LEI", ALL, "INST");
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetDisabledAgencyWhenActiveOnly() {
        final var agency = repository.findAgency("ZZGHI", ACTIVE_ONLY, "INST");
        assertThat(agency).isEmpty();
    }

    @Test
    public void testGetDisabledAgencyWithInactive() {
        final var agency = repository.findAgency("ZZGHI", ALL, "INST");
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetEnabledAgencyWithNoAgencyTypeFilter() {
        final var agency = repository.findAgency("LEI", ALL, null);
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetAgencyWithWrongTypeFilter() {
        final var agency = repository.findAgency("COURT1", ALL, "INST");
        assertThat(agency).isNotPresent();
    }

    @Test
    public void testGetAgencyWithCorrectTypeFilter() {
        final var agency = repository.findAgency("COURT1", ALL, "CRT");
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetAgencyWithNoypeFilter() {
        final var agency = repository.findAgency("COURT1", ALL, null);
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetAgencyLocations() {
        final var locations = repository.getAgencyLocations("LEI", List.of("APP", "VISIT"), null, null);
        assertThat(locations).extracting("locationType").contains("AREA", "AREA", "CLAS", "WSHP");
    }

    @Test
    public void testGetAgencyLocationsEventTypeOccur() {
        final var locations = repository.getAgencyLocations("LEI", List.of("OCCUR"), null, null);
        assertThat(locations).extracting("locationType").contains("WING", "WING", "WING", "WING", "WING", "WING");
    }

    @Test
    public void testGetAgencyIepLevels() {
        final var iepLevels = repository.getAgencyIepLevels("LEI");
        assertThat(iepLevels).extracting("iepDescription").contains("Entry", "Basic", "Standard", "Enhanced");
    }

    @Test
    public void testGetAgencyByType() {
        final var agencies = repository.getAgenciesByType("INST");
        assertThat(agencies).extracting("agencyId")
                .contains("BMI", "BXI", "LEI", "MDI", "MUL", "SYI", "TRO", "WAI");

        assertThat(agencies).extracting("agencyType")
                .contains("INST", "INST", "INST", "INST", "INST", "INST", "INST", "INST");
    }


    @Test
    public void testGetAgencyLocationsNoResults1() {
        final var locations = repository.getAgencyLocations("LEI", List.of("OTHER"), null, null);
        assertThat(locations).isEmpty();
    }

    @Test
    public void testGetAgencyLocationsNoResults2() {
        final var locations = repository.getAgencyLocations("doesnotexist", List.of("APP"), null, null);
        assertThat(locations).isEmpty();
    }

    @Test
    public void testGetAgencyLocationsAll() {
        final var locations = repository.getAgencyLocations("LEI", List.of(), null, null);
        assertThat(locations).hasSize(139);
    }

    @Test
    public void testGetAgencyLocationsWithDates() {
        final var locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), null);
        assertThat(locations).hasSize(3);
    }

    @Test
    public void testGetAgencyLocationsWithDatesAM() {
        final var locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), TimeSlot.AM);
        assertThat(locations).hasSize(1);
    }

    @Test
    public void testGetAgencyLocationsWithDatesPM() {
        final var locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), TimeSlot.PM);
        assertThat(locations).hasSize(2);
    }

    @Test
    public void testGetAllPrisonContactDetailsInAgencyIdOrder() {
        final var prisonContactDetailList = repository.getPrisonContactDetails(null);
        assertThat(prisonContactDetailList).extracting("agencyId")
                .containsExactly(
                        "BMI", "BXI", "LEI", "MDI", "MUL", "RNI", "SYI", "TRO", "WAI"
                );
        assertThat(prisonContactDetailList).contains(buildBmiPrisonContactDetails());
    }

    @Test
    public void testGetAllPrisonContactDetailsByAgencyIdMultipleAddressesOnePrimary() {
        final var prisonContactDetailList = repository.getPrisonContactDetails("TRO");
        assertThat(prisonContactDetailList).extracting("agencyId")
                .containsExactly(
                        "TRO"
                );
    }

    @Test
    public void testGetPrisonContactDetailsByAgencyId() {
        final var prisonContactDetails = repository.getPrisonContactDetails("BMI");
        assertThat(prisonContactDetails.get(0)).isEqualTo(buildBmiPrisonContactDetails());
    }

    private PrisonContactDetail buildBmiPrisonContactDetails() {
        return PrisonContactDetail.builder()
                .agencyId("BMI")
                .description("BIRMINGHAM")
                .formattedDescription("Birmingham")
                .addressType("BUS")
                .premise("Birmingham HMP")
                .locality("Ambley")
                .city("Birmingham")
                .country("England")
                .postCode("BM1 23V")
                .phones(ImmutableList.of(Telephone.builder().number("0114 2345345").type("BUS").ext("345").build())).build();
    }

    @Test
    public void testGetPrisonIepReview() {

        val criteria = OffenderIepReviewSearchCriteria.builder()
                .agencyId("LEI")
                .pageRequest(new PageRequest(0L, 10L))
                .build();

        val results = repository.getPrisonIepReview(criteria);

        assertThat(results.getItems()).containsExactly(OFFENDER_1_IEP_REVIEW,
                OFFENDER_2_IEP_REVIEW,
                OFFENDER_3_IEP_REVIEW,
                OFFENDER_4_IEP_REVIEW,
                OFFENDER_5_IEP_REVIEW,
                OFFENDER_6_IEP_REVIEW,
                OFFENDER_7_IEP_REVIEW,
                OFFENDER_8_IEP_REVIEW,
                OFFENDER_9_IEP_REVIEW,
                OFFENDER_10_IEP_REVIEW);
    }

    @Test
    public void testFilterPrisonIepReviewByIepLevel() {

        val criteria = OffenderIepReviewSearchCriteria.builder()
                .agencyId("LEI")
                .iepLevel("Basic")
                .pageRequest(new PageRequest(0L, 1L))
                .build();

        val results = repository.getPrisonIepReview(criteria);

        assertThat(results.getItems()).containsExactly(OFFENDER_6_IEP_REVIEW);
    }

    private static final OffenderIepReview OFFENDER_1_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A5577RS")
            .currentLevel("Standard")
            .provenAdjudications(2)
            .positiveIeps(1)
            .negativeIeps(10)
            .bookingId(-33L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("HAROLD")
            .middleName(null)
            .lastName("LLOYD")
            .cellLocation("LEI-H-1-1")
            .build();


    private static final OffenderIepReview OFFENDER_2_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo(null)
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(9)
            .bookingId(-51L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName(null)
            .middleName(null)
            .lastName(null)
            .cellLocation(null)
            .build();

    private static final OffenderIepReview OFFENDER_3_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A1176RS")
            .currentLevel("Enhanced")
            .provenAdjudications(1)
            .positiveIeps(0)
            .negativeIeps(8)
            .bookingId(-32L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("FRED")
            .middleName(null)
            .lastName("JAMES")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_4_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A5576RS")
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(1)
            .negativeIeps(7)
            .bookingId(-31L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("HARRY")
            .middleName(null)
            .lastName("SARLY")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_5_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A4476RS")
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(6)
            .bookingId(-30L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("NEIL")
            .middleName(null)
            .lastName("SARLY")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_6_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A6676RS")
            .currentLevel("Basic")
            .provenAdjudications(1)
            .positiveIeps(0)
            .negativeIeps(5)
            .bookingId(-29L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("NEIL")
            .middleName("IAN")
            .lastName("BRADLEY")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_7_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A9876RS")
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(4)
            .bookingId(-28L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("RODERICK")
            .middleName(null)
            .lastName("STEWART")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_8_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A9876EC")
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(3)
            .bookingId(-27L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("ERIC")
            .middleName(null)
            .lastName("CLAPTON")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_9_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A1178RS")
            .currentLevel("Standard")
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(2)
            .bookingId(-34L)
            .lastReviewTime(LocalDateTime.of(2017, 9, 6, 9, 44, 1))
            .firstName("FRED")
            .middleName(null)
            .lastName("QUIMBY")
            .cellLocation("LEI-H-1")
            .build();

    private static final OffenderIepReview OFFENDER_10_IEP_REVIEW = OffenderIepReview.builder()
            .offenderNo("A1234AI")
            .currentLevel(null)
            .provenAdjudications(0)
            .positiveIeps(0)
            .negativeIeps(1)
            .bookingId(-9L)
            .lastReviewTime(null)
            .firstName("CHESTER")
            .middleName("JAMES")
            .lastName("THOMPSON")
            .cellLocation("LEI-A-1-5")
            .build();

}
