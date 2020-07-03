package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.hmpps.prison.api.model.auth.UserPersonDetails;
import uk.gov.justice.hmpps.prison.service.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


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
