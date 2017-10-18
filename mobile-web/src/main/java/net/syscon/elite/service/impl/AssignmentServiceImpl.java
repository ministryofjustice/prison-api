package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.syscon.elite.service.impl.InmateServiceImpl.DEFAULT_OFFENDER_SORT;

@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {
    private final UserRepository userRepository;

    private final InmateRepository inmateRepository;

    private final String locationTypeGranularity;

    @Autowired
    public AssignmentServiceImpl(InmateRepository inmateRepository, UserRepository userRepository, @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity ) {
        this.userRepository = userRepository;
        this.inmateRepository = inmateRepository;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public List<OffenderBooking> findMyAssignments(long offset, long limit) {
        final String username = UserSecurityUtils.getCurrentUsername();
        final UserDetail loggedInUser = userRepository.findByUsername(username).orElseThrow(new EntityNotFoundException(username));
        return inmateRepository.findMyAssignments(loggedInUser.getStaffId(), loggedInUser.getActiveCaseLoadId(), locationTypeGranularity, DEFAULT_OFFENDER_SORT, true, offset, limit);
    }
}
