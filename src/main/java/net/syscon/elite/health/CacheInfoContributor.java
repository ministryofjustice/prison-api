package net.syscon.elite.health;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheInfoContributor implements InfoContributor {

    private final CacheManager cacheManager;

    @Autowired
    public CacheInfoContributor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, String> results = new HashMap<>();
        long memory = 0;

        for (String name : cacheManager.getCacheNames()) {
            final Cache cache = cacheManager.getCache(name);
            final StatisticsGateway statistics = cache.getStatistics();
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

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long seconds = uptime / 1000;
        double days = seconds / 86400.0;
        builder.withDetail("uptime", String.format("%.1f days (%d seconds)", days, seconds));
    }
}
