package uk.gov.justice.hmpps.prison.web.config;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper;
import uk.gov.justice.hmpps.prison.util.JwtParameters;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ExtendWith(SpringExtension.class)
@Import({JwtAuthenticationHelper.class, ClientTrackingTelemetryModule.class})
@ContextConfiguration(initializers = {ConfigFileApplicationContextInitializer.class})
@ActiveProfiles("test")
public class ClientTrackingTelemetryModuleTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ClientTrackingTelemetryModule clientTrackingTelemetryModule;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    @BeforeEach
    public void setup() {
        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(1L));
    }

    @AfterEach
    public void tearDown() {
        ThreadContext.remove();
    }

    @Test
    public void shouldAddClientIdAndUserNameToInsightTelemetry() {

        final var token = createJwt("bob", List.of(), 1L);

        final var req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        final var res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).containsOnly(entry("username", "bob"), entry("clientId", "prison-api-client"), entry("clientIpAddress", "127.0.0.1"));
    }

    @Test
    public void shouldAddClientIdAndUserNameToInsightTelemetryEvenIfTokenExpired() {

        final var token = createJwt("Fred", List.of(), -1L);

        final var req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        final var res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).containsOnly(entry("username", "Fred"), entry("clientId", "prison-api-client"), entry("clientIpAddress", "127.0.0.1"));
    }

    private String createJwt(final String user, final List<String> roles, final Long duration) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(duration))
                .build());
    }

}
