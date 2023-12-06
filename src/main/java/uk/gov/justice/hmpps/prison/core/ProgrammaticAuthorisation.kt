package uk.gov.justice.hmpps.prison.core

import org.springframework.stereotype.Component

/**
 * Annotation to denote that this API call uses logic in the code to e.g. limit data returned to the users caseload etc. and so needs no role protection.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ProgrammaticAuthorisation(val value: String)
