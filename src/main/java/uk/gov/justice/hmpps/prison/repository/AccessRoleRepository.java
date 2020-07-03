package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.AccessRole;

import java.util.List;
import java.util.Optional;

public interface AccessRoleRepository {

    void createAccessRole(AccessRole accessRole);

    void updateAccessRole(AccessRole accessRole);

    Optional<AccessRole> getAccessRole(String accessRoleCode);

    List<AccessRole> getAccessRoles(boolean includeAdmin);
}
