package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Service
public class UserServiceImpl implements UserService {
	private final CaseLoadService caseLoadService;
	private final UserRepository userRepository;
	private final String apiCaseloadId;

	public UserServiceImpl(CaseLoadService caseLoadService, UserRepository userRepository, @Value("${application.caseload.id:NEWB}") String apiCaseloadId) {
		this.caseLoadService = caseLoadService;
		this.userRepository = userRepository;
		this.apiCaseloadId = apiCaseloadId;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetail getUserByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
	}

	@Override
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	public List<UserRole> getRolesByUsername(String username, boolean allRoles) {
		String query = allRoles ? null : format("caseloadId:eq:'%s',or:caseloadId:is:null", apiCaseloadId);
		final List<UserRole> rolesByUsername = userRepository.findRolesByUsername(username, query);

		if (!allRoles) {
			rolesByUsername.forEach(role -> role.setRoleCode(StringUtils.replaceFirst(role.getRoleCode(), apiCaseloadId + "_", "")));
		}
		return rolesByUsername;
	}
}
