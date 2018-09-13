package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.repository.AccessRoleRepository;
import net.syscon.elite.service.AccessRoleService;
import net.syscon.elite.service.EntityAlreadyExistsException;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Validated
public class AccessRoleServiceImpl implements AccessRoleService {
    private final AccessRoleRepository accessRoleRepository;

    public AccessRoleServiceImpl(AccessRoleRepository accessRoleRepository) {
        this.accessRoleRepository = accessRoleRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void createAccessRole(@Valid AccessRole accessRole) {
        if(accessRole.getParentRoleCode() != null) {
            final Optional<AccessRole> roleOptional = accessRoleRepository.getAccessRole(accessRole.getParentRoleCode());
            if(!roleOptional.isPresent()) {
                throw  EntityNotFoundException.withMessage("Parent Access role with code [%s] not found", accessRole.getParentRoleCode());
            }
        }

        final Optional<AccessRole> roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        if(roleOptional.isPresent()) {
            throw  EntityAlreadyExistsException.withMessage("Access role with code [%s] already exists: [%s]", accessRole.getRoleCode(), roleOptional.get().getRoleName());
        }

        accessRoleRepository.createAccessRole(accessRole);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void updateAccessRole(@Valid AccessRole accessRole) {

        final Optional<AccessRole> roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        if(!roleOptional.isPresent()) {
            throw  EntityNotFoundException.withMessage("Access role with code [%s] not found", accessRole.getRoleCode());
        }
        accessRoleRepository.updateAccessRole(accessRole);
    }

    @Override
    public List<AccessRole> getAccessRoles() {
        return accessRoleRepository.getAccessRoles();
    }
}
