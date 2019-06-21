package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.UserResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.util.ProfileUtil;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestResource
@Path("/users")
public class UserResourceImpl implements UserResource {
    private final AuthenticationFacade authenticationFacade;
    private final UserService userService;
    private final InmateService inmateService;
    private final LocationService locationService;
    private final StaffService staffService;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;
    private final KeyWorkerAllocationService keyWorkerAllocationService;
    private final Environment env;

    public UserResourceImpl(final AuthenticationFacade authenticationFacade,
                            final LocationService locationService,
                            final UserService userService,
                            final StaffService staffService,
                            final CaseLoadService caseLoadService,
                            final CaseNoteService caseNoteService,
                            final InmateService inmateService,
                            final KeyWorkerAllocationService keyWorkerAllocationService,
                            final Environment env) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.userService = userService;
        this.staffService = staffService;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
        this.inmateService = inmateService;
        this.keyWorkerAllocationService = keyWorkerAllocationService;
        this.env = env;
    }

    @Override
    public GetAllUsersHavingRoleAtCaseloadResponse getAllUsersHavingRoleAtCaseload(final String caseload, final String roleCode) {
        final var users = userService.getAllUsernamesForCaseloadAndRole(caseload, roleCode);
        return GetAllUsersHavingRoleAtCaseloadResponse.respond200WithApplicationJson(new ArrayList<>(users));
    }

    @Override
    public GetUsersByCaseLoadResponse getUsersByCaseLoad(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersByCaseload(caseload, nameFilter, accessRole, pageRequest);

        return GetUsersByCaseLoadResponse.respond200WithApplicationJson(userDetails);
    }

    @Override
    public GetStaffUsersForLocalAdminstrator getStaffUsersForLocalAdministrator(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersAsLocalAdministrator(authenticationFacade.getCurrentUsername(), nameFilter, accessRole, pageRequest);

        return GetStaffUsersForLocalAdminstrator.respond200WithApplicationJson(userDetails);
    }

    @Override
    public GetStaffUsersForLocalAdminstrator deprecatedPleaseRemove(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        return getStaffUsersForLocalAdministrator(nameFilter, accessRole, pageOffset, pageLimit, sortFields, sortOrder);
    }

    @Override
    @ProxyUser
    public RemoveUsersAccessRoleForCaseloadResponse removeUsersAccessRoleForCaseload(final String username, final String caseload, final String roleCode) {
        userService.removeUsersAccessRoleForCaseload( username,  caseload,  roleCode);
        return RemoveUsersAccessRoleForCaseloadResponse.respond200WithApplicationJson();
    }

    @Override
    public GetUsersResponse getUsers(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsers(nameFilter, accessRole, pageRequest);

        return GetUsersResponse.respond200WithApplicationJson(userDetails);
    }

    @Override
    @ProxyUser
    public AddAccessRoleResponse addAccessRole(final String username, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode);
        return added? AddAccessRoleResponse.respond201WithApplicationJson() : AddAccessRoleResponse.respond200WithApplicationJson();
    }

    @Override
    @ProxyUser
    public AddAccessRoleByCaseloadResponse addAccessRoleByCaseload(final String username, final String caseload, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode, caseload);
        return added? AddAccessRoleByCaseloadResponse.respond201WithApplicationJson() : AddAccessRoleByCaseloadResponse.respond200WithApplicationJson();
    }

    @Override
    public GetMyUserInformationResponse getMyUserInformation() {
        final var user = userService.getUserByUsername(authenticationFacade.getCurrentUsername());

        return GetMyUserInformationResponse.respond200WithApplicationJson(user);
    }

    @Override
    public GetMyCaseLoadsResponse getMyCaseLoads(final boolean allCaseloads) {
        final var caseLoads = userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);

        return GetMyCaseLoadsResponse.respond200WithApplicationJson(caseLoads);
    }

    @Override
    public GetMyCaseNoteTypesResponse getMyCaseNoteTypes(final String sortFields, final Order sortOrder) {
        final var currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        final var caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";

        final var caseNoteTypes = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);

        return GetMyCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
    }

    @Override
    public GetMyLocationsResponse getMyLocations() {
        final var userLocations = locationService.getUserLocations(authenticationFacade.getCurrentUsername());

        return GetMyLocationsResponse.respond200WithApplicationJson(userLocations);
    }

    @Override
    public GetMyRolesResponse getMyRoles(final boolean allRoles) {
        final var rolesByUsername = userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);

        return GetMyRolesResponse.respond200WithApplicationJson(rolesByUsername);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public UpdateMyActiveCaseLoadResponse updateMyActiveCaseLoad(final CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(authenticationFacade.getCurrentUsername(), caseLoad.getCaseLoadId());
        } catch (final AccessDeniedException ex) {
            return UpdateMyActiveCaseLoadResponse.respond403WithApplicationJson(ErrorResponse.builder()
                    .userMessage("Not Authorized")
                    .developerMessage("The current user does not have acess to this CaseLoad")
                    .build());
        }

        return null;
    }

    @Override
    public GetStaffDetailResponse getStaffDetail(final Long staffId) {
        return GetStaffDetailResponse.respond200WithApplicationJson(staffService.getStaffDetail(staffId));
    }

    @Override
    public GetUserDetailsResponse getUserDetails(final String username) {
        final var userByUsername = userService.getUserByUsername(username.toUpperCase());

        return GetUserDetailsResponse.respond200WithApplicationJson(userByUsername);
    }

    @Override
    public PostUserDetailsListResponse getUserDetailsList(final Set<String> usernames) {
        final var users = userService.getUserListByUsernames(usernames);

        return PostUserDetailsListResponse.respond200WithApplicationJson(users);
    }

    @Override
    public GetRolesForUserAndCaseloadResponse getRolesForUserAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        final var roles = userService.getAccessRolesByUserAndCaseload(username, caseload, includeAdmin);

        return GetRolesForUserAndCaseloadResponse.respond200WithApplicationJson(roles);
    }

    @Override
    @ProxyUser
    public AddApiAccessForCaseloadResponse addApiAccessForCaseload(final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return AddApiAccessForCaseloadResponse.respond201WithApplicationJson(caseloadUpdate);
        }
        return AddApiAccessForCaseloadResponse.respond200WithApplicationJson(caseloadUpdate);
    }

    @Override
    public GetMyAssignmentsResponse getMyAssignments(final Long pageOffset, final Long pageLimit) {
        final var nomisProfile = ProfileUtil.isNomisProfile(env);
        var iepLevel = false;
        List<Long> bookingIds = null;
        List<String> offenderNos = null;

        if (nomisProfile) {
            iepLevel = true;
            final var allocations = keyWorkerAllocationService.getAllocationsForCurrentCaseload(authenticationFacade.getCurrentUsername());
            offenderNos = allocations.stream().map(KeyWorkerAllocationDetail::getOffenderNo).collect(Collectors.toList());
        } else {
            bookingIds = inmateService.getPersonalOfficerBookings(authenticationFacade.getCurrentUsername());
        }

        final var pageRequest = new PageRequest(null, Order.ASC, pageOffset, pageLimit);
        var assignments = new Page<OffenderBooking>(Collections.emptyList(), 0, pageRequest.getOffset(), pageRequest.getLimit());

        if (!(CollectionUtils.isEmpty(bookingIds) && CollectionUtils.isEmpty(offenderNos))) {
            assignments = inmateService.findAllInmates(
                    InmateSearchCriteria.builder()
                            .username(authenticationFacade.getCurrentUsername())
                            .iepLevel(iepLevel)
                            .offenderNos(offenderNos)
                            .bookingIds(bookingIds)
                            .pageRequest(pageRequest)
                            .build());
        }
        return GetMyAssignmentsResponse.respond200WithApplicationJson(assignments);
    }

}
