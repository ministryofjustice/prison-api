package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.OK
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER

class AgencyResourceTest : ResourceTest() {
  @Test
  fun testCanFindAgenciesByType() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/type/INST",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "agencies_by_type.json")
  }

  @Test
  fun testCanFindAgenciesByTypeAndCourtTypeCode() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/agencies/type/CRT?courtType=YC",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )
    assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCode_CC.json")
  }

  @Test
  fun testCanFindAgenciesByTypeAndSuppressFormatOfDescription() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/agencies/type/CRT?courtType=YC&skipFormatLocation=true",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )
    assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_suppress_format.json")
  }

  @Test
  fun testCanFindAgenciesByTypeAndDeprecatedJurisdictionCode() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/agencies/type/CRT?jurisdictionCode=YC",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )
    assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCode_CC.json")
  }

  @Test
  fun testCanFindAgenciesByTypeAndCourtTypeCodes() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/agencies/type/CRT?courtType=YC&courtType=MC",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )
    assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCodes_CC_MC.json")
  }

  @Test
  fun testCanFindAgencyById() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/LEI",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "single_agency.json")
  }

  @Test
  fun testCanFindAgencyWithAddress() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/BMI?withAddresses=true",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "single_agency_with_address.json")
  }

  @Test
  fun testCanUpdateAgencyById() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)

    val update1 = mapOf(
      "agencyId" to "LEI",
      "description" to "LEEDS",
      "longDescription" to "This is a prison based in Leeds",
      "agencyType" to "INST",
      "active" to "false",
    )

    val httpEntity1 = createHttpEntity(token, update1)

    val response1 = testRestTemplate.exchange(
      "/api/agencies/LEI",
      PUT,
      httpEntity1,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response1, 200, "single_agency_updated_inactive.json")

    val update2 = mapOf(
      "agencyId" to "LEI",
      "description" to "LEEDS",
      "longDescription" to "HMP LEEDS",
      "agencyType" to "INST",
      "active" to "true",
    )

    val httpEntity2 = createHttpEntity(token, update2)

    val response2 = testRestTemplate.exchange(
      "/api/agencies/LEI",
      PUT,
      httpEntity2,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response2, 200, "single_agency_updated.json")

    val getResponse = testRestTemplate.exchange(
      "/api/agencies/LEI?skipFormatLocation=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponse, 200, "single_agency_updated.json")
  }

  @Test
  fun testCanCreateNewAgency() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "SHFCRT",
      "description" to "Sheffield Crown Court",
      "longDescription" to "This is a court in Sheffield",
      "agencyType" to "CRT",
      "courtType" to "CC",
      "active" to "true",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 201, "new_agency.json")

    val getResponse = testRestTemplate.exchange(
      "/api/agencies/SHFCRT",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponse, 200, "new_agency.json")
  }

  @Test
  fun testCanCreateNewAddress() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "addressType" to "BUS",
      "premise" to "Leeds Prison",
      "town" to "29059",
      "postalCode" to "LS12 5TH",
      "county" to "W.YORKSHIRE",
      "country" to "ENG",
      "primary" to "true",
      "startDate" to "2006-01-12",
      "locality" to "North Leeds",
      "comment" to "Some text",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/LEI/addresses",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 201, "new_address.json")

    val getResponse = testRestTemplate.exchange(
      "/api/agencies/LEI?withAddresses=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponse, 200, "new_agency_address.json")

    val addressId = (Gson().fromJson<Map<*, *>>(response.body, MutableMap::class.java)["addressId"] as Double?)!!.toInt()

    val updateBody = mapOf(
      "addressType" to "BUS",
      "premise" to "Leeds Prison",
      "town" to "29059",
      "postalCode" to "LS12 5TH",
      "county" to "W.YORKSHIRE",
      "country" to "ENG",
      "primary" to "false",
      "startDate" to "2006-01-12",
      "endDate" to "2021-01-04",
      "locality" to "North Leeds",
    )

    val updateHttpEntity = createHttpEntity(token, updateBody)

    val updateResponse = testRestTemplate.exchange(
      "/api/agencies/LEI/addresses/$addressId",
      PUT,
      updateHttpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(updateResponse, 200, "updated_address.json")

    val deleteResponse = testRestTemplate.exchange(
      "/api/agencies/LEI/addresses/$addressId",
      DELETE,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(deleteResponse, 200)

    val getResponseAgain = testRestTemplate.exchange(
      "/api/agencies/LEI?withAddresses=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponseAgain, 200, "new_agency_address_updated.json")
  }

  @Test
  fun testCanCreateNewPhone() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "number" to "0115 2345222",
      "type" to "FAX",
      "ext" to "121",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/BMI/addresses/-3/phones",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 201, "new_phone.json")

    val getResponse = testRestTemplate.exchange(
      "/api/agencies/BMI?withAddresses=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponse, 200, "new_agency_address_phone.json")

    val phoneId = (Gson().fromJson<Map<*, *>>(response.body, MutableMap::class.java)["phoneId"] as Double?)!!.toInt()

    val updateBody = mapOf(
      "number" to "0115 2345221",
      "type" to "FAX",
      "ext" to "122",
    )

    val updateHttpEntity = createHttpEntity(token, updateBody)

    val updateResponse = testRestTemplate.exchange(
      "/api/agencies/BMI/addresses/-3/phones/$phoneId",
      PUT,
      updateHttpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(updateResponse, 200, "updated_phone.json")

    val deleteResponse = testRestTemplate.exchange(
      "/api/agencies/BMI/addresses/-3/phones/$phoneId",
      DELETE,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(deleteResponse, 200)

    val getResponseAgain = testRestTemplate.exchange(
      "/api/agencies/BMI?withAddresses=true",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(getResponseAgain, 200, "new_agency_address_phone_updated.json")
  }

  @Test
  fun testCantCreateExistingAgency() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "MDI",
      "description" to "Moorland",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 409)
  }

  @Test
  fun testCantCreateAgencyWithNoType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "XXI",
      "description" to "Will Fail",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateCourtWithInvalidCourtType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "SHEFCC",
      "description" to "Will Fail",
      "agencyType" to "CRT",
      "courtType" to "BADTYPE",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateCourtWithNoCourtType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "SHEFCC",
      "description" to "Will Fail",
      "agencyType" to "CRT",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 404)
  }

  @Test
  fun testCantCreateAgencyWithBadAgencyId() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "   ",
      "description" to "Will Fail",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWith1charAgencyId() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "X",
      "description" to "Will Fail",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWith1charDescription() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "MDI",
      "description" to " ",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWithInvalidChars() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "%$£",
      "description" to "Cool",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWithInvalidId() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "XXIDDDDDDDDDDDD",
      "description" to "Will Fail",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWithInvalidType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "XXI",
      "description" to "Will Fail",
      "agencyType" to "BLOB",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantUupdateAgencyWithNoType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "description" to "Will Fail",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/MDI",
      PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantUpdateAgencyWithInvalidType() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "description" to "Will Fail",
      "agencyType" to "BLOB",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/MDI",
      PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantCreateAgencyWithNoDescription() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
    val body = mapOf(
      "agencyId" to "XXI",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 400)
  }

  @Test
  fun testCantUpdateAgencyWithOutRequiredRole() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val body = mapOf(
      "description" to "Moorland -fail",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/MDI",
      PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 403)
  }

  @Test
  fun testCantUpdateAgencyWithoutWriteScope() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER_NO_WRITE)
    val body = mapOf(
      "description" to "Moorland -fail",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies/MDI",
      PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 403)
  }

  @Test
  fun testCantCreateAgencyWithoutWriteScope() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER_NO_WRITE)
    val body = mapOf(
      "agencyId" to "XXI",
      "description" to "Will fail",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 403)
  }

  @Test
  fun testCantCreateAgencyWithoutRequireRole() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val body = mapOf(
      "agencyId" to "XXI",
      "description" to "Will fail",
      "agencyType" to "INST",
    )

    val httpEntity = createHttpEntity(token, body)

    val response = testRestTemplate.exchange(
      "/api/agencies",
      POST,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatStatus(response, 403)
  }

  @Test
  fun testCanFindAgenciesByTypePlusInactive() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/type/INST?activeOnly={activeOnly}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
      "false",
    )

    assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json")
  }

  @Test
  fun testCanFindCourtsPlusAddresses() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/type/CRT?withAddresses={withAddresses}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
      "true",
    )

    assertThatJsonFileAndStatus(response, 200, "courts_by_type.json")
  }

  @Test
  fun testCanFindReceptionsWithCapacity() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/LEI/receptionsWithCapacity",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )
    assertThatJsonFileAndStatus(response, 200, "reception_with_capacity.json")
  }

  @Test
  fun testCanFindCellsWithCapacity() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/LEI/cellsWithCapacity",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "cells_with_capacity.json")
  }

  @Test
  fun testCanFindCellsWithCapacity_filtered() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/LEI/cellsWithCapacity?attribute=DO",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThatJsonFileAndStatus(response, 200, "cells_with_capacity_filtered.json")
  }

  @Test
  fun testEstablishmentTypesForMoorlandPrison() {
    val token = authTokenHelper.getToken(NORMAL_USER)

    val httpEntity = createHttpEntity(token, null)

    val response = testRestTemplate.exchange(
      "/api/agencies/MDI/establishment-types",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<AgencyEstablishmentTypes>() {
      },
    )

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body!!.agencyId).isEqualTo("MDI")
    assertThat(response.body!!.establishmentTypes).containsExactlyInAnyOrder(
      AgencyEstablishmentType.builder().code("CM").description("Closed (Male)").build(),
      AgencyEstablishmentType.builder().code("CNOMIS").description("C-NOMIS Establishment").build(),
      AgencyEstablishmentType.builder().code("IM").description("Closed Young Offender Institute (Male)").build(),
    )
  }
}
