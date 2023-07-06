package uk.gov.justice.hmpps.prison.web.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.model.ReferenceDomain
import uk.gov.justice.hmpps.prison.api.model.StaffDetail
import uk.gov.justice.hmpps.prison.api.support.Page
import java.time.Duration
import javax.cache.CacheManager

@Configuration
@EnableCaching(proxyTargetClass = true)
class CacheConfig : CachingConfigurer {
  @Value("\${cache.timeout.seconds.reference-data:3600}")
  private val referenceDataTimeoutSeconds = 0L

  @Value("\${cache.timeout.seconds.user:3600}")
  private val userTimeoutSeconds = 0L

  @Value("\${cache.timeout.seconds.casenote:3600}")
  private val caseNoteTimeoutSeconds = 0L

  @Value("\${cache.timeout.seconds.agency:3600}")
  private val agencyTimeoutSeconds = 0L

  @Value("\${cache.timeout.seconds.activity:3600}")
  private val activityTimeoutSeconds = 0L



  @Bean
  fun cacheConfiguration(): JCacheManagerCustomizer = JCacheManagerCustomizer { cm: CacheManager ->
    // single item cache jwks json with no expiry
    cm.createCache(
      "jwks",
      Eh107Configuration.fromEhcacheCacheConfiguration(
        CacheConfigurationBuilder
          .newCacheConfigurationBuilder(String::class.java, String::class.java, ResourcePoolsBuilder.heap(1)),
      ),
    )

    cm.createCache(
      "referenceDomain",
      String::class.java,
      ReferenceDomain::class.java,
      500,
      referenceDataTimeoutSeconds,
    )
    cm.createCache(
      "referenceCodesByDomain",
      SimpleKey::class.java,
      Page::class.java,
      1000,
      referenceDataTimeoutSeconds,
    )
    cm.createCache(
      "referenceCodeByDomainAndCode",
      String::class.java,
      ReferenceCode::class.java,
      1000,
      referenceDataTimeoutSeconds,
    )
    cm.createCache(
      "getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag",
      SimpleKey::class.java,
      java.util.List::class.java,
      100,
      caseNoteTimeoutSeconds,
    )
    cm.createCache(
      "usedCaseNoteTypesWithSubTypes",
      SimpleKey::class.java,
      java.util.List::class.java,
      100,
      caseNoteTimeoutSeconds,
    )
    cm.createCache(
      "findByStaffId",
      java.lang.Long::class.java,
      StaffDetail::class.java,
      1000,
      userTimeoutSeconds,
    )
    cm.createCache(
      "findAgenciesByUsername",
      String::class.java,
      java.util.List::class.java,
      1000,
      agencyTimeoutSeconds,
    )
    cm.createCache(
      GET_AGENCY_LOCATIONS_BOOKED,
      String::class.java,
      java.util.List::class.java,
      500,
      activityTimeoutSeconds,
    )
  }

  companion object {
    const val GET_AGENCY_LOCATIONS_BOOKED = "getAgencyLocationsBooked"
  }
}

private fun <K, V> CacheManager.createCache(
  cacheName: String,
  keyType: Class<K>,
  valueType: Class<V>,
  maxElements: Long,
  timeoutSeconds: Long,
) {
  val configuration = Eh107Configuration.fromEhcacheCacheConfiguration(
    CacheConfigurationBuilder
      .newCacheConfigurationBuilder(keyType, valueType, ResourcePoolsBuilder.heap(maxElements))
      .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timeoutSeconds))),
  )

  createCache(cacheName, configuration)
}
