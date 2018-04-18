package net.syscon.elite.repository;

import net.syscon.elite.service.support.CustodyStatusDto;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.hamcrest.Matchers;
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

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class CustodyStatusRepositoryTest {

    @Autowired
    private CustodyStatusRepository repository;

    private final String offenderWithMovementRecord = "Z0023ZZ";
    private final String offenderWithHistoricalMovementRecords = "Z0017ZZ";
    private final String offenderWithNoMovementRecord = "Z0022ZZ";
    private final String offenderInGhostPrison = "Z0026ZZ";
    private final String offenderThatDoesNotExist = "O0000OO";

    @Before
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void canRetrieveAListOfCustodyStatusDetails() {
        assertThat("A list containing results is returned", repository.listCustodyStatuses(LocalDate.now()).size(), Matchers.greaterThan(0));
    }

    @Test
    public final void canRetrieveCustodyStatusDetails() {
        assertTrue("A CustodyStatusDto is returned", repository.getCustodyStatus(offenderWithMovementRecord, LocalDate.now()).isPresent());
    }

    @Test
    public final void canFailGracefully() {
        assertFalse("A CustodyStatusDto is NOT returned", repository.getCustodyStatus(offenderThatDoesNotExist, LocalDate.now()).isPresent());
    }

    @Test
    public final void canRetrieveCustodyStatusDetailsForASpecificIdentifier() {
        assertEquals(
                String.format("The record for Offender %s is returned", offenderWithMovementRecord),
                offenderWithMovementRecord,
                repository.getCustodyStatus(offenderWithMovementRecord, LocalDate.now()).map(x -> x.getOffenderIdDisplay()).get());
    }

    @Test
    public final void includesBookingFlagsWithinTheRetrievedData() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(offenderWithMovementRecord, LocalDate.now()).orElse(null);

        assertNotNull("Includes Booking StatusFlag", custodyStatusDto.getBookingStatus());
        assertNotNull("Includes Booking ActiveFlag", custodyStatusDto.getActiveFlag());
    }

    @Test
    public final void includesMovementDetailsWithinTheRetrievedDataWhenMovementDetailsAreAvailable() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(offenderWithMovementRecord, LocalDate.now()).orElse(null);

        assertNotNull("Includes Movement DirectionCode", custodyStatusDto.getDirectionCode());
        assertNotNull("Includes Movement TypeCode", custodyStatusDto.getMovementType());
        assertNotNull("Includes Movement ReasonCode", custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void includesNullValuesWithinTheRetrievedDataWhenMovementDetailsAreNotAvailable() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(offenderWithNoMovementRecord, LocalDate.now()).orElse(null);

        assertNull("Includes Null for DirectionCode", custodyStatusDto.getDirectionCode());
        assertNull("Includes Null for MovementType", custodyStatusDto.getMovementType());
        assertNull("Includes Null for MovementReasonCode", custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void cannotRetrieveCustodyStatusDetailsForAnOffenderLocatedInTheGhostPrison() {
        assertFalse("A CustodyStatusDto is NOT returned", repository.getCustodyStatus(offenderInGhostPrison, LocalDate.now()).isPresent());
    }

    @Test
    public final void includesMostRecentMovementDetailsWithinTheRetrievedDataWhenNoDateIsPassed() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(offenderWithMovementRecord, LocalDate.now()).orElse(null);

        assertNotNull("Includes Movement DirectionCode", custodyStatusDto.getDirectionCode());
        assertNotNull("Includes Movement TypeCode", custodyStatusDto.getMovementType());
        assertNotNull("Includes Movement ReasonCode", custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void includesMostRecentMovementDetailsWithinTheRetrievedDataWhenNowIsProvided() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(
                offenderWithHistoricalMovementRecords, LocalDate.now()).orElse(null);

        assertEquals("Includes Movement DirectionCode", "OUT", custodyStatusDto.getDirectionCode());
        assertEquals("Includes Movement TypeCode", "TAP", custodyStatusDto.getMovementType());
        assertEquals("Includes Movement ReasonCode", "C6", custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void includesHistoricalMovementDetailsWithinTheRetrievedDataWhenAPassedDateIsProvided() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus(
                offenderWithHistoricalMovementRecords, LocalDate.of(2016, 7,15)).orElse(null);

        assertEquals("Includes Movement DirectionCode", "IN", custodyStatusDto.getDirectionCode());
        assertEquals("Includes Movement TypeCode", "TAP", custodyStatusDto.getMovementType());
        assertEquals("Includes Movement ReasonCode", "C3", custodyStatusDto.getMovementReasonCode());
    }

}