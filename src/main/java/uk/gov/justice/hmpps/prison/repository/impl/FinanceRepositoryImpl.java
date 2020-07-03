package uk.gov.justice.hmpps.prison.repository.impl;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;

import java.util.Map;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private @Value("${api.currency:GBP}")
    String currency;

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Account getBalances(final long bookingId) {
        final var sql = getQuery("GET_ACCOUNT");
        final var rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        final var balances = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), rowMapper);
        balances.setCurrency(currency);
        return balances;
    }
}
