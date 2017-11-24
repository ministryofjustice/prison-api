package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.String.format;

@Service
public class UserServiceImpl implements UserService {
	private final CaseLoadService caseLoadService;
	private final UserRepository userRepository;

	public UserServiceImpl(CaseLoadService caseLoadService, UserRepository userRepository) {
		this.caseLoadService = caseLoadService;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public StaffDetail getUserByStaffId(Long staffId) {
		return userRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetail getUserByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CaseLoad> getCaseLoads(String username) {
		return caseLoadService.getCaseLoadsForUser(username);
	}

	@Override
	@Transactional
	public void setActiveCaseLoad(String username, String caseLoadId) {
		List<CaseLoad> userCaseLoads = caseLoadService.getCaseLoadsForUser(username);

		if (userCaseLoads.stream().anyMatch(cl -> cl.getCaseLoadId().equalsIgnoreCase(caseLoadId))) {
			UserDetail userDetails = getUserByUsername(username);

			userRepository.updateWorkingCaseLoad(userDetails.getStaffId(), caseLoadId);
		} else {
			throw new AccessDeniedException(format("The user does not have access to the caseLoadid = %s", caseLoadId));
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserRole> getRolesByUsername(String username) {
		return userRepository.findRolesByUsername(username);
	}
}
