package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.IdempotentRequestRepositorySql;
import uk.gov.justice.hmpps.prison.repository.support.IdempotentRequestControl;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * IdempotentRequestControl repository implementation.
 */
@Repository
@Slf4j
public class IdempotentRequestRepository extends RepositoryBase {
    private static final StandardBeanPropertyRowMapper<IdempotentRequestControl> IRC_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(IdempotentRequestControl.class);


    public IdempotentRequestControl getAndSet(final String correlationId) {
        Validate.notBlank(correlationId);

        final IdempotentRequestControl irc;

        final var optIrc = getIdempotentRequestControl(correlationId);

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


    public void updateResponse(final String correlationId, final String responseData, final Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notBlank(responseData);

        final var optIrc = getIdempotentRequestControl(correlationId);

        if (optIrc.isPresent() && StringUtils.isBlank(optIrc.get().getResponse())) {
            final var sql = IdempotentRequestRepositorySql.UPDATE_IDEMPOTENT_REQUEST_CONTROL_RESPONSE.getSql();

            final var rows = jdbcTemplate.update(
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

    private Optional<IdempotentRequestControl> getIdempotentRequestControl(final String correlationId) {
        final var initialSql = IdempotentRequestRepositorySql.GET_IDEMPOTENT_REQUEST_CONTROL.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, IRC_ROW_MAPPER);

        final var sql = builder.build();

        IdempotentRequestControl irc;

        try {
            irc = jdbcTemplate.queryForObject(sql, createParams("correlationId", correlationId), IRC_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            irc = null;
        }

        return Optional.ofNullable(irc);
    }

    private IdempotentRequestControl setIdempotentRequestControl(final String correlationId) {
        final var initialSql = IdempotentRequestRepositorySql.SET_IDEMPOTENT_REQUEST_CONTROL.getSql();

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, IRC_ROW_MAPPER);

        final var sql = builder.build();

        final var now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

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
