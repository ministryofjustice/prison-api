package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String ROLE_FUNCTION_ADMIN = "ADMIN";
    private final CaseLoadService caseLoadService;
	private final StaffService staffService;
	private final UserRepository userRepository;
	private final UserSecurityUtils securityUtils;
	private final String apiCaseloadId;

	public UserServiceImpl(CaseLoadService caseLoadService, StaffService staffService,
						   UserRepository userRepository, UserSecurityUtils securityUtils, @Value("${application.caseload.id:NWEB}") String apiCaseloadId) {
		this.caseLoadService = caseLoadService;
		this.staffService = staffService;
		this.userRepository = userRepository;
		this.securityUtils = securityUtils;
		this.apiCaseloadId = apiCaseloadId;
	}

	@Override
	public UserDetail getUserByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
	}

	@Override
	public List<CaseLoad> getCaseLoads(String username, boolean allCaseloads) {
		return caseLoadService.getCaseLoadsForUser(username, allCaseloads);
	}

	@Override
	public Set<String> getCaseLoadIds(String username) {
		return caseLoadService.getCaseLoadIdsForUser(username, true);
	}

	@Override
	@Transactional
	public void setActiveCaseLoad(String username, String caseLoadId) {
		List<CaseLoad> userCaseLoads = caseLoadService.getCaseLoadsForUser(username, true);

		if (userCaseLoads.stream().anyMatch(cl -> cl.getCaseLoadId().equalsIgnoreCase(caseLoadId))) {
			UserDetail userDetails = getUserByUsername(username);

			userRepository.updateWorkingCaseLoad(userDetails.getStaffId(), caseLoadId);
		} else {
			throw new AccessDeniedException(format("The user does not have access to the caseLoadId = %s", caseLoadId));
		}
	}

	@Override
	public List<UserRole> getRolesByUsername(String username, boolean allRoles) {
		String query = allRoles ? null : format("caseloadId:eq:'%s',or:caseloadId:is:null", apiCaseloadId);
		final List<UserRole> rolesByUsername = userRepository.findRolesByUsername(username, query);

		if (!allRoles) {
			rolesByUsername.forEach(role -> role.setRoleCode(StringUtils.replaceFirst(role.getRoleCode(), apiCaseloadId + "_", "")));
		}
		return rolesByUsername;
	}

	@Override
	public UserDetail getUserByExternalIdentifier(String idType, String id, boolean activeOnly) {
	    StaffDetail staffDetail = staffService.getStaffDetailByPersonnelIdentifier(idType, id);

        Optional<UserDetail> userDetail;

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

	@Override
    public Set<String> getAllUsernamesForCaseloadAndRole(String caseload, String roleCode) {
		return userRepository
                .getAllStaffRolesForCaseload(caseload, roleCode)
                .stream()
                .map(StaffUserRole::getUsername)
                .collect(Collectors.toSet());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isUserAssessibleCaseloadAvailable(String caseload, String username) {
		return userRepository.isUserAssessibleCaseloadAvailable(caseload, username);
	}

	@Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
	@Transactional
	public void removeUsersAccessRoleForCaseload(String username, String caseload, String roleCode) {
		final AccessRole role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        verifyMaintainRolesAdminAccess(role);

        if(! userRepository.isRoleAssigned(username, caseload, role.getRoleId())) {
			throw  EntityNotFoundException.withMessage("Role [%s] not assigned to user [%s] at caseload [%s]", roleCode, username, caseload);
		}
		userRepository.removeRole(username, caseload, role.getRoleId()); // Don't care if it doesn't exist...
        log.info("Removed role '{}' from username '{}' at caseload '{}'", roleCode,  username, caseload);
	}

    private void verifyMaintainRolesAdminAccess(AccessRole role) {
        if(role.getRoleFunction().equals(ROLE_FUNCTION_ADMIN)){
            if (!securityUtils.isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN")){
                throw new AccessDeniedException("Maintain roles Admin access required to perform this action");
            }
        }
    }

    /**
	 * Add an 'access' role - using the API caseload
	 * @param username The user to whom the role is being assigned
	 * @param roleCode The role to assign
	 * @return true if the role was added, false if the role assignment already exists (no change).
	 */
	@Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
	@Transactional
    public boolean addAccessRole(String username, String roleCode) {

		return addAccessRole(username, roleCode, apiCaseloadId);
	}

	/**
	 * Add an 'access' role
	 * @param username The user to whom the role is being assigned
	 * @param roleCode The role to assign
     * @param caseloadId The caseload to assign the role to
	 * @return true if the role was added, false if the role assignment already exists (no change).
	 */
	@Override
	@PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
	@Transactional
	public boolean addAccessRole(String username, String roleCode, String caseloadId) {

		final AccessRole role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

		verifyMaintainRolesAdminAccess(role);

		if (userRepository.isRoleAssigned(username, caseloadId, role.getRoleId())) {
			return false;
		}

		if (!userRepository.isUserAssessibleCaseloadAvailable(caseloadId, username)) {
			if(caseloadId.equals(apiCaseloadId)) {
				// only for NWEB - ensure that user accessible caseload exists...
				userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
			}else{
				throw EntityNotFoundException.withMessage("Caseload %s is not accessible for user %s", caseloadId, username);
			}
		}

		userRepository.addRole(username, caseloadId, role.getRoleId());
        log.info("Assigned role '{}' to username '{}' at caseload '{}'", roleCode,  username, caseloadId);
		return true;
	}



	@Override
	@PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
	@Transactional
	public int addDefaultCaseloadForPrison(String caseloadId) {
		List<UserDetail> users = userRepository.findAllUsersWithCaseload(caseloadId);

		log.debug("Found {} users with caseload {}", users.size(), caseloadId);
		final List<UserDetail> caseloadsAdded = new ArrayList<>();
		users.forEach(user -> {
		    final String username = user.getUsername();
            if (!userRepository.isUserAssessibleCaseloadAvailable(apiCaseloadId, username)) {
                userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
                caseloadsAdded.add(user);
            }
        });

        log.debug("{} Users added to caseload {}", caseloadsAdded.size(), apiCaseloadId);
        return caseloadsAdded.size();
	}

	@Override
	@PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
	public Page<UserDetail> getUsersByCaseload(String caseload, String nameFilter, String accessRole, PageRequest pageRequest) {

		PageRequest pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

		return userRepository
				.findUsersByCaseload(caseload, accessRole, new NameFilter(nameFilter), pageWithDefaults);
	}

	@Override
	@PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
	public Page<UserDetail> getLocalAdministratorUsersByCaseload(String caseload, String nameFilter, String accessRole, PageRequest pageRequest) {

		PageRequest pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

		return userRepository
				.findLocalAdministratorUsersByCaseload(caseload, accessRole, new NameFilter(nameFilter), pageWithDefaults);
	}

	private PageRequest getPageRequestDefaultLastNameOrder(PageRequest pageRequest) {
		PageRequest pageWithDefaults = pageRequest;
		if (pageWithDefaults == null) {
			pageWithDefaults = new PageRequest("lastName,firstName");
		} else {
			if (pageWithDefaults.getOrderBy() == null) {
				pageWithDefaults = new PageRequest("lastName,firstName", pageWithDefaults.getOrder(), pageWithDefaults.getOffset(), pageWithDefaults.getLimit());
			}
		}
		return pageWithDefaults;
	}

	@Override
	public List<AccessRole> getAccessRolesByUserAndCaseload(String username, String caseload, boolean includeAdmin) {
		Validate.notBlank(caseload, "A caseload id is required.");
		Validate.notBlank(username, "A username is required.");

		if(!caseLoadService.getCaseLoad(caseload).isPresent()) {
			throw  EntityNotFoundException.withMessage("Caseload with id [%s] not found", caseload);
		}

		return userRepository
				.findAccessRolesByUsernameAndCaseload(username, caseload, includeAdmin);
	}

	@Override
	@PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
	public Page<UserDetail> getUsers(String nameFilter, String accessRole, PageRequest pageRequest) {

		PageRequest pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

		return userRepository
				.findUsers(accessRole, new NameFilter(nameFilter), pageWithDefaults);
	}
}
