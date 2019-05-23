package net.syscon.elite.repository;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.web.config.PersistenceConfigs;
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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AgencyRepositoryTest {

    @Autowired
    private AgencyRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetEnabledAgencyWhenActiveOnly() {
        final var agency = repository.getAgency("LEI", true);
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetEnabledAgencyWithInactive() {
        final var agency = repository.getAgency("LEI", false);
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetDisabledAgencyWhenActiveOnly() {
        final var agency = repository.getAgency("ZZGHI", true);
        assertThat(agency).isEmpty();
    }

    @Test
    public void testGetDisabledAgencyWithInactive() {
        final var agency = repository.getAgency("ZZGHI", false);
        assertThat(agency).isPresent();
    }

    @Test
    public void testGetAgencyLocations() {
        final var locations = repository.getAgencyLocations("LEI", Arrays.asList("APP", "VISIT"), null, null);
        assertThat(locations).extracting("locationType").contains("AREA", "AREA", "CLAS", "WSHP");
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
        final var locations = repository.getAgencyLocations("LEI", Arrays.asList("OTHER"), null, null);
        assertThat(locations).isEmpty();
    }

    @Test
    public void testGetAgencyLocationsNoResults2() {
        final var locations = repository.getAgencyLocations("doesnotexist", Arrays.asList("APP"), null, null);
        assertThat(locations).isEmpty();
    }

    @Test
    public void testGetAgencyLocationsAll() {
        final var locations = repository.getAgencyLocations("LEI", Collections.emptyList(), null, null);
        assertThat(locations).hasSize(40);
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
                        "BMI", "BXI", "LEI", "MDI", "MUL", "SYI", "TRO", "WAI"
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
                .addressType("BUS")
                .premise("Birmingham HMP")
                .locality("Ambley")
                .city("Birmingham")
                .country("England")
                .postCode("BM1 23V")
                .phones(ImmutableList.of(Telephone.builder().number("0114 2345345").type("BUS").ext("345").build())).build();
    }
}
