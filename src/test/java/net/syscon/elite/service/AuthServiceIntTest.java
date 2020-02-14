package net.syscon.elite.service;

import net.syscon.elite.repository.jpa.repository.StaffUserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
public class AuthServiceIntTest {

    @MockBean
    StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    private AuthService authService;

    @Test
    @WithMockUser(roles = {"AUTH_NOMIS"})
    public void getUserDetails_NotFound() {

        assertThatThrownBy(() -> authService.getNomisUserByUsername("bob"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Resource with id [bob] not found.");
    }

    @Test
    @WithMockUser(roles = {"INCORRECT_ROLE"})
    public void getUserDetails_NoRole() {

        assertThatThrownBy(() -> authService.getNomisUserByUsername("bob"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access is denied");
    }

}
