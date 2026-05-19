package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.AddressDto
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsage
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsageType
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.County
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.time.LocalDate
import java.util.Optional

class OffenderAddressServiceImplTest {
  private val offenderRepository: OffenderRepository = mock()
  private val offenderAddressRepository: OffenderAddressRepository = mock()

  private val offenderAddressService = OffenderAddressService(offenderRepository, offenderAddressRepository)

  @Test
  fun canRetrieveAddresses() {
    val offenderNo = "off-1"

    val offender = Offender.builder().id(1L).rootOffenderId(1L).build()
    offender.rootOffender = offender
    whenever(offenderRepository.findRootOffenderByNomsId(any()))
      .thenReturn(Optional.of(offender))
    val addresses = listOf(
      OffenderAddress.builder()
        .addressId(-15L)
        .addressType(AddressType("HOME", "Home Address"))
        .offender(offender)
        .noFixedAddressFlag("N")
        .commentText(null)
        .primaryFlag("Y")
        .mailFlag("N")
        .flat("Flat 1")
        .premise("Brook Hamlets")
        .street("Mayfield Drive")
        .locality("Nether Edge")
        .postalCode("B5")
        .country(Country("ENG", "England"))
        .county(County("S.YORKSHIRE", "South Yorkshire"))
        .city(City("25343", "Sheffield"))
        .startDate(LocalDate.of(2016, 8, 2))
        .endDate(null)
        .phones(
          setOf(
            AddressPhone.builder()
              .phoneId(-7L)
              .phoneNo("0114 2345345")
              .phoneType("HOME")
              .extNo("345")
              .build(),
            AddressPhone.builder()
              .phoneId(-8L)
              .phoneNo("0114 2345346")
              .phoneType("BUS")
              .extNo(null)
              .build(),
          ),
        )
        .addressUsages(
          setOf(
            AddressUsage(null, true, AddressUsageType("HDC", "HDC address")),
          ),
        )
        .build(),
      OffenderAddress.builder()
        .addressId(-16L)
        .addressType(AddressType("BUS", "Business Address"))
        .offender(offender)
        .noFixedAddressFlag("Y")
        .commentText(null)
        .primaryFlag("N")
        .mailFlag("N")
        .flat(null)
        .premise(null)
        .street(null)
        .locality(null)
        .postalCode(null)
        .country(Country("ENG", "England"))
        .county(null)
        .city(null)
        .startDate(LocalDate.of(2016, 8, 2))
        .endDate(null)
        .build(),
    )
    whenever(offenderAddressRepository.findByOffenderId(1L)).thenReturn(addresses)

    val results = offenderAddressService.getAddressesByOffenderNo(offenderNo)

    verify(offenderRepository).findRootOffenderByNomsId(offenderNo)

    // ignore Set order for phone and addresses
    val configuration = RecursiveComparisonConfiguration
      .builder()
      .withIgnoreCollectionOrder(true)
      .build()

    assertThat(results)
      .usingRecursiveFieldByFieldElementComparator(configuration)
      .isEqualTo(
        listOf(
          AddressDto.builder()
            .addressType("Home Address")
            .noFixedAddress(false)
            .primary(true)
            .mail(false)
            .comment(null)
            .flat("Flat 1")
            .premise("Brook Hamlets")
            .street("Mayfield Drive")
            .postalCode("B5")
            .locality("Nether Edge")
            .country("England")
            .countryCode("ENG")
            .county("South Yorkshire")
            .countyCode("S.YORKSHIRE")
            .town("Sheffield")
            .townCode("25343")
            .startDate(LocalDate.of(2016, 8, 2))
            .addressId(-15L)
            .phones(
              listOf(
                Telephone.builder()
                  .phoneId(-7L)
                  .number("0114 2345345")
                  .ext("345")
                  .type("HOME")
                  .build(),
                Telephone.builder()
                  .phoneId(-8L)
                  .number("0114 2345346")
                  .ext(null)
                  .type("BUS")
                  .build(),
              ),
            )
            .addressUsages(
              listOf(
                AddressUsageDto.builder()
                  .addressId(-15L)
                  .activeFlag(true)
                  .addressUsage("HDC")
                  .addressUsageDescription("HDC address")
                  .build(),
              ),
            )
            .build(),
          AddressDto.builder()
            .addressType("Business Address")
            .noFixedAddress(true)
            .primary(false)
            .mail(false)
            .comment(null)
            .flat(null)
            .premise(null)
            .street(null)
            .postalCode(null)
            .country("England")
            .countryCode("ENG")
            .county(null)
            .countyCode(null)
            .town(null)
            .townCode(null)
            .startDate(LocalDate.of(2016, 8, 2))
            .addressId(-16L)
            .phones(mutableListOf<Telephone?>())
            .addressUsages(mutableListOf<AddressUsageDto?>())
            .build(),
        ),
      )
  }
}
