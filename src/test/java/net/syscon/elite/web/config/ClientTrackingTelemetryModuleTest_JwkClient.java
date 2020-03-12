package net.syscon.elite.web.config;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import net.syscon.elite.util.JwtAuthenticationHelper;
import net.syscon.elite.util.JwtParameters;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@Import({JwtAuthenticationHelper.class, ClientTrackingTelemetryModule.class, JwkClient.class})
@ContextConfiguration(initializers = {ConfigFileApplicationContextInitializer.class})
@ActiveProfiles({"test", "test-jwk"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ClientTrackingTelemetryModuleTest_JwkClient {

    @Autowired
    private ClientTrackingTelemetryModule clientTrackingTelemetryModule;

    @Autowired
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    @Rule
    public JwkMockServer jwkServer = new JwkMockServer(9090);

    @Before
    public void setup() {
        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(1L));
        jwkServer.stubJwkServer();
        jwkServer.start();
    }

    @After
    public void tearDown() {
        ThreadContext.remove();
        jwkServer.stop();
        jwkServer.resetAll();
    }

    @Test
    public void shouldAddClientIdAndUserNameToInsightTelemetry() {

        final var token = createJwt("bob", List.of(), 1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).hasSize(3);
        assertThat(insightTelemetry.get("username")).isEqualTo("bob");
        assertThat(insightTelemetry.get("clientId")).isEqualTo("elite2apiclient");
        assertThat(insightTelemetry.get("clientIpAddress")).isEqualTo("127.0.0.1");
    }

    @Test
    public void shouldNotAddClientIdAndUserNameToInsightTelemetryAsTokenExpired() {

        final var token = createJwt("Fred", List.of(), -1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).isEmpty();
    }

    @Test
    public void shouldRetrieveJwkSetFromServer_onlyOnce() {

        final var token = createJwt("bob", List.of(), 1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);
        clientTrackingTelemetryModule.onBeginRequest(req, res);

        jwkServer.verify(1, getRequestedFor(urlEqualTo("/auth/.well-known/jwks.json")));
    }

    private String createJwt(final String user, final List<String> roles, Long duration) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(duration))
                .build());
    }

}
