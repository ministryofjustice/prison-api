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

    @Value("${cache.timeout.seconds.casenote:3600}")
    private int caseNoteTimeoutSeconds;

    @Value("${cache.timeout.seconds.agency:3600}")
    private int agencyTimeoutSeconds;

    @Value("${cache.timeout.seconds.booking:3600}")
    private int bookingTimeoutSeconds;

    @Value("${cache.timeout.seconds.location:3600}")
    private int locationTimeoutSeconds;

    @Value("${cache.timeout.seconds.offender.search:300}")
    private int offenderSearchTimeoutSeconds;

    @Bean(destroyMethod="shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();

        config.addCache(config("referenceDomain", 500, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("referenceCodesByDomain", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("referenceCodeByDomainAndCode", 1000, referenceDataTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("caseNoteTypesByCaseLoadType", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("caseNoteTypesWithSubTypesByCaseLoadType", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("usedCaseNoteTypesWithSubTypes", 100, caseNoteTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findByStaffId", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findRolesByUsername", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findApiRolesByUsername", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("loadUserByUsername", 5000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findByStaffIdAndStaffUserType", 1000, userTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("getCaseLoadsByUsername", 1000, caseLoadTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findAgenciesByUsername", 1000, agencyTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("verifyBookingAccess", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("getBookingAgency", 1000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("findLocationsByAgencyAndType", 1000, locationTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("getGroup", 200, locationTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("searchForOffenderBookings", 1000, offenderSearchTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("findInmate", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("basicInmateDetail", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

        config.addCache(config("bookingAssessments", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("offenderAssessments", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalMarks", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingProfileInformation", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalCharacteristics", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingPhysicalAttributes", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("offenderIdentifiers", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));
        config.addCache(config("bookingIdByOffenderNo", 10000, bookingTimeoutSeconds, MemoryStoreEvictionPolicy.LRU));

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
