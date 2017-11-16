package net.syscon.elite.web.config;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.servlet.Filter;

@Configuration
public class ApplicationInsightsConfiguration {

    private final String telemetryKey;

    @Autowired
    public ApplicationInsightsConfiguration(@Value("${application.insights.ikey}") String telemetryKey) {
        this.telemetryKey = telemetryKey;
    }

    @Bean
    @Conditional(AppInsightKeyPresentCondition.class)
    public FilterRegistrationBean aiFilterRegistration(Filter webRequestTrackingFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(webRequestTrackingFilter);
        registration.addUrlPatterns("/**");
        registration.setOrder(1);
        return registration;

    }

    @Bean(name = "webRequestTrackingFilter")
    @Conditional(AppInsightKeyPresentCondition.class)
    public Filter webRequestTrackingFilter() {
        return new WebRequestTrackingFilter();
    }

    @Bean
    @Conditional(AppInsightKeyPresentCondition.class)
    public String telemetryConfig() {
        TelemetryConfiguration.getActive().setInstrumentationKey(telemetryKey);
        return telemetryKey;
    }

    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

    public static class AppInsightKeyPresentCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            final String telemetryKey = context.getEnvironment().getProperty("application.insights.ikey");
            return StringUtils.isNotBlank(telemetryKey);
        }
    }
}