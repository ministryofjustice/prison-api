package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Account;
import net.syscon.elite.repository.FinanceRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private @Value("${api.currency:GBP}") String currency;

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Account getBalances(final long bookingId) {
        final String sql = getQuery("GET_ACCOUNT");
        final RowMapper<Account> rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        final Account balances = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), rowMapper);
        balances.setCurrency(currency);
        return balances;
    }
}
