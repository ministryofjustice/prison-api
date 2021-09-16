package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.api.model.StaffUserRole;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.CaseLoadRepository;
import uk.gov.justice.hmpps.prison.repository.StaffRepository;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadId;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRoleIdentity;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.RoleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.service.support.GetStaffRoleRequest;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;

@Service
@Transactional(readOnly = true)
public class StaffService {
    public static final String STAFF_STATUS_ACTIVE = "ACTIVE";

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final UserCaseloadRoleRepository userCaseloadRoleRepository;
    private final RoleRepository roleRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final CaseLoadRepository caseLoadRepository;
    private final UserCaseloadRepository userCaseloadRepository;

    public StaffService(final StaffRepository staffRepository,
                        final StaffUserAccountRepository staffUserAccountRepository,
                        final UserRepository userRepository, CaseLoadRepository caseLoadRepository,
                        final UserCaseloadRoleRepository userCaseloadRoleRepository,
                        final RoleRepository roleRepository,
                        final UserCaseloadRepository userCaseloadRepository) {
        this.staffRepository = staffRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.userRepository = userRepository;
        this.caseLoadRepository = caseLoadRepository;
        this.userCaseloadRoleRepository = userCaseloadRoleRepository;
        this.roleRepository = roleRepository;
        this.userCaseloadRepository = userCaseloadRepository;
    }

    public static boolean isStaffActive(final StaffDetail staffDetail) {
        Validate.notNull(staffDetail);

        return StringUtils.equals(STAFF_STATUS_ACTIVE, staffDetail.getStatus());
    }

