package net.syscon.elite.repository;

import net.syscon.elite.api.model.AccessRole;
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
public class AccessRoleRepositoryTest {

    public static final String NEW_ROLE_CODE = "NEW_ROLE_CODE";
    public static final String NEW_ROLE_CODE_2 = "NEW_ROLE_CODE2";
    public static final String NEW_ROLE_NAME = "NEW_ROLE_NAME";
    public static final String EXISTING_ROLE_CODE = "WING_OFF";
    @Autowired
    private AccessRoleRepository repository;

    @Test
    public void testCreateAccessRole() {
        AccessRole role = AccessRole.builder()
                .roleCode(NEW_ROLE_CODE)
                .roleName(NEW_ROLE_NAME)
                .parentRoleCode(EXISTING_ROLE_CODE)
                .build();

        repository.createAccessRole(role);

        Optional<AccessRole> optionalRole = repository.getAccessRole(NEW_ROLE_CODE);

        assertThat(optionalRole.isPresent()).isTrue();

        AccessRole accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
        assertThat(accessRole.getParentRoleCode()).isEqualTo(EXISTING_ROLE_CODE);
    }

    @Test
    public void testCreateAccessRoleNoParentRole() {
        AccessRole role = AccessRole.builder()
                .roleCode(NEW_ROLE_CODE_2)
                .roleName(NEW_ROLE_NAME)
                .build();

        repository.createAccessRole(role);

        Optional<AccessRole> optionalRole = repository.getAccessRole(NEW_ROLE_CODE_2);

        assertThat(optionalRole.isPresent()).isTrue();

        AccessRole accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
        assertThat(accessRole.getParentRoleCode()).isNull();
    }

    @Test
    public void testUpdateAccessRole() {
        AccessRole role = AccessRole.builder()
                .roleCode(EXISTING_ROLE_CODE)
                .roleName(NEW_ROLE_NAME)
                .build();

        repository.updateAccessRole(role);

        Optional<AccessRole> optionalRole = repository.getAccessRole(EXISTING_ROLE_CODE);

        assertThat(optionalRole.isPresent()).isTrue();

        AccessRole accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
    }

}
