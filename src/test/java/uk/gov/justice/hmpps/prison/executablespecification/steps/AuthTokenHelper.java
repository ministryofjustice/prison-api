package uk.gov.justice.hmpps.prison.executablespecification.steps;

import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper;
import uk.gov.justice.hmpps.prison.util.JwtParameters;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class AuthTokenHelper {

    private final Map<String, String> tokens = new HashMap<>();
    private String currentToken;
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    public enum AuthToken {
        PRISON_API_USER,
        API_TEST_USER,
        RENEGADE_USER,
        NO_CASELOAD_USER,
        NORMAL_USER,
        GLOBAL_SEARCH,
        VIEW_PRISONER_DATA,
        LOCAL_ADMIN,
        ADMIN_TOKEN,
        SUPER_ADMIN,
        INACTIVE_BOOKING_USER,
        SYSTEM_USER_READ_WRITE,
        CATEGORISATION_CREATE,
        CATEGORISATION_APPROVE,
        LAA_USER,
        BULK_APPOINTMENTS_USER,
        // ITAG_USER with ROLE_MAINTAIN_IEP and scope ['read','write]
        MAINTAIN_IEP,
        PAY,
        UPDATE_ALERT,
        COURT_HEARING_MAINTAINER,
        PRISON_MOVE_MAINTAINER,
        CREATE_BOOKING_USER,
        SMOKE_TEST,
        REF_DATA_MAINTAINER,
        REF_DATA_MAINTAINER_NO_WRITE,
        UNAUTHORISED_USER
    }


    public AuthTokenHelper(final JwtAuthenticationHelper jwtAuthenticationHelper) {
        this.jwtAuthenticationHelper = jwtAuthenticationHelper;

        tokens.put(String.valueOf(AuthToken.PRISON_API_USER), prisonApiUser());
        tokens.put(String.valueOf(AuthToken.API_TEST_USER), apiTestUser());
        tokens.put(String.valueOf(AuthToken.RENEGADE_USER), renegadeUser());
        tokens.put(String.valueOf(AuthToken.NO_CASELOAD_USER), noCaseloadUser());
        tokens.put(String.valueOf(AuthToken.GLOBAL_SEARCH), globalSearchUser());
        tokens.put(String.valueOf(AuthToken.VIEW_PRISONER_DATA), viewPrisonerDataUser());
        tokens.put(String.valueOf(AuthToken.LOCAL_ADMIN), localAdmin());
        tokens.put(String.valueOf(AuthToken.ADMIN_TOKEN), adminToken());
        tokens.put(String.valueOf(AuthToken.SUPER_ADMIN), superAdmin());
        tokens.put(String.valueOf(AuthToken.INACTIVE_BOOKING_USER), inactiveBookingUser());
        tokens.put(String.valueOf(AuthToken.SYSTEM_USER_READ_WRITE), systemUserReadWrite());
        tokens.put(String.valueOf(AuthToken.CATEGORISATION_CREATE), categorisationCreate());
        tokens.put(String.valueOf(AuthToken.CATEGORISATION_APPROVE), categorisationApprove());
        tokens.put(String.valueOf(AuthToken.LAA_USER), laaUser());
        tokens.put(String.valueOf(AuthToken.BULK_APPOINTMENTS_USER), bulkAppointmentsUser());
        tokens.put(String.valueOf(AuthToken.MAINTAIN_IEP), maintainIep());
        tokens.put(String.valueOf(AuthToken.NORMAL_USER), normalUser());
        tokens.put(String.valueOf(AuthToken.PAY), payUser());
        tokens.put(String.valueOf(AuthToken.UPDATE_ALERT), updateAlert());
        tokens.put(String.valueOf(AuthToken.COURT_HEARING_MAINTAINER), courtHearingMaintainer());
        tokens.put(String.valueOf(AuthToken.PRISON_MOVE_MAINTAINER), prisonMoveMaintiner());
        tokens.put(String.valueOf(AuthToken.CREATE_BOOKING_USER), createBookingApiUser());
        tokens.put(String.valueOf(AuthToken.SMOKE_TEST), createSmokeTestUser());
        tokens.put(String.valueOf(AuthToken.REF_DATA_MAINTAINER), createRefDataMaintainerUser(true));
        tokens.put(String.valueOf(AuthToken.REF_DATA_MAINTAINER_NO_WRITE), createRefDataMaintainerUser(false));
        tokens.put(String.valueOf(AuthToken.UNAUTHORISED_USER), createUnauthorisedUser());
    }

    public String getToken() {
        return currentToken;
    }

    public void setToken(final AuthToken clientId) {
        this.currentToken = getToken(clientId);
    }

    public String getToken(final AuthToken clientId) {
        return tokens.get(String.valueOf(clientId));
    }

    private String prisonApiUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("PRISON_API_USER")
                        .scope(singletonList("read"))
                        .roles(emptyList())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String apiTestUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("API_TEST_USER")
                        .scope(singletonList("read"))
                        .roles(emptyList())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String renegadeUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("RENEGADE")
                        .scope(singletonList("read"))
                        .roles(emptyList())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String noCaseloadUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("RO_USER")
                        .scope(List.of("read", "write"))
                        .roles(singletonList("ROLE_LICENCE_RO"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String globalSearchUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .clientId("deliusnewtech")
                        .internalUser(false)
                        .scope(List.of("read"))
                        .roles(singletonList("ROLE_GLOBAL_SEARCH"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String viewPrisonerDataUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .clientId("aclient")
                        .internalUser(false)
                        .scope(List.of("read"))
                        .roles(singletonList("ROLE_VIEW_PRISONER_DATA"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String systemUserReadWrite() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .clientId("PRISON_API_USER")
                        .internalUser(false)
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_SYSTEM_USER"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String localAdmin() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER_ADM")
                        .scope(List.of("read"))
                        .roles(List.of("ROLE_MAINTAIN_ACCESS_ROLES", "ROLE_KW_MIGRATION", "ROLE_OAUTH_ADMIN"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );

    }

    private String adminToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read"))
                        .roles(List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN", "ROLE_GLOBAL_SEARCH", "ROLE_OMIC_ADMIN"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String superAdmin() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .clientId("PRISON_API_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN", "ROLE_GLOBAL_SEARCH", "ROLE_MAINTAIN_ACCESS_ROLES", "ROLE_OMIC_ADMIN"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String inactiveBookingUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("RO_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_GLOBAL_SEARCH", "ROLE_INACTIVE_BOOKINGS", "ROLE_LICENCE_RO"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );


    }

    private String categorisationCreate() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN", "ROLE_GLOBAL_SEARCH", "ROLE_CREATE_CATEGORISATION", "ROLE_OMIC_ADMIN"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String categorisationApprove() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_APPROVE_CATEGORISATION"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String laaUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("LAA_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_MAINTAIN_ACCESS_ROLES"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String bulkAppointmentsUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("API_TEST_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_BULK_APPOINTMENTS"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String maintainIep() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_MAINTAIN_IEP"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String normalUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String payUser() {
        return jwtAuthenticationHelper.createJwt(JwtParameters
                .builder()
                .username("ITAG_USER")
                .roles(List.of("ROLE_PAY"))
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build()
        );
    }

    private String updateAlert() {
        return jwtAuthenticationHelper.createJwt(JwtParameters
                .builder()
                .username("ITAG_USER")
                .roles(List.of("ROLE_UPDATE_ALERT"))
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build()
        );

    }

    private String courtHearingMaintainer() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("DOES_NOT_EXIST")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_COURT_HEARING_MAINTAINER"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String prisonMoveMaintiner() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("DOES_NOT_EXIST")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_PRISON_MOVE_MAINTAINER"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String createBookingApiUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_BOOKING_CREATE", "ROLE_RELEASE_PRISONER", "ROLE_TRANSFER_PRISONER", "ROLE_INACTIVE_BOOKINGS", "ROLE_VIEW_PRISONER_DATA"))
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String createSmokeTestUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("SMOKE_TEST_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of("ROLE_SMOKE_TEST"))
                    .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String createUnauthorisedUser() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("UNAUTHORISED_USER")
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String createRefDataMaintainerUser(boolean allowWriteScope) {
        return jwtAuthenticationHelper.createJwt(
            JwtParameters.builder()
                .username("ITAG_USER")
                .scope(allowWriteScope ? List.of("read", "write") : List.of("read"))
                .roles(List.of("ROLE_MAINTAIN_REF_DATA"))
                .expiryTime(Duration.ofDays(365 * 10))
                .build()
        );
    }
}