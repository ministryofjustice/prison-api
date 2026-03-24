package uk.gov.justice.hmpps.prison.repository

import com.google.common.collect.ImmutableMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.api.model.Account
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper
import uk.gov.justice.hmpps.prison.repository.sql.FinanceRepositorySql
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderAdminProcs.CreateTrustAccount
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew
import java.math.BigDecimal
import java.util.Date

@Repository
class FinanceRepository(
  @param:Value("\${api.currency:GBP}") private val currency: String,
  private val insertIntoOffenderTrans: InsertIntoOffenderTrans,
  private val processGlTransNew: ProcessGlTransNew,
  private val createTrustAccount: CreateTrustAccount,
) : RepositoryBase() {
  private val accountMapping: MutableMap<String, FieldMapper> = ImmutableMap.Builder<String, FieldMapper>()
    .put("cash_balance", FieldMapper("cash"))
    .put("spends_balance", FieldMapper("spends"))
    .put("savings_balance", FieldMapper("savings")).build()

  fun getBalances(bookingId: Long, agencyId: String): Account {
    val sql = FinanceRepositorySql.GET_ACCOUNT.sql
    val rowMapper = Row2BeanRowMapper.makeMapping<Account>(Account::class.java, accountMapping)
    val balances = jdbcTemplate.queryForObject<Account>(
      sql,
      createParams("bookingId", bookingId, "agencyId", agencyId),
      rowMapper,
    )
    balances.currency = currency
    return balances
  }

  fun insertIntoOffenderTrans(
    prisonId: String,
    offId: Long,
    offBookId: Long,
    transPostType: String,
    subActType: String,
    transNumber: Long,
    transSeq: Long,
    transAmount: BigDecimal,
    transDesc: String,
    transDate: Date,
  ) {
    val params = MapSqlParameterSource()
      .addValue("p_trans_number", transNumber)
      .addValue("p_trans_seq", transSeq)
      .addValue("p_csld_id", prisonId)
      .addValue("p_off_id", offId)
      .addValue("p_off_book_id", offBookId)
      .addValue("p_trans_post_type", transPostType)
      .addValue("p_trans_type", "OT")
      .addValue("p_trans_desc", transDesc)
      .addValue("p_trans_amount", transAmount)
      .addValue("p_trans_date", transDate)
      .addValue("p_sub_act_type", subActType)
      .addValue("p_deduction_flag", null)
      .addValue("p_pre_ded_amount", null)
      .addValue("p_deduction_type", null)
      .addValue("p_payee_corp_id", null)
      .addValue("p_payee_person_id", null)
      .addValue("p_info_number", null)
      .addValue("p_slip_print_flag", "N")
      .addValue("p_allow_overdrawn", "N")

    insertIntoOffenderTrans.execute(params)
  }

  fun processGlTransNew(
    prisonId: String,
    offId: Long,
    offBookId: Long,
    subActTypeDr: String,
    subActTypeCr: String? = null,
    transNumber: Long,
    transSeq: Long,
    transAmount: BigDecimal,
    transDesc: String,
    transDate: Date,
    transactionType: String,
    moduleName: String,
  ) {
    val params = MapSqlParameterSource()
      .addValue("p_csld_id", prisonId)
      .addValue("p_trans_type", transactionType)
      .addValue("p_operation_type", null)
      .addValue("p_trans_amount", transAmount)
      .addValue("p_trans_number", transNumber)
      .addValue("p_trans_date", transDate)
      .addValue("p_trans_desc", transDesc)
      .addValue("p_trans_seq", transSeq)
      .addValue("p_module_name", moduleName)
      .addValue("p_off_id", offId)
      .addValue("p_off_book_id", offBookId)
      .addValue("p_sub_act_type_dr", subActTypeDr)
      .addValue("p_sub_act_type_cr", subActTypeCr)
      .addValue("p_payee_pers_id", null)
      .addValue("p_payee_corp_id", null)
      .addValue("p_payee_name_text", null)
      .addValue("p_gl_sqnc", 0)
      .addValue("p_off_ded_id", null)
    processGlTransNew.execute(params)
  }

  fun createTrustAccount(
    caseloadId: String,
    offBookId: Long,
    rootOffId: Long,
    fromAgencyLocationId: String,
    movementReasonCode: String,
    shadowId: Long?,
    receiptNumber: Long?,
    destinationCaseloadId: String,
  ) {
    val params = MapSqlParameterSource()
      .addValue("p_caseload_id", caseloadId)
      .addValue("p_off_book_id", offBookId)
      .addValue("p_root_off_id", rootOffId)
      .addValue("p_from_agy_loc_id", fromAgencyLocationId)
      .addValue("p_mvmt_reason_code", movementReasonCode)
      .addValue("p_shadow_id", shadowId)
      .addValue("p_receipt_no", receiptNumber)
      .addValue("p_dest_caseload_id", destinationCaseloadId)

    createTrustAccount.execute(params)
  }
}
