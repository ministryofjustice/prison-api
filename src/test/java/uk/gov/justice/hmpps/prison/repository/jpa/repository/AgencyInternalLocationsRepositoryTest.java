package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitReferenceCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class AgencyInternalLocationsRepositoryTest {

    @Autowired
    private AgencyInternalLocationRepository repository;

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActive_returnsAllLocations() {
        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SYI", "CELL", true);

        assertThat(locations).extracting("locationId").containsExactlyInAnyOrder(-202L, -204L, -207L);
    }

    @Test
    public void findLocationsByAgencyIdAndLocationTypeAndActive_hydratesReturnObject() {

        final var parentParentLocation = AgencyInternalLocation.builder().locationId(-205L).locationType("WING").agencyId("SYI")
            .currentOccupancy(20).operationalCapacity(20).description("SYI-H").capacity(null)
            .certifiedFlag(true).locationCode("H").active(true).build();

        final var parentLocation = AgencyInternalLocation.builder().locationId(-206L).locationType("LAND").agencyId("SYI").capacity(null)
            .currentOccupancy(20).operationalCapacity(20).description("SYI-H-1").parentLocation(parentParentLocation).userDescription("Landing H/1")
            .certifiedFlag(true).locationCode("1").active(true).build();

        final var expected = AgencyInternalLocation.builder().locationId(-207L).locationType("CELL").agencyId("SYI")
                .currentOccupancy(1).operationalCapacity(1).description("SYI-H-1-1").parentLocation(parentLocation).userDescription("Cell H/1-1")
                .certifiedFlag(true).locationCode("1").active(true).build();

        final var locations = repository.findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActive("SYI", "CELL", true);

        final var actual = locations.stream().filter(l -> l.getLocationId().equals(expected.getLocationId())).findFirst().orElseThrow();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findCellSwapLocation() {
        final var location = repository.findByLocationCodeAndAgencyId("CSWAP", "LEI")
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(location.getDescription()).isEqualTo("LEI-CSWAP");
    }

    @Test
    public void findLivingUnit() {
        final var location = repository.findOneByLocationId(-200L).orElseThrow();

        assertThat(location.getLocationType()).isEqualTo("WING");
        assertThat(location.getLivingUnit()).isEqualTo(new LivingUnitReferenceCode("WING", "Wing"));
    }

}
