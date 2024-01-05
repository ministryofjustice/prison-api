package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.repository.CaseLoadRepository;
import uk.gov.justice.hmpps.prison.repository.StaffRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffJobRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffJobRoleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StaffServiceImplTest {

    private static final Long ID_MULTIPLE = 8888L;
    private static final Long ID_SINGLE = 9999L;
    private static final Long ID_NONE = 9999L;
    private static final Long ID_BAD = 123L;

    private final List<String> multipleAddresses = List.of("test1@a.com", "test2@b.com", "test3@c.com");
    private final List<String> singleAddress = List.of("test1@a.com");
    private final List<String> emptyList = Collections.emptyList();

    @Mock
    public StaffRepository staffRepository;

    @Mock
    public StaffUserAccountRepository staffUserAccountRepository;

    @Mock
    public CaseLoadRepository caseLoadRepository;

    @Mock
    private UserCaseloadRoleRepository userCaseloadRoleRepository;

    @Mock
    private StaffJobRoleRepository staffJobRoleRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private TelemetryClient telemetryClient;

    private StaffService staffService;

    @BeforeEach
    public void init() {
        staffService = new StaffService(staffRepository, staffUserAccountRepository, caseLoadRepository, userCaseloadRoleRepository, staffJobRoleRepository, authenticationFacade, telemetryClient);
    }

    @Test
    public void testMultipleEmails() {
        when(staffRepository.findByStaffId(ID_MULTIPLE)).thenReturn(getValidStaffDetails(ID_MULTIPLE));
        when(staffRepository.findEmailAddressesForStaffId(ID_MULTIPLE)).thenReturn(multipleAddresses);

        final var addresses = staffService.getStaffEmailAddresses(ID_MULTIPLE);

        assertThat(addresses).containsOnly(multipleAddresses.get(0), multipleAddresses.get(1), multipleAddresses.get(2));

        verify(staffRepository, times(1)).findByStaffId(ID_MULTIPLE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_MULTIPLE);
    }

    @Test
    public void testSingleEmail() {

        when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
        when(staffRepository.findEmailAddressesForStaffId(ID_SINGLE)).thenReturn(singleAddress);

        final var addresses = staffService.getStaffEmailAddresses(ID_SINGLE);

        assertThat(addresses).containsOnly(singleAddress.getFirst());

        verify(staffRepository, times(1)).findByStaffId(ID_SINGLE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_SINGLE);
    }

    @Test
    public void testNoneExist() throws NoContentException {
        when(staffRepository.findByStaffId(ID_NONE)).thenReturn(getValidStaffDetails(ID_NONE));
        when(staffRepository.findEmailAddressesForStaffId(ID_NONE)).thenReturn(emptyList);

        assertThatThrownBy(() -> staffService.getStaffEmailAddresses(ID_NONE)).isInstanceOf(NoContentException.class);

        verify(staffRepository, times(1)).findByStaffId(ID_NONE);
        verify(staffRepository, times(1)).findEmailAddressesForStaffId(ID_NONE);
    }

    @Test
    public void testInvalidId() throws EntityNotFoundException, NoContentException {
        when(staffRepository.findByStaffId(ID_BAD)).thenThrow(EntityNotFoundException.withId(ID_BAD));

        assertThatThrownBy(() -> staffService.getStaffEmailAddresses(ID_BAD)).isInstanceOf(EntityNotFoundException.class);

        verify(staffRepository, times(1)).findByStaffId(ID_BAD);
    }

    @Test
    public void testFiltersOutOldRoles() {

        when(staffJobRoleRepository.findAllByAgencyIdAndStaffStaffId("MDI", -1L)).thenReturn(List.of(
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(1),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 1"),
                null
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(2),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 2"),
                null
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(3),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 3"),
                LocalDate.now().minusDays(2)
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(5),
                "POS",
                "POM",
                new StaffRole("POM", "POM 1"),
                LocalDate.now().minusDays(2)
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(2),
                "POS",
                "POM",
                new StaffRole("POM", "POM 2"),
                null
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(3),
                "POS",
                "POM",
                new StaffRole("POM", "POM 3"),
                null
            )
        ));

        final var returnedData = staffService.getAllRolesForAgency(-1L, "MDI");

        assertThat(returnedData).hasSize(2);

        assertThat(returnedData.get(0).getRoleDescription()).isEqualTo("POM 2");
        assertThat(returnedData.get(1).getRoleDescription()).isEqualTo("Key worker 1");
    }

    @Test
    public void testFindRole() {

        when(staffJobRoleRepository.findAllByAgencyIdAndStaffStaffIdAndRole("MDI", -1L, "KW")).thenReturn(List.of(
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(1),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 1"),
                null
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(2),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 2"),
                null
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(3),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 3"),
                LocalDate.now().minusDays(2)
            )
        ));

        final var found = staffService.hasStaffRole(-1L, "MDI", StaffJobType.KW);

        assertThat(found).isTrue();
    }

    @Test
    public void testDoesNotFindRole() {

        when(staffJobRoleRepository.findAllByAgencyIdAndStaffStaffIdAndRole("MDI", -1L, "KW")).thenReturn(List.of(
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(2),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 2"),
                LocalDate.now().minusDays(1)
            ),
            new StaffJobRole(Staff.builder().staffId(-1L).build(),
                AgencyLocation.builder().id("MDI").build(),
                LocalDate.now().minusDays(3),
                "POS",
                "KW",
                new StaffRole("KW", "Key worker 3"),
                LocalDate.now().minusDays(2)
            )
        ));

        final var found = staffService.hasStaffRole(-1L, "MDI", StaffJobType.KW);

        assertThat(found).isFalse();
    }

    @Nested
    public class TemporaryLog {
        @Test
        public void testTemporaryLog() {

            when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
            when(authenticationFacade.getCurrentUsername()).thenReturn("TEST_USER");
            when(staffUserAccountRepository.findByUsername("TEST_USER"))
                .thenReturn(Optional.of(StaffUserAccount.builder().staff(Staff.builder().staffId(234L).build()).build()));

            staffService.getStaffDetail(ID_SINGLE);

            verify(telemetryClient).trackEvent("staff access accessing other staff details",
                Map.of("currentUsername", "TEST_USER",
                    "userStaffId", String.valueOf(234L),
                    "requestedStaffId", String.valueOf(ID_SINGLE)),
                null);
        }

        @Test
        public void testTemporaryLogNoUsername() {

            when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));

            staffService.getStaffDetail(ID_SINGLE);

            verify(telemetryClient).trackEvent("staff access with no username",
                Map.of("requestedStaffId", String.valueOf(ID_SINGLE)),
                null);
        }

        @Test
        public void testTemporaryLogUserMatchesStaff() {

            when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
            when(authenticationFacade.getCurrentUsername()).thenReturn("TEST_USER");
            when(staffUserAccountRepository.findByUsername("TEST_USER"))
                .thenReturn(Optional.of(StaffUserAccount.builder().staff(Staff.builder().staffId(ID_SINGLE).build()).build()));

            staffService.getStaffDetail(ID_SINGLE);

            verifyNoInteractions(telemetryClient);
        }

        @Test
        public void testTemporaryLogUserNotInNomis() {

            when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
            when(authenticationFacade.getCurrentUsername()).thenReturn("TEST_USER");
            when(staffUserAccountRepository.findByUsername("TEST_USER"))
                .thenReturn(Optional.empty());

            staffService.getStaffDetail(ID_SINGLE);

            verify(telemetryClient).trackEvent("staff access cannot find staff for username",
                Map.of("currentUsername", "TEST_USER",
                    "requestedStaffId", String.valueOf(ID_SINGLE)),
                null);
        }

        @Test
        public void testTemporaryLogSkippedGrantType() {
            // This test is to ensure that the telemetry is not logged when the grant type is client_credentials
            when(staffRepository.findByStaffId(ID_SINGLE)).thenReturn(getValidStaffDetails(ID_SINGLE));
            when(authenticationFacade.getCurrentUsername()).thenReturn("TEST_USER");
            when(authenticationFacade.getGrantType()).thenReturn("client_credentials");

            staffService.getStaffDetail(ID_SINGLE);

            verifyNoInteractions(telemetryClient);
        }
    }

    private Optional<StaffDetail> getValidStaffDetails(final Long staffId) {
        final var staffDetail = StaffDetail.builder().staffId(staffId).firstName("Bob").lastName("Harris").status("ACTIVE").gender("M").dateOfBirth(LocalDate.EPOCH).build();
        return Optional.of(staffDetail);
    }
}
