package net.syscon.elite.web.config;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class ApplicationInsightsConfiguration {

    @Bean
    public String telemetryConfig(@Value("${application.insights.ikey}") String telemetryKey) {
        if (telemetryKey != null) {
            TelemetryConfiguration.getActive().setInstrumentationKey(telemetryKey);
        }
        return telemetryKey;
    }

    @Bean
    public FilterRegistrationBean aiFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(WebRequestTrackingFilter());
        registration.addUrlPatterns("/**");
        registration.setOrder(1);
        return registration;
    }

    @Bean(name = "WebRequestTrackingFilter")
    public Filter WebRequestTrackingFilter() {
        return new WebRequestTrackingFilter();
    }
}