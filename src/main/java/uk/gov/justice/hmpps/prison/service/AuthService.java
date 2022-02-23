package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.auth.AccountDetail;
import uk.gov.justice.hmpps.prison.api.model.auth.Staff;
import uk.gov.justice.hmpps.prison.api.model.auth.UserPersonDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ROLE_AUTH_NOMIS')")
public class AuthService {

    private final StaffUserAccountRepository staffUserAccountRepository;

    public AuthService(final StaffUserAccountRepository staffUserAccountRepository) {
        this.staffUserAccountRepository = staffUserAccountRepository;
    }

    public UserPersonDetails getNomisUserByUsername(final String username) {
        final var user = staffUserAccountRepository.findById(StringUtils.upperCase(username)).orElseThrow(EntityNotFoundException.withId(username));
        return buildUserPersonDetails(user);
    }

    private UserPersonDetails buildUserPersonDetails(final StaffUserAccount user) {

        List<String> userCaseloadRoleList = user.getDpsRoles().stream()
            .map(userCaseloadRole ->
                userCaseloadRole.getRole().getCode())
            .toList();

        return UserPersonDetails.builder()
                .username(user.getUsername())
                .staff(Staff.builder()
                        .staffId(user.getStaff().getStaffId())
                        .firstName(user.getStaff().getFirstName())
                        .lastName(user.getStaff().getLastName())
                        .status(user.getStaff().getStatus())
                        .build())
                .type(user.getType())
                .activeCaseLoadId(user.getActiveCaseLoadId())
                .roles(userCaseloadRoleList)
                .accountDetail(AccountDetail.builder()
                        .username(user.getAccountDetail().getUsername())
                        .accountStatus(user.getAccountDetail().getAccountStatus())
                        .profile(user.getAccountDetail().getProfile())
                        .passwordExpiry(user.getAccountDetail().getPasswordExpiry())
                        .build())
                .build();
    }
}

