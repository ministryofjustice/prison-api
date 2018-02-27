package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;

    public StaffServiceImpl(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public StaffDetail getStaffDetail(Long staffId) {
        Validate.notNull(staffId, "A staff id is required in order to retrieve staff details.");

        return staffRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
    }
}
