package uk.gov.justice.hmpps.prison.service.support;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

@Getter
public class GetStaffRoleRequest extends AgencyRequest {
    private final String position;
    private final String role;
    private final String nameFilter;
    private final Boolean activeOnly;
    private final Long staffId;

    public GetStaffRoleRequest(final String agencyId, final String role, final Boolean activeOnly) {
        this(agencyId, null, role, null, activeOnly, null);
    }

    public GetStaffRoleRequest(final String agencyId, final String position, final String role, final Boolean activeOnly) {
        this(agencyId, position, role, null, activeOnly, null);
    }

    public GetStaffRoleRequest(final String agencyId, final String position, final String role, final String nameFilter, final Boolean activeOnly, final Long staffId) {
        super(agencyId);
        this.activeOnly = activeOnly;

        Validate.notBlank(role, "Role is required.");

        this.position = position;
        this.role = role;
        this.nameFilter = nameFilter;
        this.staffId = staffId;
    }
}
