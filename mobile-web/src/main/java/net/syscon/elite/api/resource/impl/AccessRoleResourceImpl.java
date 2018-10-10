package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.resource.AccessRoleResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AccessRoleService;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/access-roles")
public class AccessRoleResourceImpl implements AccessRoleResource{
    private final AccessRoleService accessRoleService;

    public AccessRoleResourceImpl(AccessRoleService accessRoleService) {
        this.accessRoleService = accessRoleService;
    }


    @Override
    public GetAccessRolesResponse getAccessRoles(boolean includeAdmin) {

        final List<AccessRole> accessRoles = accessRoleService.getAccessRoles(includeAdmin);
        return GetAccessRolesResponse.respond200WithApplicationJson(accessRoles);
    }

    @Override
    public CreateAccessRoleResponse createAccessRole(AccessRole newAccessRole) {
        accessRoleService.createAccessRole(newAccessRole);

        return CreateAccessRoleResponse.respond201WithApplicationJson();
    }

    @Override
    public UpdateAccessRoleResponse updateAccessRole(AccessRole accessRole) {
        accessRoleService.updateAccessRole(accessRole);

        return UpdateAccessRoleResponse.respond200WithApplicationJson();
    }

}