    public StaffDetail getStaffDetail(@NotNull final Long staffId) {
        return staffRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    public List<String> getStaffEmailAddresses(@NotNull final Long staffId) {
        checkStaffExists(staffId);

        final var emailAddressList = staffRepository.findEmailAddressesForStaffId(staffId);
        if (emailAddressList == null || emailAddressList.isEmpty()) {
            throw NoContentException.withId(staffId);
        }

        return emailAddressList;
    }


    public List<CaseLoad> getStaffCaseloads(@NotNull final Long staffId) {
        checkStaffExists(staffId);
        final var staffCaseloads = caseLoadRepository.getCaseLoadsByStaffId(staffId);

        if (staffCaseloads == null || staffCaseloads.isEmpty()) {
            throw NoContentException.withId(staffId);
        }
        return staffCaseloads;
    }

    private void checkStaffExists(Long staffId) {
        final var staffDetail = staffRepository.findByStaffId(staffId);
        if (staffDetail.isEmpty()) {
            throw EntityNotFoundException.withId(staffId);
        }
    }

    public StaffDetail getStaffDetailByPersonnelIdentifier(final String idType, final String id) {
        Validate.notBlank(idType, "An id type is required.");
        Validate.notBlank(id, "An id is required.");

        return staffRepository.findStaffByPersonnelIdentifier(idType, id)
                .orElseThrow(EntityNotFoundException.withMessage(
                        "Staff member not found for external identifier with idType [{}] and id [{}].", idType, id));
    }

    @VerifyAgencyAccess
    public Page<StaffLocationRole> getStaffByAgencyPositionRole(final GetStaffRoleRequest request, final PageRequest pageRequest) {
        Validate.notNull(request, "Staff role request details are required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        final Page<StaffLocationRole> staffDetails;

        if (StringUtils.isBlank(request.getPosition())) {
            staffDetails = staffRepository.findStaffByAgencyRole(request.getAgencyId(), request.getRole(), request.getNameFilter(), request.getStaffId(), request.getActiveOnly(), pageRequest);
        } else {
            staffDetails = staffRepository.findStaffByAgencyPositionRole(request.getAgencyId(), request.getPosition(), request.getRole(), request.getNameFilter(), request.getStaffId(), request.getActiveOnly(), pageRequest);
        }

        return staffDetails;
    }

    public List<StaffUserRole> getStaffRoles(final Long staffId) {
        final var userDetail = staffUserAccountRepository.findByTypeAndStaff_StaffId(STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION, staffId).orElseThrow(EntityNotFoundException.withId(staffId));
        return mapToStaffUserRole(staffId, userDetail.getUsername(), filterRoles(UserCaseloadRoleFilter.builder().username(userDetail.getUsername()).build()));
    }

    public List<StaffUserRole> getRolesByCaseload(final Long staffId, final String caseload) {
        final var userDetail = staffUserAccountRepository.findByTypeAndStaff_StaffId(STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION, staffId).orElseThrow(EntityNotFoundException.withId(staffId));
        return mapToStaffUserRole(staffId, userDetail.getUsername(), filterRoles(UserCaseloadRoleFilter.builder().username(userDetail.getUsername()).caseload(caseload).build()));
    }

    private Optional<StaffUserRole> getRoleByCaseload(final Long staffId, final String username, final String caseload, final String roleCode) {
        final var rolesByUsername = filterRoles(UserCaseloadRoleFilter.builder().username(username).caseload(caseload).roleCode(roleCode).build());
        final var staffUserRoles = mapToStaffUserRole(staffId, username, rolesByUsername);
        return Optional.ofNullable(staffUserRoles.isEmpty() ? null : staffUserRoles.get(0));
    }

    private List<UserRole> filterRoles(final UserCaseloadRoleFilter filter) {
        return userCaseloadRoleRepository.findAll(filter)
            .stream().map(UserCaseloadRole::transform)
            .sorted(Comparator.comparing(UserRole::getRoleCode))
            .collect(Collectors.toList());
    }

    private List<StaffUserRole> mapToStaffUserRole(final Long staffId, final String username, final List<UserRole> rolesByUsername) {
        return rolesByUsername.stream().map(role -> transform(staffId, username, role)).collect(Collectors.toList());
    }

    private StaffUserRole transform(final Long staffId, final String username, final UserRole role) {
        return StaffUserRole.builder()
                .roleId(role.getRoleId())
                .caseloadId(role.getCaseloadId())
                .parentRoleCode(role.getParentRoleCode())
                .roleCode(RegExUtils.replaceFirst(role.getRoleCode(), role.getCaseloadId() + "_", ""))
                .roleName(role.getRoleName())
                .username(username)
                .staffId(staffId)
                .build();
    }

    public List<StaffUserRole> getAllStaffRolesForCaseload(final String caseload, final String roleCode) {
        return userRepository.getAllStaffRolesForCaseload(caseload, roleCode);
    }

    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    @Transactional
    public StaffUserRole addStaffRole(final Long staffId, final String caseload, final String roleCode) {
        final var userDetail = staffUserAccountRepository.findByTypeAndStaff_StaffId(STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION, staffId).orElseThrow(EntityNotFoundException.withId(staffId));
        // check if role already exists
        userDetail.findByCaseloadAndRoleCode(caseload, roleCode).ifPresent(r -> {
            throw new EntityAlreadyExistsException(roleCode);
        });

        final var username = userDetail.getUsername();
        // ensure that user accessible caseload exists...

        final var userCaseload = userCaseloadRepository.findById(UserCaseloadId.builder().caseload(caseload).username(username).build());
        if (userCaseload.isEmpty()) {
            userCaseloadRepository.save(UserCaseload.builder().id(UserCaseloadId.builder().username(username).caseload(caseload).build()).startDate(LocalDate.now()).build());
        }

        final var role = roleRepository.findByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        final var newRole = UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(caseload)
                .roleId(role.getId())
                .username(username)
                .build())
            .role(role)
            .build();
        userDetail.getRoles().add(newRole);

        return transform(staffId, username, newRole.transform());
    }

    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES')")
    @Transactional
    public void removeStaffRole(final Long staffId, final String caseload, final String roleCode) {
        final var userDetail = staffUserAccountRepository.findByTypeAndStaff_StaffId(STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION, staffId).orElseThrow(EntityNotFoundException.withId(staffId));

        // check if role exists
        final var userRole = userDetail.findByCaseloadAndRoleCode(caseload, roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));
        userCaseloadRoleRepository.deleteRole(userRole.getId().getUsername(), userRole.getId().getCaseload(), userRole.getId().getRoleId());
    }

    public List<StaffRole> getAllRolesForAgency(final Long staffId, final String agencyId) {
        Validate.notNull(staffId, "A staff id is required.");
        Validate.notBlank(agencyId, "An agency id is required.");

        return staffRepository.getAllRolesForAgency(staffId, agencyId);
    }
}
