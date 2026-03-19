package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.api.resource.FinanceHold
import uk.gov.justice.hmpps.prison.repository.storedprocs.AddFinanceHold
import uk.gov.justice.hmpps.prison.repository.storedprocs.RemoveFinanceHold
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata
import java.math.BigDecimal

@Repository
class FinanceHoldsRepository(
  private val addHoldProcedure: AddFinanceHold,
  private val removeHoldProcedure: RemoveFinanceHold,
) {
  fun addHold(
    prisonId: String,
    nomisId: String,
    rootOffenderId: Long,
    accountCode: String,
    amount: BigDecimal,
    holdDescription: String? = null,
  ): FinanceHold {
    val params = MapSqlParameterSource()
      .addValue(StoreProcMetadata.P_NOMS_ID, nomisId)
      .addValue(StoreProcMetadata.P_ROOT_OFFENDER_ID, rootOffenderId)
      .addValue(StoreProcMetadata.P_AGY_LOC_ID, prisonId)
      // TODO
      // .addValue(StoreProcMetadata.P_SUB_ACCOUNT_TYPE, accountCode)
      .addValue(StoreProcMetadata.P_TXN_ENTRY_AMOUNT, amount)
      .addValue(StoreProcMetadata.P_TXN_ENTRY_DESC, holdDescription ?: "Add Hold")

    val result: MutableMap<String, Any?> = addHoldProcedure.execute(params)
    return FinanceHold((result[StoreProcMetadata.P_HOLD_NUMBER] as BigDecimal).toLong())
  }

  fun removeHold(
    prisonId: String,
    nomisId: String,
    rootOffenderId: Long,
    holdNumber: Long,
    releaseHoldDescription: String? = null,
  ) {
    val params = MapSqlParameterSource()
      .addValue(StoreProcMetadata.P_NOMS_ID, nomisId)
      .addValue(StoreProcMetadata.P_ROOT_OFFENDER_ID, rootOffenderId)
      .addValue(StoreProcMetadata.P_AGY_LOC_ID, prisonId)
      .addValue(StoreProcMetadata.P_TXN_ENTRY_DESC, releaseHoldDescription ?: "Remove Hold")
      .addValue(StoreProcMetadata.P_HOLD_NUMBER, holdNumber)
    removeHoldProcedure.execute(params)
  }
}
