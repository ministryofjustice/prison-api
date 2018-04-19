package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityAlreadyExistsException;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static net.syscon.elite.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;

@Service
@Transactional
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public StaffServiceImpl(StaffRepository staffRepository, UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    @Override
    public StaffDetail getStaffDetail(Long staffId) {
        Validate.notNull(staffId, "A staff id is required.");

        return staffRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    @Override
    public StaffDetail getStaffDetailByPersonnelIdentifier(String idType, String id) {
        Validate.notBlank(idType, "An id type is required.");
        Validate.notBlank(id, "An id is required.");

        return staffRepository.findStaffByPersonnelIdentifier(idType, id)
                .orElseThrow(EntityNotFoundException.withMessage(
                        "Staff member not found for external identifier with idType [{}] and id [{}].", idType, id));
    }

    @Override
    @VerifyAgencyAccess
    public Page<StaffLocationRole> getStaffByAgencyPositionRole(GetStaffRoleRequest request, PageRequest pageRequest) {
        Validate.notNull(request, "Staff role request details are required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        Page<StaffLocationRole> staffDetails;

        if (StringUtils.isBlank(request.getPosition())) {
            staffDetails = staffRepository.findStaffByAgencyRole(request.getAgencyId(), request.getRole(), request.getNameFilter(), request.getStaffId(), pageRequest);
        } else {
            staffDetails = staffRepository.findStaffByAgencyPositionRole(request.getAgencyId(), request.getPosition(), request.getRole(), request.getNameFilter(), request.getStaffId(), pageRequest);
        }

        return staffDetails;
    }

    @Override
    public List<StaffUserRole> getStaffRoles(Long staffId) {
        UserDetail userDetail = userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        return mapToStaffUserRole(staffId, userDetail.getUsername(), userRepository.findRolesByUsername(userDetail.getUsername(), null));
    }

    @Override
    public List<StaffUserRole> getRolesByCaseload(Long staffId, String caseload) {
        UserDetail userDetail = userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        final List<UserRole> rolesByUsername = userRepository.findRolesByUsername(userDetail.getUsername(), format("caseloadId:eq:'%s',or:caseloadId:is:null", caseload));
        return mapToStaffUserRole(staffId, userDetail.getUsername(), rolesByUsername);
    }

    private List<StaffUserRole> mapToStaffUserRole(Long staffId, String username, List<UserRole> rolesByUsername) {
        return rolesByUsername.stream().map(role -> StaffUserRole.builder()
                .roleId(role.getRoleId())
                .caseloadId(role.getCaseloadId())
                .parentRoleCode(role.getParentRoleCode())
                .roleCode(StringUtils.replaceFirst(role.getRoleCode(), role.getCaseloadId() + "_", ""))
                .roleName(role.getRoleName())
                .username(username)
                .staffId(staffId)
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<StaffUserRole> getAllStaffRolesForCaseload(String caseload, String roleCode) {
        return userRepository.getAllStaffRolesForCaseload(caseload, roleCode);
    }

    @Override
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public StaffUserRole addStaffRole(Long staffId, String caseload, String roleCode) {
        UserDetail userDetail = userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        // check if role already exists
        Optional<StaffUserRole> staffUserRole = getRoleByCaseload(staffId, userDetail.getUsername(), caseload, roleCode);

        if (staffUserRole.isPresent()) {
            throw new EntityAlreadyExistsException(roleCode);
        }

        // ensure that user accessible caseload exists...
        if (!userRepository.isUserAssessibleCaseloadAvailable(caseload, userDetail.getUsername())) {
            userRepository.addUserAssessibleCaseload(caseload, userDetail.getUsername());
        }

        Long roleId = userRepository.getRoleIdForCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));
        userRepository.addRole(userDetail.getUsername(), caseload, roleId);
        return getRoleByCaseload(staffId, userDetail.getUsername(), caseload, roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));
    }

    @Override
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    public void removeStaffRole(Long staffId, String caseload, String roleCode) {
        UserDetail userDetail = userRepository.findByStaffIdAndStaffUserType(staffId, STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION).orElseThrow(EntityNotFoundException.withId(staffId));

        // check if role exists
        getRoleByCaseload(staffId, userDetail.getUsername(), caseload, roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        Long roleId = userRepository.getRoleIdForCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));
        userRepository.removeRole(userDetail.getUsername(), caseload, roleId);
    }

    private Optional<StaffUserRole> getRoleByCaseload(Long staffId, String username, String caseload, String roleCode) {
        final List<UserRole> rolesByUsername = userRepository.findRolesByUsername(username, format("roleCode:eq:'%s',and:caseloadId:eq:'%s'", caseload + "_" + roleCode, caseload));
        List<StaffUserRole> staffUserRoles = mapToStaffUserRole(staffId, username, rolesByUsername);
        return Optional.ofNullable(staffUserRoles.isEmpty() ? null : staffUserRoles.get(0));
    }
}
