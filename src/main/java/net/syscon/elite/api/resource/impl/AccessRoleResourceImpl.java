package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.resource.AccessRoleResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.AccessRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/access-roles")
public class AccessRoleResourceImpl implements AccessRoleResource {
    private final AccessRoleService accessRoleService;

    public AccessRoleResourceImpl(final AccessRoleService accessRoleService) {
        this.accessRoleService = accessRoleService;
    }

    @Override
    public List<AccessRole> getAccessRoles(final boolean includeAdmin) {
        return accessRoleService.getAccessRoles(includeAdmin);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> createAccessRole(final AccessRole newAccessRole) {
        accessRoleService.createAccessRole(newAccessRole);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void>  updateAccessRole(final AccessRole accessRole) {
        accessRoleService.updateAccessRole(accessRole);
        return ResponseEntity.ok().build();
    }

}
