package net.syscon.elite.persistence.impl;

import java.util.Map;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.persistence.FinanceRepository;
import net.syscon.elite.persistence.mapping.*;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Account getBalances(final long bookingId) {
        final String sql = getQuery("GET_ACCOUNT");
        final RowMapper<Account> rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), rowMapper);
    }
}
