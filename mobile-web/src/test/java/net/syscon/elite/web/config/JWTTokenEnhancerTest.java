package net.syscon.elite.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.syscon.elite.security.ExternalIdAuthenticationHelper;
import net.syscon.elite.security.UserDetailsImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.syscon.elite.web.config.JWTTokenEnhancer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JWTTokenEnhancerTest {
    private static final String TEST_ACCESS_TOKEN_VALUE = "8c8e3a98-5072-4369-8d2c-d4a8d442e3f7";

    @Mock
    private OAuth2Authentication authentication;

    @Mock
    private ExternalIdAuthenticationHelper externalIdAuthenticationHelper;

    @InjectMocks
    private JWTTokenEnhancer tokenEnhancer;

    private OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(TEST_ACCESS_TOKEN_VALUE);
    private OAuth2Request authRequest;

    @Before
    public void setUp() {
        Set<GrantedAuthority> initialAuthorities =
                Stream.of(new SimpleGrantedAuthority("ROLE_SYSTEM_USER")).collect(Collectors.toSet());

        Set<String> initialScope = Stream.of("read", "admin").collect(Collectors.toSet());

        authRequest = new OAuth2Request(null, "testClient", initialAuthorities, false,
                initialScope, null, null, null, null);

        doAnswer((invocation) -> authRequest).when(authentication).getOAuth2Request();
    }

    // When client credentials authentication request is made
    // Then internal user property is false
    @Test
    public void testEnhanceClientOnly() throws Exception {
        when(authentication.isClientOnly()).thenReturn(true);

        OAuth2AccessToken enhancedToken = tokenEnhancer.enhance(accessToken, authentication);

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_INTERNAL_USER);
        assertThat(enhancedToken.getAdditionalInformation().get(ADD_INFO_INTERNAL_USER)).isEqualTo(Boolean.FALSE);

        verifyTokenSerializationDeserialization(enhancedToken);

        verify(authentication, times(1)).isClientOnly();
    }

    // When password authentication request is made
    // And external user identifiers are not provided as request parameters
    // Then internal user property is true
    @Test
    public void testEnhanceUserOnly() throws Exception {
        when(authentication.isClientOnly()).thenReturn(false);

        OAuth2AccessToken enhancedToken = tokenEnhancer.enhance(accessToken, authentication);

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_INTERNAL_USER);
        assertThat(enhancedToken.getAdditionalInformation().get(ADD_INFO_INTERNAL_USER)).isEqualTo(Boolean.TRUE);

        verifyTokenSerializationDeserialization(enhancedToken);

        verify(authentication, times(1)).isClientOnly();
    }

    // When password authentication request is made
    // And external user identifiers are provided as request parameters
    // Then internal user property is true
    // And no attempt is made to process the request parameters
    @Test
    public void testEnhanceUserOnlyWithExternalUserIdParams() throws Exception {
        when(authentication.isClientOnly()).thenReturn(false);

        OAuth2AccessToken enhancedToken = tokenEnhancer.enhance(accessToken, authentication);

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_INTERNAL_USER);
        assertThat(enhancedToken.getAdditionalInformation().get(ADD_INFO_INTERNAL_USER)).isEqualTo(Boolean.TRUE);

        verifyTokenSerializationDeserialization(enhancedToken);

        verify(authentication, times(1)).isClientOnly();
        verify(externalIdAuthenticationHelper, never()).getUserDetails(anyMap());
    }

    // When client credentials authentication request is made
    // And valid external user identifiers are provided as request parameters
    // Then internal user property is true
    @Test
    public void testEnhanceClientWithValidUserIdentity() throws Exception {
        String TEST_USER_NAME = "NJ6F4X";

        Set<GrantedAuthority> authorities =
                Stream.of(new SimpleGrantedAuthority("ROLE_A"), new SimpleGrantedAuthority("ROLE_Z")).collect(Collectors.toSet());

        UserDetails userDetails = new UserDetailsImpl(TEST_USER_NAME, null, authorities, null);

        when(authentication.isClientOnly()).thenReturn(true);
        when(externalIdAuthenticationHelper.getUserDetails(anyMap())).thenReturn(userDetails);

        OAuth2AccessToken enhancedToken = tokenEnhancer.enhance(accessToken, authentication);

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_INTERNAL_USER);
        assertThat(enhancedToken.getAdditionalInformation().get(ADD_INFO_INTERNAL_USER)).isEqualTo(Boolean.TRUE);

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_USER_NAME);
        assertThat(enhancedToken.getAdditionalInformation().get(ADD_INFO_USER_NAME)).isEqualTo(TEST_USER_NAME);

        assertThat(enhancedToken.getScope()).containsExactlyInAnyOrder("read", "write", "admin");

        assertThat(enhancedToken.getAdditionalInformation()).containsKey(ADD_INFO_AUTHORITIES);

        assertThat((Set<String>) enhancedToken.getAdditionalInformation().get(ADD_INFO_AUTHORITIES))
                .containsExactlyInAnyOrder("ROLE_SYSTEM_USER", "ROLE_A", "ROLE_Z");

        verifyTokenSerializationDeserialization(enhancedToken);

        verify(authentication, times(1)).isClientOnly();
    }

    private void verifyTokenSerializationDeserialization(OAuth2AccessToken accessToken) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String serializedToken = objectMapper.writeValueAsString(accessToken);

        objectMapper.readValue(serializedToken, OAuth2AccessToken.class);
    }
}
