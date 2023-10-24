package uk.gov.justice.hmpps.prison.security

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER

@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
annotation class VerifyAgencyAccess(val overrideRoles: Array<String> = [])
