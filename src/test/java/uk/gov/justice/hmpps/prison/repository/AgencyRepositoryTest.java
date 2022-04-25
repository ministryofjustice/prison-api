package uk.gov.justice.hmpps.prison.repository;

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
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
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
    public void testGetAgencyByType() {
        final var agencies = repository.getAgenciesByType("INST");
        assertThat(agencies).extracting("agencyId")
                .contains("BMI", "BXI", "LEI", "MDI", "MUL", "SYI", "TRO", "WAI");

        assertThat(agencies).extracting("agencyType")
                .contains("INST", "INST", "INST", "INST", "INST", "INST", "INST", "INST");
    }

    @Test
    public void testGetAgencies() {
        final var agencies = repository.getAgencies("agencyId", Order.ASC, 0, 10);
        assertThat(agencies.getItems()).extracting("agencyId").containsAnyOf("ABDRCT", "BMI", "BXI", "COURT1", "LEI", "MDI", "MUL", "RNI", "SYI", "TRO");
        assertThat(agencies.getItems()).extracting("agencyType").containsAnyOf("CRT", "INST", "INST", "CRT", "INST", "INST", "INST", "INST", "INST", "INST");
        assertThat(agencies.getItems()).extracting("active").containsAnyOf(true, true, true, true, true, true, true, true, true, true);
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
    public void testGetAgencyLocationsWhenAllOffendersSuspended() {
        final var locations = repository.getAgencyLocationsBooked("BXI", LocalDate.of(2021, Month.MAY, 1), null);

        assertThat(locations).extracting("locationId").containsExactly(-3001L, -3002L);
    }
}
