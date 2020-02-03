package net.syscon.elite.api.resource;

import net.syscon.elite.api.model.auth.UserPersonDetails;
import net.syscon.elite.service.AuthService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AuthUserResourceTest {

    @Mock
    private AuthService service;

    private AuthUserResource authUserResource;

    @Before
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
