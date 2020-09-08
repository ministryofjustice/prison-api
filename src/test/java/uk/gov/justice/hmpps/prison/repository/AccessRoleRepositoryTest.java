package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AccessRoleRepositoryTest {

    public static final String NEW_ROLE_CODE = "NEW_ROLE_CODE";
    public static final String NEW_ROLE_CODE_2 = "NEW_ROLE_CODE2";
    public static final String NEW_ROLE_CODE_3 = "NEW_ROLE_CODE3";
    public static final String NEW_ROLE_NAME = "NEW_ROLE_NAME";
    public static final String ROLE_FUNCTION_ADMIN = "ADMIN";
    public static final String ROLE_FUNCTION_GENERAL = "GENERAL";
    public static final String EXISTING_ROLE_CODE = "WING_OFF";
    @Autowired
    private AccessRoleRepository repository;

    @Test
    public void testCreateAccessRole() {
        final var role = AccessRole.builder()
                .roleCode(NEW_ROLE_CODE)
                .roleName(NEW_ROLE_NAME)
                .roleFunction(ROLE_FUNCTION_GENERAL)
                .parentRoleCode(EXISTING_ROLE_CODE)
                .build();

        repository.createAccessRole(role);

        final var optionalRole = repository.getAccessRole(NEW_ROLE_CODE);

        assertThat(optionalRole.isPresent()).isTrue();

        final var accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
        assertThat(accessRole.getParentRoleCode()).isEqualTo(EXISTING_ROLE_CODE);
        assertThat(accessRole.getRoleFunction()).isEqualTo(ROLE_FUNCTION_GENERAL);
    }

    @Test
    public void testCreateAdminAccessRole() {
        final var role = AccessRole.builder()
                .roleCode(NEW_ROLE_CODE_3)
                .roleName(NEW_ROLE_NAME)
                .parentRoleCode(EXISTING_ROLE_CODE)
                .roleFunction(ROLE_FUNCTION_ADMIN)
                .build();

        repository.createAccessRole(role);

        final var optionalRole = repository.getAccessRole(NEW_ROLE_CODE_3);

        assertThat(optionalRole.isPresent()).isTrue();

        final var accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
        assertThat(accessRole.getParentRoleCode()).isEqualTo(EXISTING_ROLE_CODE);
        assertThat(accessRole.getRoleFunction()).isEqualTo(ROLE_FUNCTION_ADMIN);
    }

    @Test
    public void testCreateAccessRoleNoParentRole() {
        final var role = AccessRole.builder()
                .roleCode(NEW_ROLE_CODE_2)
                .roleName(NEW_ROLE_NAME)
                .roleFunction(ROLE_FUNCTION_ADMIN)
                .build();

        repository.createAccessRole(role);

        final var optionalRole = repository.getAccessRole(NEW_ROLE_CODE_2);

        assertThat(optionalRole.isPresent()).isTrue();

        final var accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
        assertThat(accessRole.getParentRoleCode()).isNull();
    }

    @Test
    public void testUpdateAccessRole() {
        final var role = AccessRole.builder()
                .roleCode(EXISTING_ROLE_CODE)
                .roleName(NEW_ROLE_NAME)
                .roleFunction("ADMIN")
                .build();

        repository.updateAccessRole(role);

        final var optionalRole = repository.getAccessRole(EXISTING_ROLE_CODE);

        assertThat(optionalRole.isPresent()).isTrue();

        final var accessRole = optionalRole.get();

        assertThat(accessRole.getRoleName()).isEqualTo(NEW_ROLE_NAME);
    }

    @Test
    public void testGetAccessRolesForAdmin() {

        final var accessRoles = repository.getAccessRoles(true);

        assertThat(accessRoles).extracting("roleCode").contains("ACCESS_ROLE_GENERAL", "ACCESS_ROLE_ADMIN");
    }

    @Test
    public void testGetAccessRolesForGeneral() {

        final var accessRoles = repository.getAccessRoles(false);

        assertThat(accessRoles).extracting("roleCode").contains("ACCESS_ROLE_GENERAL");
    }

}
