package net.syscon.elite.api.resource;

import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.jpa.model.*;
import net.syscon.elite.repository.jpa.repository.StaffUserAccountRepository;
import net.syscon.elite.service.EntityNotFoundException;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AuthUserResourceIntTest extends ResourceTest {

    @MockBean
    private StaffUserAccountRepository staffUserAccountRepository;

    @Test
    public void getAuthUser() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("BOB", List.of("ROLE_AUTH_NOMIS"), null);

        when(staffUserAccountRepository.findById(anyString())).thenReturn(buildStaffUserAccountRepositoryResponse());

        final var responseEntity = testRestTemplate.exchange("/auth/user/bob", HttpMethod.GET, requestEntity, String.class);

        assertThatStatus(responseEntity, 200);
        assertThatJsonFileAndStatus(responseEntity, 200, "user-person-details.json");
    }

    @Test
    public void getAuthUser_NotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("BAB_JOHN", List.of("ROLE_AUTH_NOMIS"), null);

        when(staffUserAccountRepository.findById("bad_john")).thenThrow(EntityNotFoundException.withId("bad_john"));

        final var responseEntity = testRestTemplate.exchange("/auth/user/bad_john", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 404, "user-person-details-not-found.json");
    }

    Optional<StaffUserAccount> buildStaffUserAccountRepositoryResponse() {
        return Optional.ofNullable(StaffUserAccount.builder()
                .username("BOB")
                .staff(Staff.builder()
                        .staffId(1234567L)
                        .firstName("BOB")
                        .lastName("SMITH")
                        .status("ACTIVE")
                        .build())
                .type("SPECIAL")
                .activeCaseLoadId("SOME")
                .roles(List.of(UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("BOB").caseload("WEB").roleId(1L).build())
                                .role(Role.builder().id(1L).code("Role1").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("BOB").caseload("WEB").roleId(2L).build())
                                .role(Role.builder().id(2L).code("Role2").build())
                                .build()
                ))
                .accountDetail(AccountDetail.builder()
                        .username("BOB")
                        .accountStatus("OPEN")
                        .profile("TAG")
                        .passwordExpiry(LocalDateTime.parse("2021-02-01T10:11:12")).build())
                .build());
    }
}
