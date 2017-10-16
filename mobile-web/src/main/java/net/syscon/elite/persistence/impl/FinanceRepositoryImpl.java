package net.syscon.elite.persistence.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Account;
import net.syscon.elite.persistence.FinanceRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Optional<Account> getAccount(final long bookingId, final Set<String> caseloads) {

        final String sql = getQuery("GET_ACCOUNT");
        final RowMapper<Account> rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        Account account;
        account = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseLoadIds", caseloads),
                rowMapper);
        if (account.getCash() == null && account.getSavings() == null && account.getSpends() == null) {
            account = null;
        }
        return Optional.ofNullable(account);
    }
}
