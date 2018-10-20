package net.syscon.elite.health;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

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
            results.put(name, String.format("%d / %d hits:%d misses:%d bytes:%d",
                    cache.getKeysNoDuplicateCheck().size(),
                    cache.getCacheConfiguration().getMaxEntriesLocalHeap(),
                    statistics.cacheHitCount(),
                    statistics.cacheMissCount(),
                    statistics.getLocalHeapSizeInBytes()
            ));
            memory += statistics.getLocalHeapSizeInBytes();

        }
        builder.withDetail("caches", results);
        builder.withDetail("cacheTotalMemoryMB", String.format("%.2f", memory / 1048576.0));
    }
}