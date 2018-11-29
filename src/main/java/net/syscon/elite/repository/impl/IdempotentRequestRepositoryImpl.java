package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.IdempotentRequestRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.support.IdempotentRequestControl;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * IdempotentRequestControl repository implementation.
 */
@Repository
@Slf4j
public class IdempotentRequestRepositoryImpl extends RepositoryBase implements IdempotentRequestRepository {
    private static final StandardBeanPropertyRowMapper<IdempotentRequestControl> IRC_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(IdempotentRequestControl.class);

    @Override
    public IdempotentRequestControl getAndSet(String correlationId) {
        Validate.notBlank(correlationId);

        IdempotentRequestControl irc;

        Optional<IdempotentRequestControl> optIrc = getIdempotentRequestControl(correlationId);

        if (optIrc.isPresent()) {
            irc = optIrc.get();

            if (StringUtils.isBlank(irc.getResponse())) {
                irc.setRequestStatus(IdempotentRequestControl.Status.PENDING);
            } else {
                irc.setRequestStatus(IdempotentRequestControl.Status.COMPLETE);
            }
        } else {
            irc = setIdempotentRequestControl(correlationId);
        }

        return irc;
    }

    @Override
    public void updateResponse(String correlationId, String responseData, Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notBlank(responseData);

        Optional<IdempotentRequestControl> optIrc = getIdempotentRequestControl(correlationId);

        if (optIrc.isPresent() && StringUtils.isBlank(optIrc.get().getResponse())) {
            String sql = getQuery("UPDATE_IDEMPOTENT_REQUEST_CONTROL_RESPONSE");

            int rows = jdbcTemplate.update(
                    sql,
                    createParams("response", responseData,
                            "correlationId", correlationId,
                            "responseStatus", responseStatus));

            if (rows != 1) {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private Optional<IdempotentRequestControl> getIdempotentRequestControl(String correlationId) {
        String initialSql = getQuery("GET_IDEMPOTENT_REQUEST_CONTROL");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, IRC_ROW_MAPPER);

        String sql = builder.build();

        IdempotentRequestControl irc;

        try {
            irc = jdbcTemplate.queryForObject(sql, createParams("correlationId", correlationId), IRC_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            irc = null;
        }

        return Optional.ofNullable(irc);
    }

    private IdempotentRequestControl setIdempotentRequestControl(String correlationId) {
        String initialSql = getQuery("SET_IDEMPOTENT_REQUEST_CONTROL");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, IRC_ROW_MAPPER);

        String sql = builder.build();

        LocalDateTime now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

        jdbcTemplate.update(
                sql,
                createParams("correlationId", correlationId,
                        "createDatetime", DateTimeConverter.fromLocalDateTime(now)));

        return IdempotentRequestControl.builder()
                .correlationId(correlationId)
                .createDatetime(now)
                .requestStatus(IdempotentRequestControl.Status.NEW)
                .build();
    }
}
