package net.syscon.elite.persistence;

import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.web.api.model.StaffDetails;
import net.syscon.elite.web.api.model.UserDetails;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis,nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    public final void testFindUserByUsername() {
        UserDetails user = repository.findByUsername("itag_user").orElseThrow(new EntityNotFoundException("not found"));

        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("itaguser@syscon.net");
    }

    @Test
    public final void testFindUserByUsernameNotExists() {
        Optional<UserDetails> user = repository.findByUsername("XXXXXXXX");
        assertThat(user).isNotPresent();
    }

    @Test
    public final void testFindUserByStaffId() {
        UserDetails user = repository.findByUsername("elite2_api_user").orElseThrow(new EntityNotFoundException("not found"));

        final StaffDetails staffDetails = repository.findByStaffId(user.getStaffId()).orElseThrow(new EntityNotFoundException("not found"));

        assertThat(staffDetails.getFirstName()).isEqualTo("Elite2");
        assertThat(staffDetails.getEmail()).isEqualTo("elite2-api-user@syscon.net");
    }

    @Test
    public final void testFindUserByStaffIdNotExists() {
        Optional<StaffDetails> staffDetails = repository.findByStaffId(9999999999L);

        assertThat(staffDetails).isNotPresent();
    }

    @Test
    public final void testFindRolesByUsername() {
        List<String> roles = repository.findRolesByUsername("itag_user");

        assertThat(roles).isNotEmpty();
        assertThat(roles).contains("WING_OFF");
    }
}
