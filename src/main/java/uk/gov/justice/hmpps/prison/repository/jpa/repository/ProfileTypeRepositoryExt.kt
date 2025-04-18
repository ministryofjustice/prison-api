package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType

fun ProfileTypeRepository.findByTypeAndCategoryAndActiveOrNull(type: String, category: String, active: Boolean): ProfileType? = findByTypeAndCategoryAndActive(type, category, active).orElse(null)

fun ProfileTypeRepository.findByTypeAndCategoryOrNull(type: String, category: String): ProfileType? = findByTypeAndCategory(type, category).orElse(null)
