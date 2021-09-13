package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.repository.AccessRoleRepository;

import javax.validation.Valid;
import java.util.List;

/**
 * Access Role API service interface.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class AccessRoleService {
    private final AccessRoleRepository accessRoleRepository;

    public AccessRoleService(final AccessRoleRepository accessRoleRepository) {
        this.accessRoleRepository = accessRoleRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void createAccessRole(@Valid final AccessRole accessRole) {
        if (accessRole.getParentRoleCode() != null) {
            final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getParentRoleCode());
            if (roleOptional.isEmpty()) {
                throw EntityNotFoundException.withMessage("Parent Access role with code [%s] not found", accessRole.getParentRoleCode());
            }
        }

        final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        if (roleOptional.isPresent()) {
            throw EntityAlreadyExistsException.withMessage("Access role with code [%s] already exists: [%s]", accessRole.getRoleCode(), roleOptional.get().getRoleName());
        }

        if (accessRole.getRoleFunction() == null) accessRole.setRoleFunction("GENERAL");

        accessRoleRepository.createAccessRole(accessRole);
        log.info("Created Access Role: {}", accessRole);
    }

    @Transactional
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void updateAccessRole(@Valid final AccessRole accessRole) {

        final var roleOptional = accessRoleRepository.getAccessRole(accessRole.getRoleCode());

        final var roleBeforeUpdate = roleOptional.orElseThrow(EntityNotFoundException.withMessage("Access role with code [%s] not found", accessRole.getRoleCode()));

        /* fill in optional parameters for mandatory fields */
        if (accessRole.getRoleName() == null) accessRole.setRoleName(roleBeforeUpdate.getRoleName());
        if (accessRole.getRoleFunction() == null) accessRole.setRoleFunction(roleBeforeUpdate.getRoleFunction());

        accessRoleRepository.updateAccessRole(accessRole);
        log.info("Updated Access Role from {} to {}", roleBeforeUpdate, accessRole);
    }

    public List<AccessRole> getAccessRoles(final boolean includeAdmin) {
        return accessRoleRepository.getAccessRoles(includeAdmin);
    }
}
