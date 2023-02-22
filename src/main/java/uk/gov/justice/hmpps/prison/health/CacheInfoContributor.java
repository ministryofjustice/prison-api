package uk.gov.justice.hmpps.prison.health;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class CacheInfoContributor implements InfoContributor {
    @Override
    public void contribute(final Info.Builder builder) {
        final var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final var uptime = runtimeMXBean.getUptime();
        final var seconds = uptime / 1000;
        final var days = seconds / 86400.0;
        builder.withDetail("uptime", String.format("%.1f days (%d seconds)", days, seconds));
    }
}
