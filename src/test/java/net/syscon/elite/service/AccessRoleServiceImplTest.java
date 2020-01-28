package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.repository.AccessRoleRepository;
import net.syscon.elite.service.AccessRoleService;
import net.syscon.elite.service.EntityAlreadyExistsException;
import net.syscon.elite.service.EntityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Test cases for {@link AccessRoleService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessRoleServiceImplTest {
    @Mock
    private AccessRoleRepository accessRoleRepository;

    private AccessRoleService accessRoleService;

    @Before
    public void init() {
        accessRoleService = new AccessRoleService(accessRoleRepository);
    }

    @Test
    public void testCreateAccessRole() {
        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        final var defaultedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("GENERAL").build();
        accessRoleService.createAccessRole(newAccessRole);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).createAccessRole(defaultedAccessRole);
    }

    @Test
    public void testCreateAccessRoleWithAdminRoleFunction() {
        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ADMIN").build();
        accessRoleService.createAccessRole(newAccessRole);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).createAccessRole(newAccessRole);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void testCreateAccessRoleAlreadyExists() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.createAccessRole(accessRole);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateAccessRoleParentNotFound() {
        Mockito.when(accessRoleRepository.getAccessRole("PARENT_NOT_FOUND")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").parentRoleCode("PARENT_NOT_FOUND").build();
        accessRoleService.createAccessRole(newAccessRole);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAccessRoleNotFound() {
        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final var newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        accessRoleService.updateAccessRole(newAccessRole);
    }

    @Test()
    public void testUpdateAccessRole() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRole);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).updateAccessRole(accessRole);
    }

    @Test()
    public void testUpdateAccessRoleNameOnly() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ROLE_FUNCTION").build();
        final var accessRoleIn = AccessRole.builder().roleCode("ROLE_CODE").roleName("NEW_ROLE_NAME").build();
        final var populatedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("NEW_ROLE_NAME").roleFunction("ROLE_FUNCTION").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRoleIn);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).updateAccessRole(populatedAccessRole);
    }

    @Test()
    public void testUpdateAccessRoleFunctionOnly() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("ROLE_FUNCTION").build();
        final var accessRoleIn = AccessRole.builder().roleCode("ROLE_CODE").roleFunction("NEW_ROLE_FUNCTION").build();
        final var populatedAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").roleFunction("NEW_ROLE_FUNCTION").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRoleIn);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).updateAccessRole(populatedAccessRole);
    }

    @Test
    public void testGetAccessRoles() {
        final var accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        Mockito.when(accessRoleRepository.getAccessRoles(true)).thenReturn(ImmutableList.of(accessRole));

        accessRoleService.getAccessRoles(true);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).getAccessRoles(true);
    }

}
