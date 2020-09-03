package uk.gov.justice.hmpps.prison.repository.impl;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private final String currency;
    private final InsertIntoOffenderTrans insertIntoOffenderTrans;
    private final ProcessGlTransNew processGlTransNew;

    public FinanceRepositoryImpl( @Value("${api.currency:GBP}") final String currency, final InsertIntoOffenderTrans insertIntoOffenderTrans, final ProcessGlTransNew processGlTransNew) {
        this.currency = currency;
        this.insertIntoOffenderTrans = insertIntoOffenderTrans;
        this.processGlTransNew = processGlTransNew;
    }

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Account getBalances(final long bookingId, final String agencyId) {
        final var sql = getQuery("GET_ACCOUNT");
        final var rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        final var balances = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "agencyId", agencyId), rowMapper);
        balances.setCurrency(currency);
        return balances;
    }

    @Override
    public void insertIntoOffenderTrans(final String prisonId, final long offId, final long offBookId,
                                        final String subActType, final long transPostType, final long transNumber,
                                        final long transSeq, final BigDecimal transAmount, final String transDesc,
                                        final Date transDate) {
        final var params = new MapSqlParameterSource()
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
                .addValue("p_allow_overdrawn", "N");

        insertIntoOffenderTrans.execute(params);
    }

    @Override
    public void processGlTransNew(final String prisonId, final long offId, final long offBookId, final Object subActTypeDr,
                                  final long subActTypeCr, final long transNumber, final long transSeq,
                                  final BigDecimal transAmount, final String transDesc, final Date transDate) {
        final var params = new MapSqlParameterSource()
                .addValue("p_csld_id", prisonId)
                .addValue("p_trans_type", "OT")
                .addValue("p_operation_type", null)
                .addValue("p_trans_amount", transAmount)
                .addValue("p_trans_number", transNumber)
                .addValue("p_trans_date", transDate)
                .addValue("p_trans_desc", transDesc)
                .addValue("p_trans_seq", transSeq)
                .addValue("p_module_name", "OTDSUBAT")
                .addValue("p_off_id", offId)
                .addValue("p_off_book_id", offBookId)
                .addValue("p_sub_act_type_dr", subActTypeDr)
                .addValue("p_sub_act_type_cr", subActTypeCr)
                .addValue("p_payee_pers_id", "")
                .addValue("p_payee_corp_id", "")
                .addValue("p_payee_name_text", "")
                .addValue("p_gl_sqnc", 0)
                .addValue("p_off_ded_id", "");
        processGlTransNew.execute(params);
    }
}
