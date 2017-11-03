package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.syscon.elite.service.impl.InmateServiceImpl.DEFAULT_OFFENDER_SORT;

@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {
    private final UserRepository userRepository;
    private final InmateRepository inmateRepository;
    private final String locationTypeGranularity;

    public AssignmentServiceImpl(InmateRepository inmateRepository, UserRepository userRepository, @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity) {
        this.userRepository = userRepository;
        this.inmateRepository = inmateRepository;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public Page<OffenderBooking> findMyAssignments(long offset, long limit) {
        String username = UserSecurityUtils.getCurrentUsername();

        UserDetail loggedInUser = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));

        return inmateRepository.findMyAssignments(
                loggedInUser.getStaffId(),
                loggedInUser.getActiveCaseLoadId(),
                locationTypeGranularity,
                DEFAULT_OFFENDER_SORT,
                true,
                offset,
                limit);
    }
}
