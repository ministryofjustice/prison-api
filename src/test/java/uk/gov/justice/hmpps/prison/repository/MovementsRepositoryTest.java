package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class MovementsRepositoryTest {

    @Autowired
    private MovementsRepository repository;

    @Test
    public void canRetrieveAListOfMovementDetails1() {

        final var threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        final var recentMovements = repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.JULY, 16), Collections.emptyList());
        assertThat(recentMovements).hasSize(1); // TAP is excluded
        assertThat(recentMovements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(tuple("Z0024ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 24, 0, 0), "OUT", "LEI", "ADM", "IN"));
    }

    @Test
    public void canRetrieveAListOfMovementDetails2() {
        final var threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0);
        final var recentMovements = repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.AUGUST, 16), Collections.emptyList());
        assertThat(recentMovements).hasSize(2);
        assertThat(recentMovements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(
                        tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0), "LEI", "OUT", "REL", "OUT"),
                        tuple("Z0019ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 19, 0, 0), "LEI", "BMI", "TRN", "OUT"));
    }

    @Test
    public void canRetrieveRollCountCells() {
        final var rollCountList = repository.getRollCount("LEI", "Y", null, false, true);
        assertThat(rollCountList).hasSize(2);
        assertThat(rollCountList).asList()
                .extracting("livingUnitDesc", "parentLocationId", "bedsInUse", "currentlyInCell", "outOfLivingUnits", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
                .contains(
                        tuple("Block A", null, 12, 11, 1, 0, 13, 1, 14, 2, 3),
                        tuple("H", null, 14, 12, 0, 2, 20, 6, 20, 6, 0));
    }

    @Test
    public void canRetrieveRollCountIncludingLandings() {
        final var rollCountList = repository.getRollCount("LEI", "Y", null, false, false);
        assertThat(rollCountList).hasSize(4);
        assertThat(rollCountList).asList()
            .extracting("livingUnitId", "locationType", "fullLocationPath", "locationCode", "livingUnitDesc", "parentLocationId", "bedsInUse", "currentlyInCell", "outOfLivingUnits", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
            .contains(
                tuple(-1L,  "WING", "A",   "A", "Block A",     null, 12, 11, 1, 0, 13, 1, 14, 2, 3),
                tuple(-2L,  "LAND", "A-1", "1", "Landing A/1", -1L,  12, 11, 1, 0, 13, 1, 14, 2, 1),
                tuple(-13L, "WING", "H",   "H", "H",           null, 14, 12, 0, 2, 20, 6, 20, 6, 0),
                tuple(-14L, "LAND", "H-1", "1", "Landing H/1", -13L, 14, 12, 0, 2, 20, 6, 20, 6, 0)
            );
    }

    @Test
    public void canRetrieveRollCountShowingCells() {
        final var rollCountList = repository.getRollCount("LEI", "Y", null, true, false);
        assertThat(rollCountList).hasSize(27);
        assertThat(rollCountList).asList()
            .extracting("livingUnitId", "fullLocationPath", "locationCode", "livingUnitDesc", "bedsInUse", "currentlyInCell", "outOfLivingUnits", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
            .contains(
                tuple(-1L,  "A",   "A", "Block A",     12, 11, 1, 0, 13, 1, 14, 2, 3),
                tuple(-2L,  "A-1", "1", "Landing A/1", 12, 11, 1, 0, 13, 1, 14, 2, 1),
                tuple(-13L, "H",   "H", "H",           14, 12, 0, 2, 20, 6, 20, 6, 0),
                tuple(-14L, "H-1", "1", "Landing H/1", 14, 12, 0, 2, 20, 6, 20, 6, 0)
            );
    }

    @Test
    public void canRetrieveRollCountOfCellsOnALanding() {
        final var rollCountList = repository.getRollCount("LEI", "Y", -2L, true, false);
        assertThat(rollCountList).hasSize(13);
        AbstractListAssert<?, List<? extends Tuple>, Tuple, ObjectAssert<Tuple>> contains = assertThat(rollCountList).asList()
            .extracting("locationType", "fullLocationPath", "locationCode", "livingUnitDesc", "parentLocationId", "bedsInUse", "currentlyInCell", "outOfLivingUnits", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
            .contains(
                tuple("CELL", "A-1-1", "1", "1", -2L, 3, 3, 0, 0, 2, -1, 2, -1, 0),
                tuple("CELL", "A-1-2", "2", "2", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-3", "3", "3", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-4", "4", "4", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-5", "5", "5", -2L, 1, 0, 1, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-6", "6", "6", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-7", "7", "7", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-8", "8", "8", -2L, 0, 0, 0, 0, 1, 1, 1, 1, 0),
                tuple("CELL", "A-1-9", "9", "9", -2L, 0, 0, 0, 0, 1, 1, 1, 1, 0),
                tuple("CELL", "A-1-10", "10", "10", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0),
                tuple("CELL", "A-1-1001", "BROKEN", "Broken", -2L, 0, 0, 0, 0, 1, 1, 1, 1, 1),
                tuple("CELL", "A-1-1002", "REPURPOSED", "Repurposed", -2L, 0, 0, 0, 0, 0, 0, 1, 1, 0),
                tuple("CELL", "AABCW-1", "THE_ROOM", "The_room", -2L, 1, 1, 0, 0, 1, 0, 1, 0, 0)
            );
    }

    @Test
    public void canRetrieveRollCountUnassigned() {
        final var rollCountList = repository.getRollCount("LEI", "N", null, false, true);
        assertThat(rollCountList).hasSize(1);
        assertThat(rollCountList).asList()
                .extracting("livingUnitDesc", "bedsInUse", "currentlyInCell", "outOfLivingUnits", "currentlyOut", "operationalCapacity", "netVacancies", "maximumCapacity", "availablePhysical", "outOfOrder")
                .contains(
                        tuple("Chapel", 0, 0, 0, 0, null, null, null, null, 0));
    }

    @Test
    public void canRetrieveRollCountMovements1() {
        final var movementCount = repository.getMovementCount("LEI", LocalDate.of(2017, Month.JULY, 16));
        assertThat(movementCount.getIn()).isEqualTo(1);
        assertThat(movementCount.getOut()).isEqualTo(1);
    }

    @Test
    public void canRetrieveRollCountMovements2() {
        final var movementCount = repository.getMovementCount("LEI", LocalDate.of(2012, Month.JULY, 5));
        assertThat(movementCount.getIn()).isEqualTo(5);
        assertThat(movementCount.getOut()).isEqualTo(0);
    }

    @Test
    public void canRetrieveRollCountMovements3() {
        final var movementCount = repository.getMovementCount("LEI", LocalDate.of(2018, Month.FEBRUARY, 2));
        assertThat(movementCount.getIn()).isEqualTo(0);
        assertThat(movementCount.getOut()).isEqualTo(0);
    }

    @Test
    public void canRetrieveRecentMovementsByOffendersAndMovementTypes() {
        final var movements = repository.getMovementsByOffenders(List.of("A6676RS"), List.of("TRN"), true, false);

        assertThat(movements).extracting(Movement::getToAgency).containsExactly("MDI");
    }

    @Test
    public void canRetrieveMovementsByOffendersAndMovementTypes() {
        final var movements = repository.getMovementsByOffenders(List.of("A6676RS"), List.of("TRN"), false, false);

        assertThat(movements).extracting(Movement::getToAgency).containsOnly("BMI", "MDI");
    }

    @Test
    public void canRetrieveRecentMovementsByOffenders() {
        final var movements = repository.getMovementsByOffenders(List.of("A6676RS"), List.of(), true, false);

        assertThat(movements).extracting(Movement::getToCity).containsExactly("Wadhurst");
    }

    @Test
    public void canRetrieveMovementsByOffenders() {
        final var movements = repository.getMovementsByOffenders(List.of("A6676RS"), List.of(), false, false);

        assertThat(movements).extracting(Movement::getFromAgency).containsOnly("BMI", "LEI");
    }

    @Test
    public void canRetrieveEnRouteOffenderMovements() {
        final var movements = repository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2017, 10, 12));

        assertThat(movements).extracting(OffenderMovement::getOffenderNo).containsOnly("A1183SH", "A1183AD");
    }

    @Test
    public void canRetrieveEnRouteOffenderCount() {
        final var count = repository.getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2017, 10, 12));

        assertThat(count).isEqualTo(2);
    }

    @Test
    public void canRetrieveOffendersIn() {
        final var offendersIn = repository.getOffendersIn("LEI", LocalDate.of(2017, 10, 12));

        assertThat(offendersIn).containsExactlyInAnyOrder(
                OffenderIn.builder()
                        .offenderNo("A6676RS")
                        .bookingId(-29L)
                        .dateOfBirth(LocalDate.of(1945, 1, 10))
                        .firstName("NEIL")
                        .lastName("BRADLEY")
                        .fromAgencyDescription("BIRMINGHAM")
                        .toAgencyDescription("LEEDS")
                        .fromAgencyId("BMI")
                        .toAgencyId("LEI")
                        .movementDateTime(LocalDateTime.of(2017, 10, 12, 10, 45, 0))
                        .movementTime(LocalTime.of(10, 45, 0))
                        .location("Landing H/1")
                        .build()
        );
    }

    @Test
    public void canRetrieveOffendersOutForAGivenDate() {
        final var offender = OffenderMovement.builder()
            .offenderNo("Z0020ZZ")
            .dateOfBirth(LocalDate.of(1966, 1, 1))
            .firstName("BURT")
            .lastName("REYNOLDS")
            .fromAgency("LEI")
            .directionCode("OUT")
            .movementTime(LocalTime.of(0, 0))
            .movementDate(LocalDate.of(2017, 7, 16))
            .movementReasonDescription("Funerals And Deaths")
            .build();

        assertThat(repository.getOffendersOut("LEI", LocalDate.of(2017, Month.JULY, 16), null)).containsExactly(offender);
    }

    @Test
    public void canRetrieveOffendersOutForAGivenDateAndMoveType() {
        final var offender = OffenderMovement.builder()
            .offenderNo("Z0020ZZ")
            .dateOfBirth(LocalDate.of(1966, 1, 1))
            .firstName("BURT")
            .lastName("REYNOLDS")
            .fromAgency("LEI")
            .directionCode("OUT")
            .movementTime(LocalTime.of(0, 0))
            .movementDate(LocalDate.of(2017, 7, 16))
            .movementReasonDescription("Funerals And Deaths")
            .build();

        assertThat(repository.getOffendersOut("LEI", LocalDate.of(2017, Month.JULY, 16), "TAP")).containsExactly(offender);
        assertThat(repository.getOffendersOut("LEI", LocalDate.of(2017, Month.JULY, 16), "REL")).isEmpty();
    }

    @Test
    public void canRetrieveOffendersInReception() {
        final var offenders = repository.getOffendersInReception("MDI");

        assertThat(offenders).containsExactly(
                OffenderInReception.builder()
                        .firstName("AMY")
                        .lastName("DUDE")
                        .offenderNo("A1181DD")
                        .bookingId(-46L)
                        .dateOfBirth(LocalDate.of(1980, 1, 2))
                        .build()
        );
    }

    @Test
    public void canRetrieveOffendersCurrentlyOut() {
        final var offenders = repository.getOffendersCurrentlyOut(-13);
        assertThat(offenders).containsExactlyInAnyOrder(
                OffenderOut.builder().offenderNo("Z0025ZZ").bookingId(-25L).dateOfBirth(LocalDate.of(1974, 1, 1)).firstName("MATTHEW").lastName("SMITH").location("Landing H/1").build(),
                OffenderOut.builder().offenderNo("Z0024ZZ").bookingId(-24L).dateOfBirth(LocalDate.of(1958, 1, 1)).firstName("LUCIUS").lastName("FOX").location("Landing H/1").build()
        );
    }

    @Test
    public void canRetrieveOffendersCurrentlyOutOfAgency() {
        final var offenders = repository.getOffendersCurrentlyOut("LEI");
        assertThat(offenders).containsExactlyInAnyOrder(
                OffenderOut.builder().offenderNo("Z0025ZZ").bookingId(-25L).dateOfBirth(LocalDate.of(1974, 1, 1)).firstName("MATTHEW").lastName("SMITH").location("Landing H/1").build(),
                OffenderOut.builder().offenderNo("Z0024ZZ").bookingId(-24L).dateOfBirth(LocalDate.of(1958, 1, 1)).firstName("LUCIUS").lastName("FOX").location("Landing H/1").build()
        );
    }

    @Test
    public void canRetrieveRecentMoves_byMovementTypes() {
        final var threshold = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0, 0);
        final var recentMovements = repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.AUGUST, 16), List.of("TRN"));
        assertThat(recentMovements).hasSize(1);
        assertThat(recentMovements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(tuple("Z0019ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 19, 0, 0), "LEI", "BMI", "TRN", "OUT"));
    }

    @Test
    public void canRetrieveMovementsForAgenciesFromTo() {

        final var fromTime = LocalDateTime.of(2019, Month.MAY, 1, 11, 0, 0);
        final var toTime = LocalDateTime.of(2019, Month.MAY, 1, 17, 0, 0);
        final var agencies = List.of("LEI", "MDI");

        final var movements = repository.getCompletedMovementsForAgencies(agencies, fromTime, toTime);

        assertThat(movements).asList()
                .extracting("offenderNo", "fromAgency", "toAgency")
                .contains(tuple("Z0018ZZ", "LEI", "BMI"))
                .contains(tuple("A9876EC", "BMI", "MDI"))
                .contains(tuple("A1179MT", "MDI", "LEI"));
    }

    @Test
    public void canRetrieveCompletedMovementsByAgency() {

        final var fromTime = LocalDateTime.of(2019, Month.MAY, 1, 11, 0, 0);
        final var toTime = LocalDateTime.of(2019, Month.MAY, 1, 17, 0, 0);

        // Agencies not present in seeded data
        final var agencies = List.of("XXX", "YYY");
        final var movements = repository.getCompletedMovementsForAgencies(agencies, fromTime, toTime);
        assertThat(movements).asList().isEmpty();
    }

    @Test
    public void canRetrieveCourtEventsByAgency() {

        final var fromTime = LocalDateTime.of(2017, Month.OCTOBER, 16, 17, 0, 0);
        final var toTime = LocalDateTime.of(2017, Month.OCTOBER, 16, 20, 0, 0);
        final var agencies = List.of("LEI");

        final var courtEvents = repository.getCourtEvents(agencies, fromTime, toTime);

        assertThat(courtEvents).isNotEmpty();
        assertThat(courtEvents).asList()
                .extracting("offenderNo", "fromAgency", "toAgency", "eventClass", "eventType", "eventStatus", "directionCode")
                .contains(tuple("A1234AG", "LEI", "LEI", "EXT_MOV", "CRT", "COMP", "IN"));
    }

    @Test
    public void canRetrieveReleaseEventsByAgency() {

        final var fromTime = LocalDateTime.of(2022, Month.FEBRUARY, 2, 0, 0, 0);
        final var toTime = LocalDateTime.of(2022, Month.FEBRUARY, 2, 23, 59, 59);
        final var agencies = List.of("LEI", "MDI");

        final var releaseEvents = repository.getOffenderReleases(agencies, fromTime, toTime);

        assertThat(releaseEvents).isNotEmpty();
        assertThat(releaseEvents).asList()
                .extracting("eventClass", "eventStatus", "movementTypeCode", "movementTypeDescription", "offenderNo", "movementReasonCode")
                .contains(tuple("EXT_MOV", "SCH", "REL", "Release", "Z0024ZZ", "DD"));
    }

    @Test
    public void getIndividualSchedules() {
        // Match with specific rows loaded in the seeded data
        final var agencies = List.of("LEI", "MDI");
        final var transferEvents = repository.getIndividualSchedules(agencies, LocalDate.now());

        assertThat(transferEvents)
            .extracting("eventClass", "eventStatus", "eventType", "offenderNo", "fromAgency", "toAgency")
            .contains(tuple("EXT_MOV", "SCH", "TRN", "A1234AC", "LEI", "MDI"))
            .contains(tuple("EXT_MOV", "SCH", "TRN", "A1234AC", "MDI", "LEI"));
    }

}
