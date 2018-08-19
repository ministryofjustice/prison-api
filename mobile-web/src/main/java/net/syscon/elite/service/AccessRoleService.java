package net.syscon.elite.service;

import net.syscon.elite.api.model.AccessRole;

import javax.validation.Valid;

/**
 * Access Role API service interface.
 */
public interface AccessRoleService {

    void createAccessRole(@Valid AccessRole accessRole);
    void updateAccessRole(@Valid AccessRole accessRole);
}
