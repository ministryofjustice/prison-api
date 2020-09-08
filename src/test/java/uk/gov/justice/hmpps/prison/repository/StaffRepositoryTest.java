package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class StaffRepositoryTest {

    @Autowired
    private StaffRepository repository;

    @Test
    public void testFindStaffDetailsByValidStaffId() {
        final Long testStaffId = -1L;

        final var staffDetail = repository.findByStaffId(testStaffId)
                .orElseThrow(EntityNotFoundException.withId(testStaffId));

        assertThat(staffDetail.getFirstName()).isEqualTo("Prison");
    }

    @Test
    public void testFindStaffDetailsByInvalidStaffId() {
        final var staffDetail = repository.findByStaffId(9999999999L);

        assertThat(staffDetail).isNotPresent();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierInvalidIdentifier() {
        final var staffDetail = repository.findStaffByPersonnelIdentifier("X", "X");

        assertThat(staffDetail).isNotPresent();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierWrongIdType() {
        final var testIdType = "SYS2";
        final var testId = "sysuser@system1.com";

        assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierWrongId() {
        final var testIdType = "SYS1";
        final var testId = "sysuser@system2.com";

        assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierDuplicateIdentifier() {
        final var testIdType = "SYS9";
        final var testId = "sysuser@system9.com";

        assertThat(repository.findStaffByPersonnelIdentifier(testIdType, testId)).isEmpty();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierActive() {
        final var testIdType = "SYS1";
        final var testId = "sysuser@system1.com";

        final var staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
                .orElseThrow(EntityNotFoundException.withId(testId));

        assertThat(staffDetail.getStaffId()).isEqualTo(-1L);
        assertThat(staffDetail.getFirstName()).isEqualTo("Prison");
        assertThat(staffDetail.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    public void testFindStaffByPersonnelIdentifierInactive() {
        final var testIdType = "ITAG";
        final var testId = "ex.officer5@itag.com";

        final var staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
                .orElseThrow(EntityNotFoundException.withId(testId));

        assertThat(staffDetail.getStaffId()).isEqualTo(-10L);
        assertThat(staffDetail.getFirstName()).isEqualTo("Ex");
        assertThat(staffDetail.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    public void testFindStaffLocationRolesByRole() {
        final var TEST_AGENCY = "SYI";
        final var TEST_AGENCY_DESCRIPTION = "Shrewsbury";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, null, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(1);

        final var slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getAgencyDescription()).isEqualTo(TEST_AGENCY_DESCRIPTION);
        assertThat(slr.getStaffId()).isEqualTo(-9);
        assertThat(slr.getFirstName()).isEqualTo("Wing");
        assertThat(slr.getLastName()).isEqualTo("Officer4");
        assertThat(slr.getFromDate()).isEqualTo(LocalDate.of(2018, 1, 2));
        assertThat(slr.getToDate()).isNull();
        assertThat(slr.getPosition()).isEqualTo("AO");
        assertThat(slr.getPositionDescription()).isEqualTo("Admin Officer");
        assertThat(slr.getRole()).isEqualTo(TEST_ROLE);
        assertThat(slr.getRoleDescription()).isEqualTo("Key Worker");
        assertThat(slr.getScheduleType()).isEqualTo("FT");
        assertThat(slr.getScheduleTypeDescription()).isEqualTo("Full Time");
        assertThat(slr.getHoursPerWeek()).isEqualTo(BigDecimal.valueOf(11).setScale(2));
    }

    @Test
    public void testFindStaffLocationRolesByRole_handles_special_character() {
        final var TEST_AGENCY = "LEI";
        final var TEST_ROLE = "KW";
        final var NAME_FILTER = "O'brien";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, NAME_FILTER, null, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(1);

        final var slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-12);
        assertThat(slr.getFirstName()).isEqualTo("Jo");
        assertThat(slr.getLastName()).isEqualTo("O'brien");
        assertThat(slr.getPosition()).isEqualTo("AO");
        assertThat(slr.getRoleDescription()).isEqualTo("Key Worker");
    }

    @Test
    public void testFindStaffLocationRolesByPositionAndRole() {
        final var TEST_AGENCY = "LEI";
        final var TEST_POSITION = "PRO";
        final var TEST_AGENCY_DESCRIPTION = "Leeds";
        final var TEST_ROLE = "OS";

        final var page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE, null, null, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(1);

        final var slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getAgencyDescription()).isEqualTo(TEST_AGENCY_DESCRIPTION);
        assertThat(slr.getStaffId()).isEqualTo(-2);
        assertThat(slr.getFirstName()).isEqualTo("API");
        assertThat(slr.getLastName()).isEqualTo("User");
        // assertThat(slr.getEmail()).isEqualTo("itaguser@syscon.net");
        assertThat(slr.getFromDate()).isEqualTo(LocalDate.of(2016, 8, 8));
        assertThat(slr.getToDate()).isNull();
        assertThat(slr.getPosition()).isEqualTo("PRO");
        assertThat(slr.getPositionDescription()).isEqualTo("Prison Officer");
        assertThat(slr.getRole()).isEqualTo("OS");
        assertThat(slr.getRoleDescription()).isEqualTo("Offender Supervisor");
        assertThat(slr.getScheduleType()).isEqualTo("FT");
        assertThat(slr.getScheduleTypeDescription()).isEqualTo("Full Time");
        assertThat(slr.getHoursPerWeek()).isEqualTo(BigDecimal.valueOf(7).setScale(2));
    }

    @Test
    public void testFindStaffLocationRolesByStaffId() {
        final var TEST_AGENCY = "LEI";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -1L, true,
                new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(1);

        final var slr = items.get(0);
        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-1);
        assertThat(slr.getRole()).isEqualTo(TEST_ROLE);
    }

    @Test
    public void testFindStaffLocationRolesByStaffIdActiveOnly() {
        final var TEST_AGENCY = "SYI";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -10L, true,
                new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(0);
    }

    @Test
    public void testFindStaffLocationRolesByStaffIdDontIncludeInactive() {
        final var TEST_AGENCY = "SYI";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -10L, false,
                new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(0);
    }

    @Test
    public void testFindStaffLocationPositionRolesByStaffId() {
        final var TEST_AGENCY = "LEI";
        final var TEST_POSITION = "AO";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE,
                null, -4L, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(1);

        final var slr = items.get(0);
        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-4);
    }

    @Test
    public void testFindStaffLocationRolesByInvalidStaffId() {
        final var TEST_AGENCY = "LEI";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE,
                null, -999999L, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(0);
    }

    @Test
    public void testFindStaffLocationPositionRolesByInvalidStaffId() {
        final var TEST_AGENCY = "LEI";
        final var TEST_POSITION = "AO";
        final var TEST_ROLE = "KW";

        final var page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE,
                null, -999999L, true, new PageRequest());

        final var items = page.getItems();

        assertThat(items).hasSize(0);
    }

    @Test
    public void testEmailAddresses() {
        final Long validStaffId = -1L;

        final var staffDetail = repository.findByStaffId(validStaffId)
                .orElseThrow(EntityNotFoundException.withId(validStaffId));
        assertThat(staffDetail).isNotNull();

        // The data has a single email address configured for this user
        final var staffEmails = repository.findEmailAddressesForStaffId(validStaffId);
        assertThat(staffEmails).containsOnly("prison-api-user@test.com");
    }

    @Test
    public void testNoEmailAddresses() {
        final var validStaffId = -3L;

        final var staffDetail = repository.findByStaffId(validStaffId);
        assertThat(staffDetail).isPresent();

        // The data has no email addresses for this staff member
        final var staffEmails = repository.findEmailAddressesForStaffId(validStaffId);
        assertThat(staffEmails).isEmpty();
    }
}
