package net.syscon.elite.service.impl.whereabouts;


import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationGroupService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class LocationGroupFromPropertiesServiceTest {

    private Properties groupsProperties;
    private LocationGroupService service;

    @Before
    public void initialiseTest() {
        groupsProperties = new Properties();
        service = new LocationGroupFromPropertiesService(groupsProperties);
    }

    @Test
    public void whenGivenNoGroupsThenShouldReturnEmptyListForAnyAgency() {
        assertThat(service.getLocationGroupsForAgency("MDI")).isEmpty();
    }

    @Test
    public void whenGivenOneGroupForOneAgencyThenGetLocationGroupsForThatAgencyShouldReturnTheGroup() {
        groupsProperties.setProperty("MDI_1", "*");
        assertThat(service.getLocationGroupsForAgency("MDI")).containsExactly(group("1"));
    }

    @Test
    public void whenGivenOneGroupForOneAgencyThenGetLocationGroupsForAnotherAgencyShouldReturnEmptyList() {
        groupsProperties.setProperty("MDI_1", "*");
        assertThat(service.getLocationGroupsForAgency("LEI")).isEmpty();
    }

    @Test
    public void whenGivenOneGroupPerAgencyLookupsShouldWorkCorrectly() {
        groupsProperties.setProperty("MDI_1", "A");
        groupsProperties.setProperty("LDS_A", "B");
        assertThat(service.getLocationGroupsForAgency("MDI")).containsExactly(group("1"));
        assertThat(service.getLocationGroupsForAgency("LDS")).containsExactly(group("A"));
    }

    @Test
    public void whenGivenMultipleGroupsPerAgencyLookupsShouldWorkCorrectly() {
        groupsProperties.setProperty("MDI_1", "A1");
        groupsProperties.setProperty("MDI_2", "A2");
        groupsProperties.setProperty("MDI_3", "A3");

        groupsProperties.setProperty("LDS_A", "B1");
        groupsProperties.setProperty("LDS_B", "B2");
        groupsProperties.setProperty("LDS_C", "B3");

        assertThat(service.getLocationGroupsForAgency("MDI")).containsExactly(group("1"), group("2"), group("3"));
        assertThat(service.getLocationGroupsForAgency("LDS")).containsExactly(group("A"), group("B"), group("C"));
    }

    @Test
    public void whenGivenOneGroupHavingOneSubGroupThenALookUpReturnsACorrectlyConfiguredLocationGroup() {
        groupsProperties.setProperty("MDI_1", "A");
        groupsProperties.setProperty("MDI_1_A", "AA");
        assertThat(service.getLocationGroupsForAgency("MDI")).containsExactly(group("1", "A"));
    }

    @Test
    public void whenGivenOneGroupHavingManySubGroupsThenALookUpReturnsACorrectlyConfiguredLocationGroup() {
        groupsProperties.setProperty("MDI_1_A", "AA");
        groupsProperties.setProperty("MDI_1_C", "AC");
        groupsProperties.setProperty("MDI_1", "A");
        groupsProperties.setProperty("MDI_1_B", "AB");
        groupsProperties.setProperty("MDI_1_D-D", "AD");

        assertThat(service.getLocationGroupsForAgency("MDI")).containsExactly(group("1", "A", "B", "C", "D-D"));
    }

    @Test
    public void whenThereAreManyGroupsAndManySubGroupsThenTheLookupReturnsTheCorrectRepresentation() {
        groupsProperties.setProperty("MDI_1", "");
        groupsProperties.setProperty("MDI_1_A", "");
        groupsProperties.setProperty("MDI_1_B", "");
        groupsProperties.setProperty("MDI_1_C", "");
        groupsProperties.setProperty("MDI_1_D-D", "");

        groupsProperties.setProperty("MDI_2", "");

        groupsProperties.setProperty("MDI_3", "");
        groupsProperties.setProperty("MDI_3_X", "");
        groupsProperties.setProperty("MDI_3_Y", "");
        groupsProperties.setProperty("MDI_3_Z", "");

        groupsProperties.setProperty("LDS_1", "");
        groupsProperties.setProperty("LDS_1_F", "");
        groupsProperties.setProperty("LDS_1_B", "");
        groupsProperties.setProperty("LDS_1_C", "");
        groupsProperties.setProperty("LDS_1_D-D", "");

        assertThat(service.getLocationGroupsForAgency("MDI"))
                .containsExactly(
                        group("1", "A", "B", "C", "D-D"),
                        group("2"),
                        group("3", "X", "Y", "Z")
                );

        assertThat(service.getLocationGroupsForAgency("LDS"))
                .containsExactly(
                        group("1", "B", "C", "D-D", "F")
                );

        assertThat(service.getLocationGroupsForAgency("")).isEmpty();
        assertThat(service.getLocationGroupsForAgency("XXX")).isEmpty();
    }

    @Test
    public void whenThereAreSpacesInLocationAndSubLoationNamesThenLookupReturnsTheCorrectRepresentation() {
        groupsProperties.setProperty("HLI_A Wing", "HLI-A-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 1", "HLI-A-1-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 2", "HLI-A-2-.+");

        groupsProperties.setProperty("HLI_B Wing", "HLI-B-.+");
        groupsProperties.setProperty("MDI_5", "MDI-5-.-A-.+,MDI-5-.-B-.+");

        assertThat(service.getLocationGroupsForAgency("HLI"))
                .containsExactly(
                        group("A Wing", "Landing 1", "Landing 2"),
                        group("B Wing")
                );
    }


    @Test
    public void givenFixedPatternThenPredicateMatchesThatPattern() {
        groupsProperties.setProperty("MDI_1", "1");

        final var predicates = service.locationGroupFilter("MDI", "1");

        assertThat(applyPredicatesToLocations(predicates, "1", "11", "X", "MDI-1-1-01")).containsExactly("1");
    }

    @Test
    public void givenMutlipleGroupsThenPatternsAreAssignedToCorrectGroups() {
        groupsProperties.setProperty("MDI_1", "1");
        groupsProperties.setProperty("MDI_2", "2");

        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "1"), "1", "2")).containsExactly("1");
        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "2"), "1", "2")).containsExactly("2");
    }

    @Test
    public void givenFixedPatternAssignedToSubGroupThenPredicateMatchesThatPattern() {
        groupsProperties.setProperty("MDI_1_A", "1");

        final var predicates = service.locationGroupFilter("MDI", "1_A");

        assertThat(applyPredicatesToLocations(predicates, "1", "11", "2")).containsExactly("1");
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenCriteriaDoNotMatchAgencyThenEntityNotFoundExceptionIsThrown() {
        groupsProperties.setProperty("MDI_1_A", "1");
        service.locationGroupFilter("XXX", "1");
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenCriteriaDoNotMatchGroupThenEntityNotFoundExceptionIsThrown() {
        groupsProperties.setProperty("MDI_1_A", "1");
        service.locationGroupFilter("MDI", "2");
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenCriteriaDoNotMatchSubGroupThenEntityNotFoundExceptionIsThrown() {
        groupsProperties.setProperty("MDI_1", "1");
        service.locationGroupFilter("MDI", "1_A");
    }

    @Test
    public void givenFixedPatternsThenPredicateMatchesThosePatterns() {
        groupsProperties.setProperty("MDI_1", "1|X|PQR|M");

        final var predicates = service.locationGroupFilter("MDI", "1");

        assertThat(applyPredicatesToLocations(predicates, "PQR", "11", "X", "XY", "1", "PQ", "Z", "")).containsExactly("PQR", "X", "1");
    }

    @Test
    public void givenASequenceOfFixedPatternsThenPredicateMatchesThosePatterns() {
        groupsProperties.setProperty("MDI_1", "1,X,PQR,M");

        final var predicates = service.locationGroupFilter("MDI", "1");

        assertThat(applyPredicatesToLocations(predicates, "PQR", "11", "X", "XY", "1", "PQ", "Z", "")).containsExactlyInAnyOrder("1", "X", "PQR");
    }


    @Test(expected = PatternSyntaxException.class)
    public void givenInvalidPatternThenRequestingFilterThrowsException() {
        groupsProperties.setProperty("MDI_1", "1|[");

        service.locationGroupFilter("MDI", "1");
    }

    @Test
    public void givenPatternsUsedForMDIThenCellsAreMatchedCorrectly() {
        groupsProperties.setProperty("MDI_1", "MDI-1-.+");
        groupsProperties.setProperty("MDI_2", "MDI-2-.+");
        groupsProperties.setProperty("MDI_1_A", "MDI-1-1-0(0[1-9]|1[0-2]),MDI-1-2-0(0[1-9]|1[0-2]),MDI-1-3-0(0[1-9]|1[0-2])");
        groupsProperties.setProperty("MDI_1_B", "MDI-1-1-0(1[3-9]|2[0-6]),MDI-1-2-0(1[3-9]|2[0-6]),MDI-1-3-0(1[3-9]|2[0-6])");
        groupsProperties.setProperty("MDI_1_C", "MDI-1-1-0(2[7-9]|3[0-8]),MDI-1-2-0(2[7-9]|3[0-8]),MDI-1-3-0(2[7-9]|3[0-8])");
        groupsProperties.setProperty("MDI_2_A", "MDI-2-1-0(0[1-9]|1[0-2]),MDI-2-2-0(0[1-9]|1[0-2]),MDI-2-3-0(0[1-9]|1[0-2])");
        groupsProperties.setProperty("MDI_2_B", "MDI-2-1-0(1[3-9]|2[0-6]),MDI-2-2-0(1[3-9]|2[0-6]),MDI-2-3-0(1[3-9]|2[0-6])");
        groupsProperties.setProperty("MDI_2_C", "MDI-2-1-0(2[7-9]|3[0-8]),MDI-2-2-0(2[7-9]|3[0-8]),MDI-2-3-0(2[7-9]|3[0-8])");

        final String[] ONE_A_PREFIXES = {
                "MDI-1-1-001",
                "MDI-1-1-002",
                "MDI-1-1-003",
                "MDI-1-1-004",
                "MDI-1-1-005",
                "MDI-1-1-006",
                "MDI-1-1-007",
                "MDI-1-1-008",
                "MDI-1-1-009",
                "MDI-1-1-010",
                "MDI-1-1-011",
                "MDI-1-1-012",

                "MDI-1-2-001",
                "MDI-1-2-002",
                "MDI-1-2-003",
                "MDI-1-2-004",
                "MDI-1-2-005",
                "MDI-1-2-006",
                "MDI-1-2-007",
                "MDI-1-2-008",
                "MDI-1-2-009",
                "MDI-1-2-010",
                "MDI-1-2-011",
                "MDI-1-2-012",

                "MDI-1-3-001",
                "MDI-1-3-002",
                "MDI-1-3-003",
                "MDI-1-3-004",
                "MDI-1-3-005",
                "MDI-1-3-006",
                "MDI-1-3-007",
                "MDI-1-3-008",
                "MDI-1-3-009",
                "MDI-1-3-010",
                "MDI-1-3-011",
                "MDI-1-3-012"

        };

        final String[] ONE_B_PREFIXES = {
                "MDI-1-1-013",
                "MDI-1-1-014",
                "MDI-1-1-015",
                "MDI-1-1-016",
                "MDI-1-1-017",
                "MDI-1-1-018",
                "MDI-1-1-019",
                "MDI-1-1-020",
                "MDI-1-1-021",
                "MDI-1-1-022",
                "MDI-1-1-023",
                "MDI-1-1-024",
                "MDI-1-1-025",
                "MDI-1-1-026",

                "MDI-1-2-013",
                "MDI-1-2-014",
                "MDI-1-2-015",
                "MDI-1-2-016",
                "MDI-1-2-017",
                "MDI-1-2-018",
                "MDI-1-2-019",
                "MDI-1-2-020",
                "MDI-1-2-021",
                "MDI-1-2-022",
                "MDI-1-2-023",
                "MDI-1-2-024",
                "MDI-1-2-025",
                "MDI-1-2-026",

                "MDI-1-3-013",
                "MDI-1-3-014",
                "MDI-1-3-015",
                "MDI-1-3-016",
                "MDI-1-3-017",
                "MDI-1-3-018",
                "MDI-1-3-019",
                "MDI-1-3-020",
                "MDI-1-3-021",
                "MDI-1-3-022",
                "MDI-1-3-023",
                "MDI-1-3-024",
                "MDI-1-3-025",
                "MDI-1-3-026"


        };

        final String[] ONE_C_PREFIXES = {
                "MDI-1-1-027",
                "MDI-1-1-028",
                "MDI-1-1-029",
                "MDI-1-1-030",
                "MDI-1-1-031",
                "MDI-1-1-032",
                "MDI-1-1-033",
                "MDI-1-1-034",
                "MDI-1-1-035",
                "MDI-1-1-036",
                "MDI-1-1-037",
                "MDI-1-1-038",

                "MDI-1-2-027",
                "MDI-1-2-028",
                "MDI-1-2-029",
                "MDI-1-2-030",
                "MDI-1-2-031",
                "MDI-1-2-032",
                "MDI-1-2-033",
                "MDI-1-2-034",
                "MDI-1-2-035",
                "MDI-1-2-036",
                "MDI-1-2-037",
                "MDI-1-2-038",

                "MDI-1-3-027",
                "MDI-1-3-028",
                "MDI-1-3-029",
                "MDI-1-3-030",
                "MDI-1-3-031",
                "MDI-1-3-032",
                "MDI-1-3-033",
                "MDI-1-3-034",
                "MDI-1-3-035",
                "MDI-1-3-036",
                "MDI-1-3-037",
                "MDI-1-3-038"

        };

        final String[] extraPrefixes = {
                "MDI-1-1-039"
        };


        final var locationPrefixes = Stream.of(ONE_A_PREFIXES, ONE_B_PREFIXES, ONE_C_PREFIXES, extraPrefixes).flatMap(Stream::of).toArray(String[]::new);

        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "1"), locationPrefixes)).containsExactly(locationPrefixes);
        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "1_A"), locationPrefixes)).containsExactly(ONE_A_PREFIXES);
        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "1_B"), locationPrefixes)).containsExactly(ONE_B_PREFIXES);
        assertThat(applyPredicatesToLocations(service.locationGroupFilter("MDI", "1_C"), locationPrefixes)).containsExactly(ONE_C_PREFIXES);
    }

    @Test
    public void givenLocationNameWithSpaces() {

        groupsProperties.setProperty("HLI_A Wing", "HLI-A-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 1", "HLI-A-1-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 2", "HLI-A-2-.+");

        groupsProperties.setProperty("HLI_B Wing", "HLI-B-.+");
        groupsProperties.setProperty("MDI_5", "MDI-5-.-A-.+,MDI-5-.-B-.+");

        final var predicates = service.locationGroupFilter("HLI", "A Wing");

        assertThat(applyPredicatesToLocations(predicates, "HLI-A-1-001", "HLI-A-2-001", "HLI-B-1-001")).containsExactly("HLI-A-1-001", "HLI-A-2-001");
    }

    @Test
    public void givenLocationAndSubLocationNameWithSpaces() {

        groupsProperties.setProperty("HLI_A Wing", "HLI-A-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 1", "HLI-A-1-.+");
        groupsProperties.setProperty("HLI_A Wing_Landing 2", "HLI-A-2-.+");

        groupsProperties.setProperty("HLI_B Wing", "HLI-B-.+");
        groupsProperties.setProperty("MDI_5", "MDI-5-.-A-.+,MDI-5-.-B-.+");

        final var predicates = service.locationGroupFilter("HLI", "A Wing_Landing 2");

        assertThat(applyPredicatesToLocations(predicates, "HLI-A-1-001", "HLI-A-2-001", "HLI-B-1-001")).containsExactly("HLI-A-2-001");
    }

    private static LocationGroup group(final String name) {
        return new LocationGroup(name, name, emptyList());
    }

    private static LocationGroup group(final String name, final String... subGroupNames) {
        return new LocationGroup(name, name, Arrays.stream(subGroupNames).map(LocationGroupFromPropertiesServiceTest::group).collect(Collectors.toList()));
    }

    private static Location location(final String locationPrefix) {
        final var location = new Location();
        location.setLocationPrefix(locationPrefix);
        return location;
    }

    private static List<String> applyPredicatesToLocations(final Predicate<Location> predicate, final String... locationPrefixes) {
        return Arrays.stream(locationPrefixes)
                .map(LocationGroupFromPropertiesServiceTest::location)
                .filter(predicate)
                .map(Location::getLocationPrefix)
                .collect(Collectors.toList());
    }
}
