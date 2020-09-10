package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.auth.UserPersonDetails;
import uk.gov.justice.hmpps.prison.service.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AuthUserResourceTest {

    @Mock
    private AuthService service;

    private AuthUserResource authUserResource;

    @BeforeEach
    public void setUp() {
        authUserResource = new AuthUserResource(service);
    }

    @Test
    public void getUserDetails() {

        when(service.getNomisUserByUsername(anyString())).thenReturn(UserPersonDetails.builder().build());
        authUserResource.getAuthUserDetails("bob");
        verify(service).getNomisUserByUsername("BOB");
        verifyNoMoreInteractions(service);
    }


}
