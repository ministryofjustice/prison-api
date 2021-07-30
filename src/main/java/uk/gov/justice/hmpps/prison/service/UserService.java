package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.CaseloadUpdate;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    public final static String STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION = "GENERAL";

    private static final String ROLE_FUNCTION_ADMIN = "ADMIN";
    private static final CaseLoad EMPTY_CASELOAD = CaseLoad.builder()
            .caseLoadId("___")
            .type("DUMMY")
            .caseloadFunction("GENERAL")
            .description("-------")
            .build();

    private final CaseLoadService caseLoadService;
    private final StaffService staffService;
    private final UserRepository userRepository;
    private final AuthenticationFacade securityUtils;
    private final String apiCaseloadId;
    private final int maxBatchSize;
    private final TelemetryClient telemetryClient;

    public UserService(final CaseLoadService caseLoadService, final StaffService staffService,
                       final UserRepository userRepository, final AuthenticationFacade securityUtils, @Value("${application.caseload.id:NWEB}") final String apiCaseloadId, @Value("${batch.max.size:1000}") final int maxBatchSize, final TelemetryClient telemetryClient) {
        this.caseLoadService = caseLoadService;
        this.staffService = staffService;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.apiCaseloadId = apiCaseloadId;
        this.maxBatchSize = maxBatchSize;
        this.telemetryClient = telemetryClient;
    }

    public UserDetail getUserByUsername(final String username) {
        final var userDetail = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        final var caseLoadsForUser = caseLoadService.getCaseLoadsForUser(username, false);
        if (userDetail.getActiveCaseLoadId() == null && (caseLoadsForUser.isEmpty() || caseLoadsForUser.get(0).equals(EMPTY_CASELOAD))) {
            userDetail.setActiveCaseLoadId(EMPTY_CASELOAD.getCaseLoadId());
        }
        return userDetail;
    }

    public List<UserDetail> getUserListByUsernames(final Set<String> usernames) {
        final List<UserDetail> results = new ArrayList<>();
        if (!usernames.isEmpty()) {
            final var batch = Lists.partition(new ArrayList<>(usernames), maxBatchSize);
            batch.forEach(userBatch -> {
                final var userList = userRepository.getUserListByUsernames(userBatch);
                results.addAll(userList);
            });
        }
        return results;
    }

    @Transactional
    public List<CaseLoad> getCaseLoads(final String username, final boolean allCaseloads) {
        final var caseLoadsForUser = caseLoadService.getCaseLoadsForUser(username, allCaseloads);
        if (caseLoadsForUser.isEmpty()) {
            caseLoadsForUser.add(EMPTY_CASELOAD);
        }
        return caseLoadsForUser;
    }

    public Set<String> getCaseLoadIds(final String username) {
        return getCaseLoads(username, false).stream()
                .map(CaseLoad::getCaseLoadId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void setActiveCaseLoad(final String username, final String caseLoadId) {
        final var userCaseLoads = caseLoadService.getCaseLoadsForUser(username, true);

        if (userCaseLoads.stream().anyMatch(cl -> cl.getCaseLoadId().equalsIgnoreCase(caseLoadId))) {
            userRepository.updateWorkingCaseLoad(username, caseLoadId);
        } else {
            throw new AccessDeniedException(format("The user does not have access to the caseLoadId = %s", caseLoadId));
        }
    }

    public List<UserRole> getRolesByUsername(final String username, final boolean allRoles) {
        final var query = allRoles ? null : format("caseloadId:eq:'%s',or:caseloadId:is:null", apiCaseloadId);
        final var rolesByUsername = userRepository.findRolesByUsername(username, query);

        if (!allRoles) {
            rolesByUsername.forEach(role -> role.setRoleCode(RegExUtils.replaceFirst(role.getRoleCode(), apiCaseloadId + "_", "")));
        }
        return rolesByUsername;
    }

    public UserDetail getUserByExternalIdentifier(final String idType, final String id, final boolean activeOnly) {
        final var staffDetail = staffService.getStaffDetailByPersonnelIdentifier(idType, id);

        final Optional<UserDetail> userDetail;

        if (activeOnly && !StaffService.isStaffActive(staffDetail)) {
            log.info("Staff member found for external identifier with idType [{}] and id [{}] but not active.", idType, id);

            userDetail = Optional.empty();
        } else {
            userDetail = userRepository.findByStaffIdAndStaffUserType(
                    staffDetail.getStaffId(), STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION);
        }

        return userDetail.orElseThrow(EntityNotFoundException
                .withMessage("User not found for external identifier with idType [{}] and id [{}].", idType, id));
    }

    public boolean isUserAssessibleCaseloadAvailable(final String caseload, final String username) {
        return userRepository.isUserAssessibleCaseloadAvailable(caseload, username);
    }

    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public void removeUsersAccessRoleForCaseload(final String username, final String caseload, final String roleCode) {
        final var role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        verifyMaintainRolesAdminAccess(role);

        if (!userRepository.isRoleAssigned(username, caseload, role.getRoleId())) {
            throw EntityNotFoundException.withMessage("Role [%s] not assigned to user [%s] at caseload [%s]", roleCode, username, caseload);
        }
        if (userRepository.removeRole(username, caseload, role.getRoleId()) > 0) {
            telemetryClient.trackEvent(
                    "PrisonUserRoleRemoveSuccess",
                    Map.of("username", username, "role", roleCode, "admin", securityUtils.getCurrentUsername()),
                    null);
            log.info("Removed role '{}' from username '{}' at caseload '{}'", roleCode, username, caseload);
        }
    }

    private void verifyMaintainRolesAdminAccess(final AccessRole role) {
        if (role.getRoleFunction().equals(ROLE_FUNCTION_ADMIN)) {
            if (!securityUtils.isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN")) {
                throw new AccessDeniedException("Maintain roles Admin access required to perform this action");
            }
        }
    }

    /**
     * Add an 'access' role - using the API caseload
     *
     * @param username The user to whom the role is being assigned
     * @param roleCode The role to assign
     * @return true if the role was added, false if the role assignment already exists (no change).
     */
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public boolean addAccessRole(final String username, final String roleCode) {

        return addAccessRole(username, roleCode, apiCaseloadId);
    }

    /**
     * Add an 'access' role
     *
     * @param username   The user to whom the role is being assigned
     * @param roleCode   The role to assign
     * @param caseloadId The caseload to assign the role to
     * @return true if the role was added, false if the role assignment already exists (no change).
     */
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public boolean addAccessRole(final String username, final String roleCode, final String caseloadId) {

        final var role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        verifyMaintainRolesAdminAccess(role);

        if (userRepository.isRoleAssigned(username, caseloadId, role.getRoleId())) {
            return false;
        }

        if (!userRepository.isUserAssessibleCaseloadAvailable(caseloadId, username)) {
            if (caseloadId.equals(apiCaseloadId)) {
                // only for NWEB - ensure that user accessible caseload exists...
                userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
            } else {
                throw EntityNotFoundException.withMessage("Caseload %s is not accessible for user %s", caseloadId, username);
            }
        }

        userRepository.addRole(username, caseloadId, role.getRoleId());
        log.info("Assigned role '{}' to username '{}' at caseload '{}'", roleCode, username, caseloadId);
        telemetryClient.trackEvent(
                "PrisonUserRoleAddSuccess",
                Map.of("username", username, "role", roleCode, "admin", securityUtils.getCurrentUsername()),
                null);
        return true;
    }


    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public CaseloadUpdate addDefaultCaseloadForPrison(final String caseloadId) {
        final var users = userRepository.findAllUsersWithCaseload(caseloadId, apiCaseloadId);

        log.debug("Found {} users with caseload {} that do not have {} caseload", users.size(), caseloadId, apiCaseloadId);
        final List<UserDetail> caseloadsAdded = new ArrayList<>();
        users.forEach(user -> {
            final var username = user.getUsername();
            try {
                userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
                caseloadsAdded.add(user);
            } catch (final Exception e) {
                log.error("Failed to add {} caseload to user {}", apiCaseloadId, username);
            }
        });

        log.debug("{} users API enabled for caseload {}", caseloadsAdded.size(), caseloadId);
        return CaseloadUpdate.builder()
                .caseload(caseloadId)
                .numUsersEnabled(caseloadsAdded.size())
                .build();
    }

    public Page<UserDetail> getUsersAsLocalAdministrator(final String laaUsername, final String nameFilter, final String accessRole, final Status status, final PageRequest pageRequest) {

        final var pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

        return userRepository
                .getUsersAsLocalAdministrator(laaUsername, accessRole, new NameFilter(nameFilter), status, pageWithDefaults);
    }

    private PageRequest getPageRequestDefaultLastNameOrder(final PageRequest pageRequest) {
        var pageWithDefaults = pageRequest;
        if (pageWithDefaults == null) {
            pageWithDefaults = new PageRequest("lastName,firstName");
        } else {
            if (pageWithDefaults.getOrderBy() == null) {
                pageWithDefaults = new PageRequest("lastName,firstName", pageWithDefaults.getOrder(), pageWithDefaults.getOffset(), pageWithDefaults.getLimit());
            }
        }
        return pageWithDefaults;
    }

    public List<AccessRole> getAccessRolesByUserAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        Validate.notBlank(caseload, "A caseload id is required.");
        Validate.notBlank(username, "A username is required.");

        if (caseLoadService.getCaseLoad(caseload).isEmpty()) {
            throw EntityNotFoundException.withMessage("Caseload with id [%s] not found", caseload);
        }

        return userRepository
                .findAccessRolesByUsernameAndCaseload(username, caseload, includeAdmin);
    }

    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
    public Page<UserDetail> getUsers(final String nameFilter, final String accessRole, final Status status, final PageRequest pageRequest) {

        final var pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

        return userRepository
                .findUsers(accessRole, new NameFilter(nameFilter), status, pageWithDefaults);
    }
}
