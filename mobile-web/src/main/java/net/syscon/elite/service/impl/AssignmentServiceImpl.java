package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.syscon.elite.service.impl.InmateServiceImpl.DEFAULT_OFFENDER_SORT;

@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InmateRepository inmateRepository;

    @Override
    public List<InmateAssignmentSummary> findMyAssignments(int offset, int limit) {
        final String username = UserSecurityUtils.getCurrentUsername();
        final UserDetails loggedInUser = userRepository.findByUsername(username).orElseThrow(new EntityNotFoundException(username));
        return inmateRepository.findMyAssignments(loggedInUser.getStaffId(), loggedInUser.getActiveCaseLoadId(), DEFAULT_OFFENDER_SORT, true, offset, limit);
    }
}
