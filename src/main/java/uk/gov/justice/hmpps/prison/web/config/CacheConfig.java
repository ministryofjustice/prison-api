package uk.gov.justice.hmpps.prison.web.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceDomain;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.support.Page;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig implements CachingConfigurer {

    public static final String GET_AGENCY_LOCATIONS_BOOKED = "getAgencyLocationsBooked";

    @Value("${cache.timeout.seconds.reference-data:3600}")
    private int referenceDataTimeoutSeconds;

    @Value("${cache.timeout.seconds.user:3600}")
    private int userTimeoutSeconds;

    @Value("${cache.timeout.seconds.casenote:3600}")
    private int caseNoteTimeoutSeconds;

    @Value("${cache.timeout.seconds.agency:3600}")
    private int agencyTimeoutSeconds;

    @Value("${cache.timeout.seconds.location:3600}")
    private int locationTimeoutSeconds;

    @Value("${cache.timeout.seconds.activity:3600}")
    private int activityTimeoutSeconds;

    @Bean
    public JCacheManagerCustomizer cacheConfiguration() {
        return cm -> {
            cm.createCache("referenceDomain", config(String.class, ReferenceDomain.class, 500, referenceDataTimeoutSeconds));
            cm.enableStatistics("referenceDomain", true);
            cm.createCache("referenceCodesByDomain", config(SimpleKey.class, Page.class, 1000, referenceDataTimeoutSeconds));
            cm.enableStatistics("referenceCodesByDomain", true);
            cm.createCache("referenceCodeByDomainAndCode", config(String.class, ReferenceCode.class, 1000, referenceDataTimeoutSeconds));
            cm.enableStatistics("referenceCodeByDomainAndCode", true);

            cm.createCache("caseNoteTypesByCaseLoadType", config(String.class, List.class, 100, caseNoteTimeoutSeconds));
            cm.enableStatistics("caseNoteTypesByCaseLoadType", true);
            cm.createCache("getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag", config(SimpleKey.class, List.class, 100, caseNoteTimeoutSeconds));
            cm.enableStatistics("getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag", true);
            cm.createCache("usedCaseNoteTypesWithSubTypes", config(SimpleKey.class, List.class, 100, caseNoteTimeoutSeconds));
            cm.enableStatistics("usedCaseNoteTypesWithSubTypes", true);

            cm.createCache("findByStaffId", config(Long.class, StaffDetail.class, 1000, userTimeoutSeconds));
            cm.enableStatistics("findByStaffId", true);
            cm.createCache("loadUserByUsername", config(String.class, UserDetails.class, 5000, userTimeoutSeconds));
            cm.enableStatistics("loadUserByUsername", true);

            cm.createCache("findAgenciesByUsername", config(String.class, List.class, 1000, userTimeoutSeconds));
            cm.enableStatistics("findAgenciesByUsername", true);
            cm.createCache("findByStaffIdAndStaffUserType", config(SimpleKey.class, UserDetails.class, 1000, userTimeoutSeconds));
            cm.enableStatistics("findByStaffIdAndStaffUserType", true);
            cm.createCache(GET_AGENCY_LOCATIONS_BOOKED, config(String.class, List.class, 500, activityTimeoutSeconds));
            cm.enableStatistics(GET_AGENCY_LOCATIONS_BOOKED, true);
        };
    }

    public static <K, V> javax.cache.configuration.Configuration<K, V> config(Class<K> keyType, Class<V> valueType, final int maxElements, final int timeoutSeconds) {
        return Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder
                .newCacheConfigurationBuilder(keyType, valueType, ResourcePoolsBuilder.heap(maxElements))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timeoutSeconds))));
    }
}
