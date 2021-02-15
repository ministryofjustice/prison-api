package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class AgencyInternalLocationsRepositoryTest {

    @Autowired
    private AgencyInternalLocationRepository repository;

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActiveFlag_returnsAllLocations() {
        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SYI", "CELL", ActiveFlag.Y);

        assertThat(locations).extracting("locationId").containsExactlyInAnyOrder(-202L, -204L, -207L);
    }

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActiveFlag_hydratesReturnObject() {

        final var parentParentLocation = AgencyInternalLocation.builder().locationId(-205L).locationType("WING").agencyId("SYI")
            .currentOccupancy(20).operationalCapacity(20).description("SYI-H").capacity(null)
            .certifiedFlag(ActiveFlag.Y).locationCode("H").activeFlag(ActiveFlag.Y).build();

        final var parentLocation = AgencyInternalLocation.builder().locationId(-206L).locationType("LAND").agencyId("SYI").capacity(null)
            .currentOccupancy(20).operationalCapacity(20).description("SYI-H-1").parentLocation(parentParentLocation).userDescription("Landing H/1")
            .certifiedFlag(ActiveFlag.Y).locationCode("1").activeFlag(ActiveFlag.Y).build();

        final var expected = AgencyInternalLocation.builder().locationId(-207L).locationType("CELL").agencyId("SYI")
                .currentOccupancy(1).operationalCapacity(1).description("SYI-H-1-1").parentLocation(parentLocation).userDescription("Cell H/1-1")
                .certifiedFlag(ActiveFlag.Y).locationCode("1").activeFlag(ActiveFlag.Y).build();

        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag("SYI", "CELL", ActiveFlag.Y);

        final var actual = locations.stream().filter(l -> l.getLocationId().equals(expected.getLocationId())).findFirst().get();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findCellSwapLocation() {
        final var location = repository.findByLocationCodeAndAgencyId("CSWAP", "LEI")
                .stream()
                .findFirst()
                .get();

        assertThat(location.getDescription()).isEqualTo("LEI-CSWAP");
    }

}
