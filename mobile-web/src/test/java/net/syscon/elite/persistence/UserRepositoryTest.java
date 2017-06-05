package net.syscon.elite.persistence;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("noHikari,nomis")
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
        final UserDetails user = repository.findByUsername("ITAG_USER");
        assertThat(user).isNotNull();
        assertThat(user.getLastName()).isEqualTo("USER");
        assertThat(user.getEmail()).isEqualTo("itaguser@syscon.net");
    }

    @Test
    public final void testFindUserByStaffId() {
        final UserDetails user = repository.findByUsername("ELITE2_API_USER");
        assertThat(user).isNotNull();
        final UserDetails userById = repository.findByStaffId(user.getStaffId());
        assertThat(userById).isNotNull();
    }

    @Test
    public final void testFindRolesByUsername() {
        final List<String> roles = repository.findRolesByUsername("ITAG_USER");
        assertThat(roles).isNotEmpty();
        assertThat(roles).contains("WING_OFF");
    }
}
