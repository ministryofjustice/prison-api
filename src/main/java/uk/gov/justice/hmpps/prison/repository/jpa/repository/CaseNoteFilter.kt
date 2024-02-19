package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.QueryParamHelper
import java.time.LocalDate

class CaseNoteFilter(
  val bookingId: Long,
  private val prisonId: String? = null,
  private val startDate: LocalDate? = null,
  private val endDate: LocalDate? = null,
  type: String? = null,
  subType: String? = null,
  typesSubTypes: List<String>? = null,
) : Specification<OffenderCaseNote> {

  private val typesAndSubTypes = if (!type.isNullOrEmpty()) {
    if (!typesSubTypes.isNullOrEmpty()) {
      throw BadRequestException("Both type and typesSubTypes are set, please only use one to filter.")
    }
    if (subType.isNullOrEmpty()) listOf(type) else listOf("$type+$subType")
  } else {
    typesSubTypes
  }

  override fun toPredicate(root: Root<OffenderCaseNote>, query: CriteriaQuery<*>, cb: CriteriaBuilder): Predicate {
    val predicateBuilder = mutableListOf(cb.equal(root.get<Any>("offenderBooking").get<Any>("bookingId"), bookingId))
    if (!prisonId.isNullOrBlank()) {
      predicateBuilder.add(cb.equal(root.get<Any>("agencyLocation").get<Any>("id"), prisonId))
    }
    startDate?.let {
      predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("occurrenceDate"), startDate))
    }
    endDate?.let {
      predicateBuilder.add(cb.lessThan(root.get("occurrenceDate"), endDate.plusDays(1)))
    }
    typesAndSubTypes?.let {
      predicateBuilder.add(getTypesPredicate(root, cb))
    }
    return cb.and(*predicateBuilder.toTypedArray())
  }

  private fun getTypesPredicate(root: Root<OffenderCaseNote>, cb: CriteriaBuilder): Predicate {
    val typesAndSubTypes = QueryParamHelper.splitTypes(typesAndSubTypes)
    val typesPredicates = typesAndSubTypes.entries
      .map { (key, value) ->
        if (value.isEmpty()) {
          cb.equal(root.get<Any>("type").get<Any>("code"), key)
        } else {
          getSubtypesPredicate(root, cb, key, value)
        }
      }

    // if we only have one entry then just return that, which prevents an or clause with only one entry
    if (typesPredicates.size == 1) return typesPredicates[0]

    return cb.or(*typesPredicates.toTypedArray())
  }

  private fun getSubtypesPredicate(
    root: Root<OffenderCaseNote>,
    cb: CriteriaBuilder,
    type: String,
    subTypes: List<String>,
  ): Predicate {
    val typePredicateOrBuilder = mutableListOf(cb.equal(root.get<Any>("type").get<Any>("code"), type))
    val inTypes = cb.`in`(root.get<Any>("subType").get<Any>("code")).apply {
      subTypes.forEach { value(it) }
    }
    typePredicateOrBuilder.add(inTypes)
    return cb.and(*typePredicateOrBuilder.toTypedArray())
  }
}
