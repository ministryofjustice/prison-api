package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class AgencyInternalLocationsRepositoryTest {

    @Autowired
    private AgencyInternalLocationRepository repository;

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActiveFlag_returnsAllLocations() {
        final var expected = AgencyInternalLocation.builder().locationId(-202L).locationType("CELL").agencyId("SYI")
                .currentOccupancy(2).operationalCapacity(2).description("SYI-A-1-1").parentLocationId(-2L).userDescription("Cell A/1-1")
                .activeFlag(ActiveFlag.Y).build();

        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SYI", "CELL", ActiveFlag.Y);

        assertThat(locations).extracting("locationId").containsExactlyInAnyOrder(-202L, -204L, -207L);
    }

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActiveFlag_hydratesReturnObject() {
        final var expected = AgencyInternalLocation.builder().locationId(-202L).locationType("CELL").agencyId("SYI")
                .currentOccupancy(2).operationalCapacity(2).description("SYI-A-1-1").parentLocationId(-2L).userDescription("Cell A/1-1")
                .locationCode("1").activeFlag(ActiveFlag.Y).build();

        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SYI", "CELL", ActiveFlag.Y);

        final var actual = locations.stream().filter(l -> l.getLocationId().equals(expected.getLocationId())).findFirst().get();
        assertThat(actual).isEqualTo(expected);
    }


}
