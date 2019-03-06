package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
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

        final StaffDetail staffDetail = repository.findByStaffId(testStaffId)
                .orElseThrow(EntityNotFoundException.withId(testStaffId));

        assertThat(staffDetail.getFirstName()).isEqualTo("Elite2");
    }

    @Test
    public void testFindStaffDetailsByInvalidStaffId() {
        Optional<StaffDetail> staffDetail = repository.findByStaffId(9999999999L);

        assertThat(staffDetail).isNotPresent();
    }

    @Test
    public void testFindStaffByPersonnelIdentifierInvalidIdentifier() {
        Optional<StaffDetail> staffDetail = repository.findStaffByPersonnelIdentifier("X", "X");

        assertThat(staffDetail).isNotPresent();
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindStaffByPersonnelIdentifierWrongIdType() {
        final String testIdType = "SYS2";
        final String testId = "sysuser@system1.com";

        repository.findStaffByPersonnelIdentifier(testIdType, testId).orElseThrow(EntityNotFoundException.withId(testId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindStaffByPersonnelIdentifierWrongId() {
        final String testIdType = "SYS1";
        final String testId = "sysuser@system2.com";

        repository.findStaffByPersonnelIdentifier(testIdType, testId).orElseThrow(EntityNotFoundException.withId(testId));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindStaffByPersonnelIdentifierDuplicateIdentifier() {
        final String testIdType = "SYS9";
        final String testId = "sysuser@system9.com";

        repository.findStaffByPersonnelIdentifier(testIdType, testId).orElseThrow(EntityNotFoundException.withId(testId));
    }

    @Test
    public void testFindStaffByPersonnelIdentifierActive() {
        final String testIdType = "SYS1";
        final String testId = "sysuser@system1.com";

        StaffDetail staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
                .orElseThrow(EntityNotFoundException.withId(testId));

        assertThat(staffDetail.getStaffId()).isEqualTo(-1L);
        assertThat(staffDetail.getFirstName()).isEqualTo("Elite2");
        assertThat(staffDetail.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    public void testFindStaffByPersonnelIdentifierInactive() {
        final String testIdType = "ITAG";
        final String testId = "ex.officer5@itag.com";

        StaffDetail staffDetail = repository.findStaffByPersonnelIdentifier(testIdType, testId)
                .orElseThrow(EntityNotFoundException.withId(testId));

        assertThat(staffDetail.getStaffId()).isEqualTo(-10L);
        assertThat(staffDetail.getFirstName()).isEqualTo("Ex");
        assertThat(staffDetail.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    public void testFindStaffLocationRolesByRole() {
        final String TEST_AGENCY = "SYI";
        final String TEST_AGENCY_DESCRIPTION = "Shrewsbury";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, null, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getAgencyDescription()).isEqualTo(TEST_AGENCY_DESCRIPTION);
        assertThat(slr.getStaffId()).isEqualTo(-9);
        assertThat(slr.getFirstName()).isEqualTo("Wing");
        assertThat(slr.getLastName()).isEqualTo("Officer4");
        assertThat(slr.getFromDate()).isEqualTo(LocalDate.of(2018,1,2));
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
        final String TEST_AGENCY = "LEI";
        final String TEST_ROLE = "KW";
        final String NAME_FILTER = "O'brien";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, NAME_FILTER, null, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-12);
        assertThat(slr.getFirstName()).isEqualTo("Jo");
        assertThat(slr.getLastName()).isEqualTo("O'brien");
        assertThat(slr.getPosition()).isEqualTo("AO");
        assertThat(slr.getRoleDescription()).isEqualTo("Key Worker");
    }

    @Test
    public void testFindStaffLocationRolesByPositionAndRole() {
        final String TEST_AGENCY = "LEI";
        final String TEST_POSITION = "PRO";
        final String TEST_AGENCY_DESCRIPTION = "Leeds";
        final String TEST_ROLE = "OS";

        Page<StaffLocationRole> page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE, null, null, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getAgencyDescription()).isEqualTo(TEST_AGENCY_DESCRIPTION);
        assertThat(slr.getStaffId()).isEqualTo(-2);
        assertThat(slr.getFirstName()).isEqualTo("API");
        assertThat(slr.getLastName()).isEqualTo("User");
        // assertThat(slr.getEmail()).isEqualTo("itaguser@syscon.net");
        assertThat(slr.getFromDate()).isEqualTo(LocalDate.of(2016,8,8));
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
        final String TEST_AGENCY = "LEI";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -1L, true,
                new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);
        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-1);
        assertThat(slr.getRole()).isEqualTo(TEST_ROLE);
    }

    @Test
    public void testFindStaffLocationRolesByStaffIdActiveOnly() {
        final String TEST_AGENCY = "SYI";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -10L, true,
                new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(0);
    }

    @Test
    public void testFindStaffLocationRolesByStaffIdDontIncludeInactive() {
        final String TEST_AGENCY = "SYI";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, -10L, false,
                new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(0);
    }

    @Test
    public void testFindStaffLocationPositionRolesByStaffId() {
        final String TEST_AGENCY = "LEI";
        final String TEST_POSITION = "AO";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE,
                null, -4L, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);
        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-4);
    }

    @Test
    public void testFindStaffLocationRolesByInvalidStaffId() {
        final String TEST_AGENCY = "LEI";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE,
                null, -999999L, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(0);
    }

    @Test
    public void testFindStaffLocationPositionRolesByInvalidStaffId() {
        final String TEST_AGENCY = "LEI";
        final String TEST_POSITION = "AO";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE,
                null, -999999L, true, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(0);
    }

    @Test
    public void testEmailAddresses() {
        final Long validStaffId = -1L;

        StaffDetail staffDetail = repository.findByStaffId(validStaffId)
                .orElseThrow(EntityNotFoundException.withId(validStaffId));
        assertThat(staffDetail).isNotNull();

        // The data has a single email address configured for this user
        final var staffEmails = repository.findEmailAddressesForStaffId(validStaffId);
        assertThat(staffEmails).containsOnly("elite2-api-user@syscon.net");
    }

    @Test
    public void testNoEmailAddresses() {
        final long validStaffId = -3L;

        Optional<StaffDetail> staffDetail = repository.findByStaffId(validStaffId);
        assertThat(staffDetail).isPresent();

        // The data has no email addresses for this staff member
        final var staffEmails = repository.findEmailAddressesForStaffId(validStaffId);
        assertThat(staffEmails).isEmpty();
    }
}
