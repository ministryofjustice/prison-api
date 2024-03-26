package uk.gov.justice.hmpps.prison.dsl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository

@Component
class OffenderAddressBuilderRepository(
  private val offenderRepository: OffenderRepository,
  private val offenderAddressRepository: OffenderAddressRepository,
  private val cityRepository: ReferenceCodeRepository<City>,
  private val countryRepository: ReferenceCodeRepository<Country>,
) {
  fun save(
    offenderId: OffenderId,
    premise: String,
    street: String,
    locality: String,
    cityCode: String,
    postalCode: String,
    countryCode: String,
    primary: Boolean,
    pafValidated: Boolean,
    mail: Boolean,
    noFixedAddress: Boolean,
  ): AddressId = offenderRepository.findOffenderByNomsId(offenderId.offenderNo).orElseThrow().let {
    offenderAddressRepository.save(
      OffenderAddress.builder()
        .offender(it)
        .addressType(null)
        .premise(premise)
        .street(street)
        .locality(locality)
        .city(lookupCity(cityCode))
        .country(lookupCountry(countryCode))
        .postalCode(postalCode)
        .mailFlag(mail.asYN())
        .noFixedAddressFlag(noFixedAddress.asYN())
        .primaryFlag(primary.asYN())
        .build(),
    ).let { address -> AddressId(address.addressId) }
  }

  fun Boolean.asYN() = if (this) "Y" else "N"

  fun lookupCity(cityCode: String): City = cityRepository.findByIdOrNull(City.pk(cityCode))!!
  fun lookupCountry(countryCode: String): Country = countryRepository.findByIdOrNull(Country.pk(countryCode))!!
}

@Component
class OffenderAddressBuilderFactory(
  private val repository: OffenderAddressBuilderRepository,
) {
  fun builder() = OffenderAddressBuilder(repository)
}

@NomisDataDslMarker
class OffenderAddressBuilder(
  private val repository: OffenderAddressBuilderRepository,
) {

  private lateinit var addressId: AddressId

  fun build(
    offenderId: OffenderId,
    premise: String,
    street: String,
    locality: String,
    cityCode: String,
    postalCode: String,
    countryCode: String,
    primary: Boolean,
    pafValidated: Boolean,
    mail: Boolean,
    noFixedAddress: Boolean,
  ): AddressId {
    return repository.save(
      offenderId = offenderId,
      premise = premise,
      street = street,
      locality = locality,
      cityCode = cityCode,
      postalCode = postalCode,
      countryCode = countryCode,
      primary = primary,
      pafValidated = pafValidated,
      mail = mail,
      noFixedAddress = noFixedAddress,
    ).also { addressId = it }
  }
}

data class AddressId(val addressId: Long)
