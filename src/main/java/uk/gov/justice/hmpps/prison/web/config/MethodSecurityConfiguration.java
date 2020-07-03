package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {}