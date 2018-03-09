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
        final StaffDetail staffDetails = repository.findByStaffId(-1L).orElseThrow(new EntityNotFoundException("not found"));

        assertThat(staffDetails.getFirstName()).isEqualTo("Elite2");
        assertThat(staffDetails.getEmail()).isEqualTo("elite2-api-user@syscon.net");
    }

    @Test
    public void testFindStaffDetailsByInvalidStaffId() {
        Optional<StaffDetail> staffDetails = repository.findByStaffId(9999999999L);

        assertThat(staffDetails).isNotPresent();
    }

    @Test
    public void testFindStaffLocationRolesByRole() {
        final String TEST_AGENCY = "SYI";
        final String TEST_ROLE = "KW";

        Page<StaffLocationRole> page = repository.findStaffByAgencyRole(TEST_AGENCY, TEST_ROLE, null, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-9);
        assertThat(slr.getFirstName()).isEqualTo("Wing");
        assertThat(slr.getLastName()).isEqualTo("Officer4");
        assertThat(slr.getEmail()).isNull();
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
    public void testFindStaffLocationRolesByPositionAndRole() {
        final String TEST_AGENCY = "LEI";
        final String TEST_POSITION = "PRO";
        final String TEST_ROLE = "OS";

        Page<StaffLocationRole> page = repository.findStaffByAgencyPositionRole(TEST_AGENCY, TEST_POSITION, TEST_ROLE, null, new PageRequest());

        List<StaffLocationRole> items = page.getItems();

        assertThat(items.size()).isEqualTo(1);

        StaffLocationRole slr = items.get(0);

        assertThat(slr.getAgencyId()).isEqualTo(TEST_AGENCY);
        assertThat(slr.getStaffId()).isEqualTo(-2);
        assertThat(slr.getFirstName()).isEqualTo("API");
        assertThat(slr.getLastName()).isEqualTo("User");
        assertThat(slr.getEmail()).isEqualTo("itaguser@syscon.net");
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
}
