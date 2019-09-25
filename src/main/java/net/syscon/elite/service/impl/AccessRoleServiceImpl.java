package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
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

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class AccessRoleServiceImpl implements AccessRoleService {
    private final AccessRoleRepository accessRoleRepository;

    public AccessRoleServiceImpl(final AccessRoleRepository accessRoleRepository) {
        this.accessRoleRepository = accessRoleRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void createAccessRole(@Valid final AccessRole accessRole) {
        if (accessRole.getParentRoleCode() != null) {
            final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getParentRoleCode());
            if (!roleOptional.isPresent()) {
                throw EntityNotFoundException.withMessage("Parent Access role with code [%s] not found", accessRole.getParentRoleCode());
            }
        }

        final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        if (roleOptional.isPresent()) {
            throw EntityAlreadyExistsException.withMessage("Access role with code [%s] already exists: [%s]", accessRole.getRoleCode(), roleOptional.get().getRoleName());
        }

        if (accessRole.getRoleFunction() == null) accessRole.setRoleFunction("GENERAL");

        accessRoleRepository.createAccessRole(accessRole);
        log.info("Created Access Role: {}", accessRole.toString());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void updateAccessRole(@Valid final AccessRole accessRole) {

        final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        final var roleBeforeUpdate = roleOptional.orElseThrow(EntityNotFoundException.withMessage("Access role with code [%s] not found", accessRole.getRoleCode()));

        /* fill in optional parameters for mandatory fields */
        if (accessRole.getRoleName() == null) accessRole.setRoleName(roleBeforeUpdate.getRoleName());
        if (accessRole.getRoleFunction() == null) accessRole.setRoleFunction(roleBeforeUpdate.getRoleFunction());

        accessRoleRepository.updateAccessRole(accessRole);
        log.info("Updated Access Role from {} to {}", roleBeforeUpdate.toString(), accessRole.toString());
    }

    @Override
    public List<AccessRole> getAccessRoles(final boolean includeAdmin) {
        return accessRoleRepository.getAccessRoles(includeAdmin);
    }
}
