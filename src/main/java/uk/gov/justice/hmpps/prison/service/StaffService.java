package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.service.support.GetStaffRoleRequest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;

@Service
@Slf4j
@Transactional(readOnly = true)
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserCaseloadRoleRepository userCaseloadRoleRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final CaseLoadRepository caseLoadRepository;
    private final StaffJobRoleRepository staffJobRoleRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TelemetryClient telemetryClient;


    public StaffService(final StaffRepository staffRepository,
                        final StaffUserAccountRepository staffUserAccountRepository,
                        final CaseLoadRepository caseLoadRepository,
                        final UserCaseloadRoleRepository userCaseloadRoleRepository,
                        final StaffJobRoleRepository staffJobRoleRepository,
                        AuthenticationFacade authenticationFacade,
                        TelemetryClient telemetryClient) {
        this.staffRepository = staffRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.caseLoadRepository = caseLoadRepository;
        this.userCaseloadRoleRepository = userCaseloadRoleRepository;
        this.staffJobRoleRepository = staffJobRoleRepository;
        this.authenticationFacade = authenticationFacade;
        this.telemetryClient = telemetryClient;
    }

    public StaffDetail getStaffDetail(@NotNull final Long staffId) {
        if (staffId == null) throw new EntityNotFoundException("No staff id specified");
        temporaryLogUsage(staffId);
        return staffRepository.findByStaffId(staffId).orElseThrow(EntityNotFoundException.withId(staffId));
    }

    public List<String> getStaffEmailAddresses(@NotNull final Long staffId) {
        checkStaffExists(staffId);
        temporaryLogUsage(staffId);

        final var emailAddressList = staffRepository.findEmailAddressesForStaffId(staffId);
        if (emailAddressList == null || emailAddressList.isEmpty()) {
            throw NoContentException.withId(staffId);
        }

        return emailAddressList;
    }

    public List<CaseLoad> getStaffCaseloads(@NotNull final Long staffId) {
        checkStaffExists(staffId);
        temporaryLogUsage(staffId);
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
        temporaryLogUsage(request.getStaffId());

        final Page<StaffLocationRole> staffDetails;

        if (StringUtils.isBlank(request.getPosition())) {
            staffDetails = staffRepository.findStaffByAgencyRole(request.getAgencyId(), request.getRole(), request.getNameFilter(), request.getStaffId(), request.getActiveOnly(), pageRequest);
        } else {
            staffDetails = staffRepository.findStaffByAgencyPositionRole(request.getAgencyId(), request.getPosition(), request.getRole(), request.getNameFilter(), request.getStaffId(), request.getActiveOnly(), pageRequest);
        }

        return staffDetails;
    }

    public List<StaffUserRole> getStaffRoles(final Long staffId) {
        temporaryLogUsage(staffId);
        final var userDetail = staffUserAccountRepository.findByTypeAndStaff_StaffId(STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION, staffId).orElseThrow(EntityNotFoundException.withId(staffId));
        return mapToStaffUserRole(staffId, userDetail.getUsername(), filterRoles(UserCaseloadRoleFilter.builder().username(userDetail.getUsername()).build()));
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
        temporaryLogUsage(staffId);
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
        temporaryLogUsage(staffId);
        return staffJobRoleRepository.findAllByAgencyIdAndStaffStaffIdAndRole(agencyId, staffId, staffJobType.name())
            .stream()
            .max(Comparator.comparing(StaffJobRole::getFromDate))
            .filter(
                staffJobRole -> staffJobRole.isWithinRange(LocalDate.now())
            ).isPresent();
    }

    /**
     * Report whether the staffId is the logged-in user
     */
    private void temporaryLogUsage(Long staffId) {
        final String currentUsername = authenticationFacade.getCurrentUsername();
        if (currentUsername == null) {
            telemetryClient.trackEvent("staff access with no username", Map.of(
                "requestedStaffId", String.valueOf(staffId)
            ), null);
        } else if (loggableClient()) {
            staffUserAccountRepository.findByUsername(currentUsername)
                .ifPresentOrElse(
                    staffUserAccount -> {
                        final Long userStaffId = staffUserAccount.getStaff().getStaffId();
                        if (!userStaffId.equals(staffId)) {
                            telemetryClient.trackEvent("staff access accessing other staff details", Map.of(
                                "currentUsername", currentUsername,
                                "userStaffId", String.valueOf(userStaffId),
                                "requestedStaffId", String.valueOf(staffId)
                            ), null);
                        }
                    },
                    () -> telemetryClient.trackEvent("staff access cannot find staff for username", Map.of(
                        "currentUsername", currentUsername,
                        "requestedStaffId", String.valueOf(staffId)
                    ), null)
                );
        }
    }

    private boolean loggableClient() {
        return !Objects.equals(authenticationFacade.getGrantType(), "client_credentials");
    }
}
