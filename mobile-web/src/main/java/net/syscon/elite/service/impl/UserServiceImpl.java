package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
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

	private final CaseLoadService caseLoadService;
	private final StaffService staffService;
	private final UserRepository userRepository;
	private final String apiCaseloadId;

	public UserServiceImpl(CaseLoadService caseLoadService, StaffService staffService,
                           UserRepository userRepository, @Value("${application.caseload.id:NEWB}") String apiCaseloadId) {
		this.caseLoadService = caseLoadService;
		this.staffService = staffService;
		this.userRepository = userRepository;
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
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
	@Transactional
	public void removeUsersAccessRoleForCaseload(String username, String caseload, String roleCode) {
		final Long roleId = userRepository.getRoleIdForCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

		if(! userRepository.isRoleAssigned(username, caseload, roleId)) {
			throw  EntityNotFoundException.withMessage("Role [%s] not assigned to user [%s] at caseload [%s]", roleCode, username, caseload);
		}
		userRepository.removeRole(username, caseload, roleId); // Don't care if it doesn't exist...
	}

	/**
	 * Add an 'access' role - a role assigned to the special 'API Caseload'.
	 * @param username The user to whom the role is being assigned
	 * @param roleCode The role to assign
	 * @return true if the role was added, false if the role assignment already exists (no change).
	 */
	@Override
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
	@Transactional
    public boolean addAccessRole(String username, String roleCode) {

		final Long roleId = userRepository.getRoleIdForCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

		if (userRepository.isRoleAssigned(username, apiCaseloadId, roleId)) {
			return false;
		}
		// ensure that user accessible caseload exists...
		if (!userRepository.isUserAssessibleCaseloadAvailable(apiCaseloadId, username)) {
			userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
		}

		userRepository.addRole(username, apiCaseloadId, roleId);
		return true;
	}

	@Override
	@PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
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
}
