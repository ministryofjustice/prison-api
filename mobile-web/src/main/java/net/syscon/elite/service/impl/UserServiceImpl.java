package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.service.UserService;
import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;

@Service
public class UserServiceImpl implements UserService {
	
	private UserRepository userRepository;
	private CaseLoadRepository caseLoadRepository;

	@Inject
	public void setCaseLoadRepository(final CaseLoadRepository caseLoadRepository) {
		this.caseLoadRepository = caseLoadRepository;
	}
	
	@Inject
	public void setUserRepository(final UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails getUserByStaffId(final Long staffId) {
		return userRepository.findByStaffId(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails getUserByUsername(final String username) {
		return userRepository.findByUsername(username);
	}
	

	@Override
	@Transactional(readOnly = true)
	public CaseLoad getActiveCaseLoad(final Long staffId) {
		final UserDetails userDetails = userRepository.findByStaffId(staffId);
		Assert.notNull(userDetails, format("User with staffId %d was not found!", staffId));
		return caseLoadRepository.find(userDetails.getActiveCaseLoadId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<CaseLoad> getCaseLoads(final Long staffId) {
		return caseLoadRepository.findCaseLoadsByStaffId(staffId);
	}

	@Override
	@Transactional
	public void setActiveCaseLoad(final Long staffId, final String caseLoadId) {
		final boolean found = caseLoadRepository.findCaseLoadsByStaffId(staffId).stream()
				.anyMatch(c -> c.getCaseLoadId().equalsIgnoreCase(caseLoadId));

		if (!found) {
			throw new AccessDeniedException(format("The user does not have access to the caseLoadid = %s", caseLoadId));
		} else {
			userRepository.updateCurrentLoad(staffId, caseLoadId);
		}
	}

}
