package net.syscon.elite.security;

import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.syscon.elite.security.ExternalIdAuthenticationHelper.REQUEST_PARAM_USER_ID;
import static net.syscon.elite.security.ExternalIdAuthenticationHelper.REQUEST_PARAM_USER_ID_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExternalIdAuthenticationHelperTest {
    @Mock
    private UserService userService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private ExternalIdAuthenticationHelper authHelper;

    @Test(expected = OAuth2AccessDeniedException.class)
    public void testGetUserDetailsEmptyUserIdType() {
        String TEST_USER_ID_TYPE = "";
        String TEST_USER_ID = "someone@example.com";

        Map<String,String> requestParams = mockRequestParams(TEST_USER_ID_TYPE, TEST_USER_ID);

        authHelper.getUserDetails(requestParams);
    }

    @Test(expected = OAuth2AccessDeniedException.class)
    public void testGetUserDetailsEmptyUserId() {
        String TEST_USER_ID_TYPE = "YJAF";
        String TEST_USER_ID = "";

        Map<String,String> requestParams = mockRequestParams(TEST_USER_ID_TYPE, TEST_USER_ID);

        authHelper.getUserDetails(requestParams);
    }

    @Test(expected = OAuth2AccessDeniedException.class)
    public void testGetUserDetailsInvalidUserIdentity() {
        String TEST_USER_ID_TYPE = "YJAF";
        String TEST_USER_ID = "someone@example.com";

        Map<String,String> requestParams = mockRequestParams(TEST_USER_ID_TYPE, TEST_USER_ID);

        when(userService.getUserByExternalIdentifier(eq(TEST_USER_ID_TYPE), eq(TEST_USER_ID), eq(Boolean.TRUE)))
                .thenThrow(EntityNotFoundException.withMessage("Not found."));

        authHelper.getUserDetails(requestParams);
    }

    @Test
    public void testGetUserDetailsValidUserIdentity() {
        String TEST_USER_ID_TYPE = "YJAF";
        String TEST_USER_ID = "someone@example.com";
        String TEST_USER_NAME = "NJ6F4X";

        Map<String,String> requestParams = mockRequestParams(TEST_USER_ID_TYPE, TEST_USER_ID);
        UserDetail mockUserDetail = UserDetail.builder().username(TEST_USER_NAME).build();
        UserDetails mockUserDetails = new UserDetailsImpl(TEST_USER_NAME, null, Collections.emptySet(), null);

        when(userService.getUserByExternalIdentifier(eq(TEST_USER_ID_TYPE), eq(TEST_USER_ID), eq(Boolean.TRUE))).thenReturn(mockUserDetail);
        when(userDetailsService.loadUserByUsername(eq(TEST_USER_NAME))).thenReturn(mockUserDetails);

        UserDetails userDetails = authHelper.getUserDetails(requestParams);

        assertThat(userDetails.getUsername()).isEqualTo(TEST_USER_NAME);

        verify(userService, times(1)).getUserByExternalIdentifier(eq(TEST_USER_ID_TYPE), eq(TEST_USER_ID), eq(Boolean.TRUE));
        verify(userDetailsService, times(1)).loadUserByUsername(eq(TEST_USER_NAME));
    }

    private Map<String,String> mockRequestParams(String userIdType, String userId) {
        Map<String,String> params = new HashMap<>();

        params.put(REQUEST_PARAM_USER_ID_TYPE, userIdType);
        params.put(REQUEST_PARAM_USER_ID, userId);

        return params;
    }
}
