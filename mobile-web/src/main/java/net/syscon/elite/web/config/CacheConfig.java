package net.syscon.elite.web.config;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Value("${cache.timeout.seconds.reference-data:3600}")
    private int referenceDataTimeoutSeconds;

    @Value("${cache.timeout.seconds.user:3600}")
    private int userTimeoutSeconds;

    @Value("${cache.timeout.seconds.caseload:3600}")
    private int caseLoadTimeoutSeconds;

    @Value("${cache.timeout.seconds.booking:3600}")
    private int bookingTimeoutSeconds;

    @Bean(destroyMethod="shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();

        config.addCache(config("caseNoteTypesByType", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteSources", 100, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteSourcesByCode", 10, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("alertTypes", 10000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("alertTypesByType", 100, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("alertTypesByTypeFiltered", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("alertTypesByTypeAndCode", 100, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypes", 10000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypesByCode", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypesByCodeFiltered", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypesByTypeSubType", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findByStaffId", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findRolesByUsername", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findCaseLoadsByUsername", 1000, caseLoadTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("verifyBookingAccess", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    public static CacheConfiguration config(String name, int maxElements, int timeoutSeconds, MemoryStoreEvictionPolicy policy){
        return new CacheConfiguration().name(name)
                .memoryStoreEvictionPolicy(policy)
                .eternal(false)
                .overflowToOffHeap(false)
                .maxEntriesLocalHeap(maxElements)
                .timeToLiveSeconds(timeoutSeconds)
                .timeToIdleSeconds(timeoutSeconds);
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    @Override
    public CacheResolver cacheResolver()    {
        return new SimpleCacheResolver(cacheManager());
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
