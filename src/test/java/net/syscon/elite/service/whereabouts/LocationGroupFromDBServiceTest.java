package net.syscon.elite.service.whereabouts;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.service.support.LocationProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class LocationGroupFromDBServiceTest {

    private final Location L1 = Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("BLOCK A").internalLocationCode("A").build();
    private final Location L2 = Location.builder().locationId(-13L).locationType("WING").description("LEI-H").internalLocationCode("H").build();

    private final Location SL1 = Location.builder().locationId(-14L).locationType("LAND").description("LEI-H-1").parentLocationId(-13L).userDescription("LANDING H/1").internalLocationCode("1").build();
    private final Location SL2 = Location.builder().locationId(-2L).locationType("LAND").description("LEI-A-1").parentLocationId(-1L).userDescription("LANDING A/1").internalLocationCode("1").build();
    private final Location SL3 = Location.builder().locationId(-32L).locationType("LAND").description("LEI-A-2").parentLocationId(-1L).userDescription("LANDING A/2").internalLocationCode("2").build();

    private final Location CELL_A_1 = Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-1-001").parentLocationId(-32L).build();
    private final Location CELL_AA_1 = Location.builder().locationId(-320L).locationType("CELL").description("LEI-AA-1-001").parentLocationId(-32L).build();
    private final Location CELL_A_3 = Location.builder().locationId(-320L).locationType("CELL").description("LEI-A-3-001").parentLocationId(-32L).build();
    private final Location CELL_B_1 = Location.builder().locationId(-320L).locationType("CELL").description("LEI-B-2-001").parentLocationId(-32L).build();

    private final LocationGroup LG1 = LocationGroup.builder().key("A").name("Block A").build();
    private final LocationGroup LG2 = LocationGroup.builder().key("H").name("H").build();

    private final LocationGroup SLG2 = LocationGroup.builder().key("1").name("Landing A/1").build();
    private final LocationGroup SLG3 = LocationGroup.builder().key("2").name("Landing A/2").build();

    @Mock
    private LocationRepository repository;

    @InjectMocks
    private LocationGroupFromDBService service;

    @Test
    public void noGroups() {
        assertThat(service.getLocationGroups("LEI")).isEmpty();
    }

    @Test
    public void oneGroup() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1));
        assertThat(service.getLocationGroups("LEI")).contains(LG1);
    }

    @Test
    public void twoGroups() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L2, L1));
        assertThat(service.getLocationGroups("LEI")).containsExactly(LG1, LG2);
    }

    @Test
    public void oneGroupOneSubGroupRemoved() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1));
        when(repository.getSubLocationGroupData(Set.of(-1L))).thenReturn(List.of(SL2));
        assertThat(service.getLocationGroups("LEI")).contains(LG1);
    }

    @Test
    public void oneGroupTwoSubGroups() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1));
        when(repository.getSubLocationGroupData(Set.of(-1L))).thenReturn(List.of(SL3, SL2));
        assertThat(service.getLocationGroups("LEI")).contains(
                LG1.toBuilder().children(List.of(SLG2, SLG3)).build()
        );
    }

    @Test
    public void twoGroupWithSubGroups() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1, L2));
        when(repository.getSubLocationGroupData(Set.of(-1L, -13L))).thenReturn(List.of(SL1, SL2, SL3));
        assertThat(service.getLocationGroups("LEI")).contains(
                LG1.toBuilder().children(List.of(SLG2, SLG3)).build(),
                LG2
        );
    }

    @Test
    public void locationGroupFilters() {
        final var filter = service.locationGroupFilter("LEI", "A");
        assertThat(locationStream(CELL_A_1, CELL_A_3, CELL_B_1, CELL_AA_1).filter(filter))
                .containsExactlyInAnyOrder(locationStream(CELL_A_1, CELL_A_3).toArray(Location[]::new));
    }

    private static Stream<Location> locationStream(final Location... locations) {
        return Stream
                .of(locations)
                .map(LocationProcessor::processLocation); // Munge description into locationPrefix... Yuk.
    }
}
