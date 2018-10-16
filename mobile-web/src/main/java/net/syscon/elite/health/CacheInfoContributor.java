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

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, String> results = new HashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            final Cache cache = cacheManager.getCache(name);
            final int size = cache.getKeysNoDuplicateCheck().size();
            final long maxSize = cache.getCacheConfiguration().getMaxEntriesLocalHeap();
            final StatisticsGateway statistics = cache.getStatistics();
            final long hitCount = statistics.cacheHitCount();
            final long missCount = statistics.cacheMissCount();
            results.put(name, String.format("%d / %d hits:%d misses:%d", size, maxSize, hitCount, missCount));
        }
        builder.withDetail("caches", results);
    }
}