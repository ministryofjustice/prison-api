package uk.gov.justice.hmpps.prison.core

import org.springframework.stereotype.Component

/**
 * Annotation to denote that this API call only returns reference data, and therefore needs no role protection.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ReferenceData(val description: String)
