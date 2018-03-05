package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffDetail;
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
    public final void testFindUserByStaffId() {
        final StaffDetail staffDetails = repository.findByStaffId(-1L).orElseThrow(new EntityNotFoundException("not found"));

        assertThat(staffDetails.getFirstName()).isEqualTo("Elite2");
        assertThat(staffDetails.getEmail()).isEqualTo("elite2-api-user@syscon.net");
    }

    @Test
    public final void testFindUserByStaffIdNotExists() {
        Optional<StaffDetail> staffDetails = repository.findByStaffId(9999999999L);

        assertThat(staffDetails).isNotPresent();
    }
}
