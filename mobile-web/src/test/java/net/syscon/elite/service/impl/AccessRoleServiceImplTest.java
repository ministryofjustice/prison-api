package net.syscon.elite.service.impl;

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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Test cases for {@link BookingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessRoleServiceImplTest {
    @Mock
    private AccessRoleRepository accessRoleRepository;

    private AccessRoleService accessRoleService;

    @Before
    public void init() {
        accessRoleService = new AccessRoleServiceImpl(accessRoleRepository);
    }

    @Test
    public void testCreateAccessRole() {
        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final AccessRole newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        accessRoleService.createAccessRole(newAccessRole);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).createAccessRole(newAccessRole);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void testCreateAccessRoleAlreadyExists() {
        final AccessRole accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.createAccessRole(accessRole);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateAccessRoleParentNotFound() {
        Mockito.when(accessRoleRepository.getAccessRole("PARENT_NOT_FOUND")).thenReturn(Optional.empty());

        final AccessRole newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").parentRoleCode("PARENT_NOT_FOUND").build();
        accessRoleService.createAccessRole(newAccessRole);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAccessRoleNotFound() {
        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.empty());

        final AccessRole newAccessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();
        accessRoleService.updateAccessRole(newAccessRole);
    }

    @Test()
    public void testUpdateAccessRole() {
        final AccessRole accessRole = AccessRole.builder().roleCode("ROLE_CODE").roleName("ROLE_NAME").build();

        Mockito.when(accessRoleRepository.getAccessRole("ROLE_CODE")).thenReturn(Optional.of(accessRole));

        accessRoleService.updateAccessRole(accessRole);

        Mockito.verify(accessRoleRepository, Mockito.times(1)).updateAccessRole(accessRole);
    }

}
