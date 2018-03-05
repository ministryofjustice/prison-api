package net.syscon.elite.repository;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
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
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    public final void testFindUserByUsername() {
        UserDetail user = repository.findByUsername("ITAG_USER").orElseThrow(new EntityNotFoundException("not found"));

        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("itaguser@syscon.net");
    }

    @Test
    public final void testFindUserByUsernameNotExists() {
        Optional<UserDetail> user = repository.findByUsername("XXXXXXXX");
        assertThat(user).isNotPresent();
    }

    @Test
    public final void testFindRolesByUsername() {
        List<UserRole> roles = repository.findRolesByUsername("ITAG_USER", null);
        assertThat(roles).isNotEmpty();
        assertThat(roles).extracting("roleCode").contains("LEI_WING_OFF");
    }
}
