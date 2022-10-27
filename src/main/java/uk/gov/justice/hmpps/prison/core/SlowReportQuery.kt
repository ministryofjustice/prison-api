package uk.gov.justice.hmpps.prison.core

/**
 * A custom annotation to indicate that this API call is slow or frequent (i.e. uses a lot of database resources) <b>and
 * read-only</b>: therefore the Replica readonly database should be accessed, not the primary.
 *
 * As a rule of thumb, use this annotation on any read-only endpoint that takes more than about 100ms, but avoid using it
 * on endpoints that are for a specific prisoner where there is any chance that data will be queried as a result of
 * receiving an event, or to refresh a web page after a change was made. I.e. where the Replica DB may not yet be
 * up-to-date because a modification has just occurred.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SlowReportQuery
