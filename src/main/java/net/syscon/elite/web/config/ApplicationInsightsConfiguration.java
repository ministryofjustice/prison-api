package net.syscon.elite.web.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Application insights now controlled by the spring-boot-starter dependency.  However when the key is not specified
 * we don't get a telemetry bean and application won't start.  Therefore need this backup configuration.
 */
@Configuration
public class ApplicationInsightsConfiguration {


    @Bean
    @Conditional(AppInsightKeyAbsentCondition.class)
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

    public static class AppInsightKeyAbsentCondition implements Condition {

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            final var telemetryKey = context.getEnvironment().getProperty("application.insights.ikey");
            return StringUtils.isNotBlank(telemetryKey);
        }
    }
}