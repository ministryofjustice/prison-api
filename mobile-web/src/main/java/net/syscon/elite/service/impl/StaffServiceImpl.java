package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import org.apache.commons.lang3.StringUtils;
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
        Validate.notNull(staffId, "A staff id is required.");

        return staffRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Override
    @VerifyAgencyAccess
    public Page<StaffDetail> getStaffByAgencyPositionRole(GetStaffRoleRequest request, PageRequest pageRequest) {
        Validate.notNull(request, "Staff role request details are required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        Page<StaffDetail> staffDetails;

        if (StringUtils.isBlank(request.getPosition())) {
            staffDetails = staffRepository.findStaffByAgencyRole(request.getAgencyId(), request.getRole(), request.getNameFilter(), pageRequest);
        } else {
            staffDetails = staffRepository.findStaffByAgencyPositionRole(request.getAgencyId(), request.getPosition(), request.getRole(), request.getNameFilter(), pageRequest);
        }

        return staffDetails;
    }
}
