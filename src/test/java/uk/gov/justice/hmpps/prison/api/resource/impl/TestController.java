package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.hmpps.prison.web.config.ApplicationInsightsConfiguration;
import uk.gov.justice.hmpps.prison.web.config.ClientTrackingConfigurationTest;
import uk.gov.justice.hmpps.prison.web.filter.UserMdcFilter;

@ActiveProfiles("test")
@Import({UserMdcFilter.class, StubUserSecurityUtilsConfig.class, ApplicationInsightsConfiguration.class, ClientTrackingConfigurationTest.class})
public abstract class TestController {
    @Autowired
    protected MockMvc mockMvc;
}
