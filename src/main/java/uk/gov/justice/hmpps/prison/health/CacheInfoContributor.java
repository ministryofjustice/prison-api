package uk.gov.justice.hmpps.prison.health;

import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheInfoContributor implements InfoContributor {

    private final CacheManager cacheManager;

    @Autowired
    public CacheInfoContributor(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void contribute(final Info.Builder builder) {
        final Map<String, String> results = new HashMap<>();
        long memory = 0;

        for (final var name : cacheManager.getCacheNames()) {
            final var cache = cacheManager.getCache(name);
            final var statistics = cache.getStatistics();
            results.put(String.format("%-50s", name), String.format("%5d /%5d  hits:%4d misses:%4d exp:%4d notfound:%4d bytes:%d",
                    cache.getKeysNoDuplicateCheck().size(),
                    cache.getCacheConfiguration().getMaxEntriesLocalHeap(),
                    statistics.cacheHitCount(),
                    statistics.cacheMissCount(),
                    statistics.cacheMissExpiredCount(),
                    statistics.cacheMissNotFoundCount(),
                    statistics.getLocalHeapSizeInBytes()
            ));
            memory += statistics.getLocalHeapSizeInBytes();

        }
        builder.withDetail("caches", results);
        builder.withDetail("cacheTotalMemoryMB", String.format("%.2f", memory / 1048576.0));

        final var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final var uptime = runtimeMXBean.getUptime();
        final var seconds = uptime / 1000;
        final var days = seconds / 86400.0;
        builder.withDetail("uptime", String.format("%.1f days (%d seconds)", days, seconds));
    }
}
