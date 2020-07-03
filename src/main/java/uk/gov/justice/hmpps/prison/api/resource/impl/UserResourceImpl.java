package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.CaseloadUpdate;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.resource.UserResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.CaseLoadService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.LocationService;
import uk.gov.justice.hmpps.prison.service.StaffService;
import uk.gov.justice.hmpps.prison.service.UserService;
import uk.gov.justice.hmpps.prison.service.keyworker.KeyWorkerAllocationService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("${api.base.path}/users")
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
    public Set<String> getAllUsersHavingRoleAtCaseload(final String caseload, final String roleCode) {
        return userService.getAllUsernamesForCaseloadAndRole(caseload, roleCode);
    }

    @Override
    public ResponseEntity<List<UserDetail>> getUsersByCaseLoad(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);
        final var userDetails = userService.getUsersByCaseload(caseload, nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    public ResponseEntity<List<UserDetail>> getStaffUsersForLocalAdministrator(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersAsLocalAdministrator(authenticationFacade.getCurrentUsername(), nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    public ResponseEntity<List<UserDetail>> deprecatedPleaseRemove(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        return getStaffUsersForLocalAdministrator(nameFilter, accessRole, pageOffset, pageLimit, sortFields, sortOrder);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> removeUsersAccessRoleForCaseload(final String username, final String caseload, final String roleCode) {
        userService.removeUsersAccessRoleForCaseload(username, caseload, roleCode);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<UserDetail>> getUsers(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsers(nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> addAccessRole(final String username, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> addAccessRoleByCaseload(final String username, final String caseload, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode, caseload);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @Override
    public UserDetail getMyUserInformation() {
        return userService.getUserByUsername(authenticationFacade.getCurrentUsername());
    }

    @Override
    public List<CaseLoad> getMyCaseLoads(final boolean allCaseloads) {
        return userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);
    }

    @Override
    public List<ReferenceCode>  getMyCaseNoteTypes(final String sortFields, final Order sortOrder) {
        final var currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        final var caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";
        return caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
    }

    @Override
    public List<Location> getMyLocations() {
        return locationService.getUserLocations(authenticationFacade.getCurrentUsername());
    }

    @Override
    public List<UserRole> getMyRoles(final boolean allRoles) {
        return userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public ResponseEntity<?> updateMyActiveCaseLoad(final CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(authenticationFacade.getCurrentUsername(), caseLoad.getCaseLoadId());
        } catch (final AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                    .userMessage("Not Authorized")
                    .developerMessage("The current user does not have acess to this CaseLoad")
                    .build());
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public StaffDetail getStaffDetail(final Long staffId) {
        return staffService.getStaffDetail(staffId);
    }

    @Override
    public UserDetail getUserDetails(final String username) {
        return userService.getUserByUsername(username.toUpperCase());
    }

    @Override
    public List<UserDetail> getUserDetailsList(final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
   }

    @Override
    public List<AccessRole> getRolesForUserAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        return userService.getAccessRolesByUserAndCaseload(username, caseload, includeAdmin);
    }

    @Override
    @ProxyUser
    public ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(caseloadUpdate);
        }
        return ResponseEntity.ok().body(caseloadUpdate);    }

}
