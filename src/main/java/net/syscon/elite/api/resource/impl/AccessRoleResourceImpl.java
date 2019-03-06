package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.resource.AccessRoleResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AccessRoleService;

import javax.ws.rs.Path;

@RestResource
@Path("/access-roles")
public class AccessRoleResourceImpl implements AccessRoleResource{
    private final AccessRoleService accessRoleService;

    public AccessRoleResourceImpl(final AccessRoleService accessRoleService) {
        this.accessRoleService = accessRoleService;
    }


    @Override
    public GetAccessRolesResponse getAccessRoles(final boolean includeAdmin) {

        final var accessRoles = accessRoleService.getAccessRoles(includeAdmin);
        return GetAccessRolesResponse.respond200WithApplicationJson(accessRoles);
    }

    @Override
    public CreateAccessRoleResponse createAccessRole(final AccessRole newAccessRole) {
        accessRoleService.createAccessRole(newAccessRole);

        return CreateAccessRoleResponse.respond201WithApplicationJson();
    }

    @Override
    public UpdateAccessRoleResponse updateAccessRole(final AccessRole accessRole) {
        accessRoleService.updateAccessRole(accessRole);

        return UpdateAccessRoleResponse.respond200WithApplicationJson();
    }

}
