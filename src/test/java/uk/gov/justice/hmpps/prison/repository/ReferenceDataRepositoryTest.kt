package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.cache.test.autoconfigure.AutoConfigureCache
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.web.config.CacheConfig
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.util.Objects
import java.util.function.Consumer

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureCache
@ContextConfiguration(classes = [PersistenceConfigs::class, CacheConfig::class])
class ReferenceDataRepositoryTest(
  @Autowired private val repository: ReferenceDataRepository,
) {
  @Test
  fun testGetReferenceDomainExists() {
    val refDomain =
      repository.getReferenceDomain("NOTE_SOURCE")

    assertThat(refDomain.isPresent).isTrue()
    assertThat(refDomain.get().domain).isEqualTo("NOTE_SOURCE")
    assertThat(refDomain.get().description).isEqualTo("Case Note Sources")
  }

  @Test
  fun testGetReferenceDomainNotExists() {
    val refDomain =
      repository.getReferenceDomain("UNKNOWN")

    assertThat(refDomain.isPresent).isFalse()
  }

  @Test
  fun testGetReferenceCodeByDomainAndCodeWithSubCodes() {
    val refCode =
      repository.getReferenceCodeByDomainAndCode("ALERT", "A", true)

    assertThat(refCode.isPresent).isTrue()
    assertThat(refCode.get().subCodes).isNotNull()
    assertThat(refCode.get().subCodes).isNotEmpty()
  }

  @Test
  fun testGetReferenceCodeByDomainAndCodeWithoutSubCodes() {
    val refCode =
      repository.getReferenceCodeByDomainAndCode("ALERT", "A", false)

    assertThat(refCode.isPresent).isTrue()
    assertThat(refCode.get().subCodes).isEmpty()
  }

  // Tests (without retrieval of sub-codes) ordering by code (ascending) and 0-10 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithoutSubCodes1() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", false, "code", Order.ASC, 0, 10)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(10)
    assertThat(refCodes.totalRecords).isEqualTo(59)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(10)

    // Verify sorting
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[9].code).isEqualTo("COMMS")

    // Verify no sub-codes returned
    refCodes.getItems().forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has no sub-codes", rc.code)
          .isNullOrEmpty()
      },
    )
  }

  // Tests (without retrieval of sub-codes) ordering by code (descending) and 15-30 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithoutSubCodes2() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", false, "code", Order.DESC, 15, 15)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(15)
    assertThat(refCodes.totalRecords).isEqualTo(59)
    assertThat(refCodes.pageOffset).isEqualTo(15)
    assertThat(refCodes.pageLimit).isEqualTo(15)

    // Verify sorting
    assertThat(refCodes.getItems()[0].code).isEqualTo("REQUIREMENT")
    assertThat(refCodes.getItems()[9].code).isEqualTo("PA")
    assertThat(refCodes.getItems()[14].code).isEqualTo("MISC")

    // Verify no sub-codes returned
    refCodes.getItems().forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has no sub-codes", rc.code)
          .isNullOrEmpty()
      },
    )
  }

  // Tests (with retrieval of sub-codes) ordering by code (ascending) and 0-10 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithSubCodes1() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", true, "code", Order.ASC, 0, 10)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(10)
    assertThat(refCodes.totalRecords).isEqualTo(48)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(10)

    // Verify sorting of codes
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[9].code).isEqualTo("CRT")

    // Verify sub-codes returned for all items
    verifySubCodesPresent(refCodes.getItems())

    // Verify quantity of sub-codes returned
    assertThat(refCodes.getItems()[0].subCodes).hasSize(22)
    assertThat(refCodes.getItems()[8].subCodes).hasSize(2)
    assertThat(refCodes.getItems()[9].subCodes).hasSize(15)

    // Verify sorting of sub-codes
    assertThat(refCodes.getItems()[0].subCodes[0].code).isEqualTo("CPS")
    assertThat(refCodes.getItems()[0].subCodes[21].code).isEqualTo("SOU")
    assertThat(refCodes.getItems()[9].subCodes[0].code).isEqualTo("APP")
    assertThat(refCodes.getItems()[9].subCodes[14].code).isEqualTo("TRA")

    // Verify correct sub-code relationship to parent code
    verifySubCodeParentCodeRelationship(refCodes.getItems())
  }

  // Tests (with retrieval of sub-codes) ordering by code (descending) and 15-30 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithSubCodes2() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", true, "code", Order.DESC, 15, 16)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(16)
    assertThat(refCodes.totalRecords).isEqualTo(48)
    assertThat(refCodes.pageOffset).isEqualTo(15)
    assertThat(refCodes.pageLimit).isEqualTo(16)

    // Verify sorting of codes
    assertThat(refCodes.getItems()[0].code).isEqualTo("REC")
    assertThat(refCodes.getItems()[10].code).isEqualTo("MIGRATION")
    assertThat(refCodes.getItems()[15].code).isEqualTo("GEN")

    // Verify sub-codes returned or not
    verifySubCodesPresent(refCodes.getItems())

    // Verify quantity of sub-codes returned
    assertThat(refCodes.getItems()[0].subCodes).hasSize(9)
    assertThat(refCodes.getItems()[10].subCodes).hasSize(1)
    assertThat(refCodes.getItems()[15].subCodes).hasSize(1)

    // Verify sorting of sub-codes
    assertThat(refCodes.getItems()[0].subCodes[0].code).isEqualTo("RI")
    assertThat(refCodes.getItems()[0].subCodes[8].code).isEqualTo("APPRECALL")
    assertThat(refCodes.getItems()[10].subCodes[0].code).isEqualTo("MISC")
    assertThat(refCodes.getItems()[15].subCodes[0].code).isEqualTo("HIST")

    // Verify correct sub-code relationship to parent code
    verifySubCodeParentCodeRelationship(refCodes.getItems())
  }

  // Tests (without retrieval of sub-codes) ordering by code (ascending) and 0-1000 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithoutSubCodes3() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", false, "code", Order.ASC, 0, 1000)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(59)
    assertThat(refCodes.totalRecords).isEqualTo(59)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(1000)

    // Verify sorting
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[9].code).isEqualTo("COMMS")
    assertThat(refCodes.getItems()[58].code).isEqualTo("VLU")

    // Verify no sub-codes returned
    refCodes.getItems().forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has no sub-codes", rc.code)
          .isNullOrEmpty()
      },
    )
  }

  // Tests (with retrieval of sub-codes) ordering by code (ascending) and 0-1000 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithSubCodes3() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", true, "code", Order.ASC, 0, 1000)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(48)
    assertThat(refCodes.totalRecords).isEqualTo(48)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(1000)

    // Verify sorting of codes
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[9].code).isEqualTo("CRT")
    assertThat(refCodes.getItems()[47].code).isEqualTo("VICTIM")

    // Verify sub-codes returned or not
    verifySubCodesPresent(refCodes.getItems())

    // Verify quantity of sub-codes returned
    assertThat(refCodes.getItems()[0].subCodes).hasSize(22)
    assertThat(refCodes.getItems()[9].subCodes).hasSize(15)
    assertThat(refCodes.getItems()[46].subCodes).hasSize(2)

    // Verify sorting of sub-codes
    assertThat(refCodes.getItems()[0].subCodes[0].code).isEqualTo("CPS")
    assertThat(refCodes.getItems()[0].subCodes[21].code).isEqualTo("SOU")
    assertThat(refCodes.getItems()[9].subCodes[0].code).isEqualTo("APP")
    assertThat(refCodes.getItems()[9].subCodes[14].code).isEqualTo("TRA")
    assertThat(refCodes.getItems()[47].subCodes[0].code).isEqualTo("INFO_COMPLET")
    assertThat(refCodes.getItems()[47].subCodes[1].code).isEqualTo("INFO_UPDATE")

    // Verify correct sub-code relationship to parent code
    verifySubCodeParentCodeRelationship(refCodes.getItems())
  }

  // Tests (without retrieval of sub-codes) ordering by description (ascending) and 0-10 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithoutSubCodes4() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", false, "description", Order.ASC, 0, 10)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(10)
    assertThat(refCodes.totalRecords).isEqualTo(59)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(10)

    // Verify sorting
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[0].description).isEqualTo("Accredited Programme")
    assertThat(refCodes.getItems()[9].code).isEqualTo("CHAP")
    assertThat(refCodes.getItems()[9].description).isEqualTo("Chaplaincy")

    // Verify no sub-codes returned
    refCodes.getItems().forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has no sub-codes", rc.code)
          .isNullOrEmpty()
      },
    )
  }

  // Tests (without retrieval of sub-codes) ordering by description (descending) and 15-30 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithoutSubCodes5() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", false, "description", Order.DESC, 15, 15)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(15)
    assertThat(refCodes.totalRecords).isEqualTo(59)
    assertThat(refCodes.pageOffset).isEqualTo(15)
    assertThat(refCodes.pageLimit).isEqualTo(15)

    // Verify sorting
    assertThat(refCodes.getItems()[0].code).isEqualTo("RR")
    assertThat(refCodes.getItems()[0].description).isEqualTo("Release Report")
    assertThat(refCodes.getItems()[9].code).isEqualTo("OM")
    assertThat(refCodes.getItems()[9].description).isEqualTo("Offender Management")
    assertThat(refCodes.getItems()[14].code).isEqualTo("MIGRATION")
    assertThat(refCodes.getItems()[14].description).isEqualTo("Migration")

    // Verify no sub-codes returned
    refCodes.getItems().forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has no sub-codes", rc.code)
          .isNullOrEmpty()
      },
    )
  }

  // Tests (with retrieval of sub-codes) ordering by description (ascending) and 0-10 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithSubCodes4() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", true, "description", Order.ASC, 0, 10)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(10)
    assertThat(refCodes.totalRecords).isEqualTo(48)
    assertThat(refCodes.pageOffset).isEqualTo(0)
    assertThat(refCodes.pageLimit).isEqualTo(10)

    // Verify sorting of codes
    assertThat(refCodes.getItems()[0].code).isEqualTo("ACP")
    assertThat(refCodes.getItems()[0].description).isEqualTo("Accredited Programme")
    assertThat(refCodes.getItems()[9].code).isEqualTo("COMMS")
    assertThat(refCodes.getItems()[9].description).isEqualTo("Communication")

    // Verify sub-codes returned or not
    verifySubCodesPresent(refCodes.getItems())

    // Verify quantity of sub-codes returned
    assertThat(refCodes.getItems()[0].subCodes).hasSize(22)
    assertThat(refCodes.getItems()[9].subCodes).hasSize(2)

    // Verify sorting of sub-codes
    assertThat(refCodes.getItems()[0].subCodes[0].code).isEqualTo("CPS")
    assertThat(refCodes.getItems()[0].subCodes[0].description)
      .isEqualTo("Core Programme Session")
    assertThat(refCodes.getItems()[0].subCodes[21].code).isEqualTo("SOU")
    assertThat(refCodes.getItems()[0].subCodes[21].description)
      .isEqualTo("Statement Of Understanding Signed")
    assertThat(refCodes.getItems()[9].subCodes[0].code).isEqualTo("COM_IN")
    assertThat(refCodes.getItems()[9].subCodes[0].description)
      .isEqualTo("Communication IN")
    assertThat(refCodes.getItems()[9].subCodes[1].code).isEqualTo("COM_OUT")
    assertThat(refCodes.getItems()[9].subCodes[1].description)
      .isEqualTo("Communication OUT")

    // Verify correct sub-code relationship to parent code
    verifySubCodeParentCodeRelationship(refCodes.getItems())
  }

  // Tests (with retrieval of sub-codes) ordering by description (descending) and 15-30 pagination.
  @Test
  fun testGetReferenceCodesByDomainWithSubCodes6() {
    val refCodes =
      repository.getReferenceCodesByDomain("TASK_TYPE", true, "description", Order.DESC, 15, 16)

    assertThat(refCodes).isNotNull()
    assertThat(refCodes.getItems()).isNotNull()
    assertThat(refCodes.getItems()).isNotEmpty()
    assertThat(refCodes.getItems()).hasSize(16)
    assertThat(refCodes.totalRecords).isEqualTo(48)
    assertThat(refCodes.pageOffset).isEqualTo(15)
    assertThat(refCodes.pageLimit).isEqualTo(16)

    // Verify sorting of codes
    assertThat(refCodes.getItems()[0].code).isEqualTo("PRISON")
    assertThat(refCodes.getItems()[0].description).isEqualTo("Prison")
    assertThat(refCodes.getItems()[10].code).isEqualTo("MHT")
    assertThat(refCodes.getItems()[10].description)
      .isEqualTo("Mental Health Treatment Requirement")
    assertThat(refCodes.getItems()[15].code).isEqualTo("FILEINSP")
    assertThat(refCodes.getItems()[15].description).isEqualTo("File Review Inspection")

    // Verify sub-codes returned or not
    verifySubCodesPresent(refCodes.getItems())

    // Verify quantity of sub-codes returned
    assertThat(refCodes.getItems()[0].subCodes).hasSize(1)
    assertThat(refCodes.getItems()[10].subCodes).hasSize(3)
    assertThat(refCodes.getItems()[15].subCodes).hasSize(3)

    // Verify sorting of sub-codes
    assertThat(refCodes.getItems()[0].subCodes[0].code).isEqualTo("RELEASE")
    assertThat(refCodes.getItems()[0].subCodes[0].description).isEqualTo("Release")
    assertThat(refCodes.getItems()[10].subCodes[0].code).isEqualTo("MHTRTO")
    assertThat(refCodes.getItems()[10].subCodes[0].description)
      .isEqualTo("Report to Office")
    assertThat(refCodes.getItems()[10].subCodes[2].code).isEqualTo("MHTIATP")
    assertThat(refCodes.getItems()[10].subCodes[2].description)
      .isEqualTo("Initial Appointment w.Treatment Provider")
    assertThat(refCodes.getItems()[15].subCodes[0].code).isEqualTo("BYSPO")
    assertThat(refCodes.getItems()[15].subCodes[0].description).isEqualTo("By SPO")
    assertThat(refCodes.getItems()[15].subCodes[1].code).isEqualTo("BYINSP")
    assertThat(refCodes.getItems()[15].subCodes[1].description)
      .isEqualTo("By Inspector")

    // Verify correct sub-code relationship to parent code
    verifySubCodeParentCodeRelationship(refCodes.getItems())
  }

  private fun verifySubCodesPresent(refCodes: MutableList<ReferenceCode?>) {
    refCodes.forEach(
      Consumer { rc: ReferenceCode ->
        assertThat(rc.subCodes).`as`("Check code [%s] has sub-codes", rc.code)
          .isNotEmpty()
      },
    )
  }

  private fun verifySubCodeParentCodeRelationship(refCodes: MutableList<ReferenceCode>) {
    refCodes.forEach(
      Consumer { rc: ReferenceCode ->
        if (Objects.nonNull(rc.subCodes)) {
          rc.subCodes.forEach(
            Consumer { sc: ReferenceCode ->
              assertThat(sc.parentDomain)
                .`as`("Check sub-code [%s] parent domain against code [%s] domain", sc.code, rc.code)
                .isEqualTo(rc.domain)
              assertThat(sc.parentCode)
                .`as`("Check sub-code [%s] parent code against code [%s] code", sc.code, rc.code)
                .isEqualTo(rc.code)
            },
          )
        }
      },
    )
  }
}
