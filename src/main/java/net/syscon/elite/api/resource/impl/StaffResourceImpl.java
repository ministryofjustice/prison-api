package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.StaffResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.StaffService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.base.path}/staff")
public class StaffResourceImpl implements StaffResource {
    private final String apiCaseloadId;
    private final StaffService staffService;

    public StaffResourceImpl(final StaffService staffService,
                             @Value("${application.caseload.id}") final String apiCaseloadId) {
        this.staffService = staffService;
        this.apiCaseloadId = apiCaseloadId;
    }

    @Override
    public StaffDetail getStaffDetail(final Long staffId) {
        return staffService.getStaffDetail(staffId);
    }

    @Override
    public List<String> getStaffEmailAddresses(final Long staffId) {
        return staffService.getStaffEmailAddresses(staffId);
    }

    @Override
    public List<CaseLoad> getStaffCaseloads(final Long staffId) {
        return staffService.getStaffCaseloads(staffId);
    }

    @Override
    public ResponseEntity<List<StaffLocationRole>> getStaffByAgencyPositionRole(
            final String agencyId, final String position, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, position, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return ResponseEntity.ok()
                .headers(staffDetails.getPaginationHeaders())
                .body(staffDetails.getItems());
    }

    @Override
    public ResponseEntity<List<StaffLocationRole>> getStaffByAgencyRole(
            final String agencyId, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return ResponseEntity.ok()
                .headers(staffDetails.getPaginationHeaders())
                .body(staffDetails.getItems());
    }

    @Override
    public List<StaffUserRole> getStaffAccessRoles(final Long staffId) {
        return staffService.getStaffRoles(staffId);
    }

    @Override
    public List<StaffUserRole> getAccessRolesByCaseload(final Long staffId, final String caseload) {
        return staffService.getRolesByCaseload(staffId, caseload);
    }

    @Override
    public List<StaffRole>  getAllRolesForAgency(final Long staffId, final String agencyId) {
        return staffService.getAllRolesForAgency(staffId, agencyId);
    }

    @Override
    @ProxyUser
    public StaffUserRole addStaffAccessRoleForApiCaseload(final Long staffId, final String body) {
        return staffService.addStaffRole(staffId, apiCaseloadId, body);
    }

    @Override
    @ProxyUser
    public StaffUserRole addStaffAccessRole(final Long staffId, final String caseload, final String body) {
        return staffService.addStaffRole(staffId, caseload, body);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> removeStaffAccessRole(final Long staffId, final String caseload, final String roleCode) {
        staffService.removeStaffRole(staffId, caseload, roleCode);
        return ResponseEntity.ok().build();
    }

    @Override
    public List<StaffUserRole> getAllStaffAccessRolesForCaseload(final String caseload, final String roleCode) {
        return staffService.getAllStaffRolesForCaseload(caseload, roleCode);
    }
}
