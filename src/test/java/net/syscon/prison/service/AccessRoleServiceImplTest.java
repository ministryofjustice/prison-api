package net.syscon.prison.service;

import com.google.common.collect.ImmutableList;
import net.syscon.prison.api.model.AccessRole;
import net.syscon.prison.repository.AccessRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link AccessRoleService}.
 */
@ExtendWith(MockitoExtension.class)
public class AccessRoleServiceImplTest {
    @Mock
    private AccessRoleRepository accessRoleRepository;

    private AccessRoleService accessRoleService;

    @BeforeEach
    public void init() {
        accessRoleService = new AccessRoleService(accessRoleRepository);
    }

    @Test
    public void testCreateAccessRole() {
        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        final var defaultedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("GENERAL").build();
        accessRoleService.createAccessRole(newAccessRole);

        verify(accessRoleRepository).createAccessRole(defaultedAccessRole);
    }

    @Test
    public void testCreateAccessRoleWithAdminRoleFunction() {
        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ADMIN").build();
        accessRoleService.createAccessRole(newAccessRole);

        verify(accessRoleRepository).createAccessRole(newAccessRole);
    }

    @Test
    public void testCreateAccessRoleAlreadyExists() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        assertThatThrownBy(() -> accessRoleService.createAccessRole(accessRole)).isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    public void testCreateAccessRoleParentNotFound() {
        when(accessRoleRepository.getAccessRole("PARENT_NOT_FOUND")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").parentRoleCode("PARENT_NOT_FOUND").build();
        assertThatThrownBy(() -> accessRoleService.createAccessRole(newAccessRole)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testUpdateAccessRoleNotFound() {
        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        assertThatThrownBy(() -> accessRoleService.updateAccessRole(newAccessRole)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testUpdateAccessRole() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRole);

        verify(accessRoleRepository).updateAccessRole(accessRole);
    }

    @Test()
    public void testUpdateAccessRoleNameOnly() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ROLE_FUNCTION").build();
        final var accessRoleIn = AccessRole.builder().roleCode("ROLE_CODE").roleName("NEW_ROLE_NAME").build();
        final var populatedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("NEW_ROLE_NAME").roleFunction("ROLE_FUNCTION").build();

        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRoleIn);

        verify(accessRoleRepository).updateAccessRole(populatedAccessRole);
    }

    @Test()
    public void testUpdateAccessRoleFunctionOnly() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ROLE_FUNCTION").build();
        final var accessRoleIn = AccessRole.builder().roleCode("ROLE_CODE").roleFunction("NEW_ROLE_FUNCTION").build();
        final var populatedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("NEW_ROLE_FUNCTION").build();

        when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRoleIn);

        verify(accessRoleRepository).updateAccessRole(populatedAccessRole);
    }

    @Test
    public void testGetAccessRoles() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        when(accessRoleRepository.getAccessRoles(true)).thenReturn(ImmutableList.of(accessRole));

        accessRoleService.getAccessRoles(true);

        verify(accessRoleRepository).getAccessRoles(true);
    }

}
