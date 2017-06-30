package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.service.UserService;
import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.StaffDetails;
import net.syscon.elite.web.api.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

import static java.lang.String.format;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CaseLoadRepository caseLoadRepository;

	@Override
	@Transactional(readOnly = true)
	public StaffDetails getUserByStaffId(Long staffId) {
		return userRepository.findByStaffId(staffId);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails getUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public CaseLoad getActiveCaseLoad(final String username) {
		final UserDetails userDetails = userRepository.findByUsername(username);
		Assert.notNull(userDetails, format("User with username %s was not found!", username));
		return caseLoadRepository.find(userDetails.getActiveCaseLoadId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<CaseLoad> getCaseLoads(final String username) {
		return caseLoadRepository.findCaseLoadsByUsername(username);
	}

	@Override
	@Transactional
	public void setActiveCaseLoad(final String username, final String caseLoadId) {
		final boolean found = caseLoadRepository.findCaseLoadsByUsername(username).stream()
				.anyMatch(c -> c.getCaseLoadId().equalsIgnoreCase(caseLoadId));

		if (!found) {
			throw new AccessDeniedException(format("The user does not have access to the caseLoadid = %s", caseLoadId));
		} else {
			final UserDetails userDetails = userRepository.findByUsername(username);
			userRepository.updateCurrentLoad(userDetails.getStaffId(), caseLoadId);
		}
	}
}
