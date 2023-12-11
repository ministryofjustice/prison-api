package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffJobRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffJobRoleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.service.support.GetStaffRoleRequest;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;

@Service
@Transactional(readOnly = true)
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserCaseloadRoleRepository userCaseloadRoleRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final CaseLoadRepository caseLoadRepository;
    private final StaffJobRoleRepository staffJobRoleRepository;

    public StaffService(final StaffRepository staffRepository,
                        final StaffUserAccountRepository staffUserAccountRepository,
                        final CaseLoadRepository caseLoadRepository,
                        final UserCaseloadRoleRepository userCaseloadRoleRepository,
                        final StaffJobRoleRepository staffJobRoleRepository) {
        this.staffRepository = staffRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.caseLoadRepository = caseLoadRepository;
        this.userCaseloadRoleRepository = userCaseloadRoleRepository;
        this.staffJobRoleRepository = staffJobRoleRepository;
    }

    public StaffDetail getStaffDetail(@NotNull final Long staffId) {
        if (staffId == null) throw new EntityNotFoundException("No staff id specified");
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

    @VerifyAgencyAccess(overrideRoles = {"STAFF_SEARCH"})
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

    private List<UserRole> filterRoles(final UserCaseloadRoleFilter filter) {
        return userCaseloadRoleRepository.findAll(filter)
            .stream().map(UserCaseloadRole::transform)
            .sorted(Comparator.comparing(UserRole::getRoleCode))
            .toList();
    }

    private List<StaffUserRole> mapToStaffUserRole(final Long staffId, final String username, final List<UserRole> rolesByUsername) {
        return rolesByUsername.stream().map(role -> transform(staffId, username, role)).toList();
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

    public List<StaffRole> getAllRolesForAgency(final Long staffId, final String agencyId) {
        return reduceToLatestActivePerType(staffJobRoleRepository.findAllByAgencyIdAndStaffStaffId(agencyId, staffId));
    }

    private static List<StaffRole> reduceToLatestActivePerType(List<StaffJobRole> staffRoles) {
        return staffRoles
            .stream()
            .filter(
                staffJobRole -> staffJobRole.isWithinRange(LocalDate.now())
            )
            .collect(Collectors.groupingBy(StaffJobRole::getStaffRole))
            .values().stream()
            .map(staffJobRoles -> staffJobRoles.stream()
                .max(Comparator.comparing(StaffJobRole::getFromDate)))
            .filter(Optional::isPresent)
            .map(
                staffJobRole -> StaffRole.builder()
                    .role(staffJobRole.get().getStaffRole().getCode())
                    .roleDescription(staffJobRole.get().getStaffRole().getDescription())
                    .build()
            ).toList();
    }

    public boolean hasStaffRole(Long staffId, String agencyId, StaffJobType staffJobType) {
        return staffJobRoleRepository.findAllByAgencyIdAndStaffStaffIdAndRole(agencyId, staffId, staffJobType.name())
            .stream()
            .max(Comparator.comparing(StaffJobRole::getFromDate))
            .filter(
                staffJobRole -> staffJobRole.isWithinRange(LocalDate.now())
            ).isPresent();
    }
}
