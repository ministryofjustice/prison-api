package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.InmateAlertRepositorySql;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@Slf4j
public class InmateAlertRepository extends RepositoryBase {

    public InmateAlertRepository() {}

    private final Map<String, FieldMapper> alertMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("ALERT_SEQ", new FieldMapper("alertId"))
            .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("ALERT_TYPE", new FieldMapper("alertType"))
            .put("ALERT_TYPE_DESC", new FieldMapper("alertTypeDescription"))
            .put("ALERT_CODE", new FieldMapper("alertCode"))
            .put("ALERT_CODE_DESC", new FieldMapper("alertCodeDescription"))
            .put("COMMENT_TEXT", new FieldMapper("comment", value -> value == null ? "" : value))
            .put("ALERT_STATUS", new FieldMapper("active", "ACTIVE"::equals))
            .put("ALERT_DATE", new FieldMapper("dateCreated", DateTimeConverter::toISO8601LocalDate))
            .put("EXPIRY_DATE", new FieldMapper("dateExpires", DateTimeConverter::toISO8601LocalDate))
            .put("MODIFY_DATETIME", new FieldMapper("modifiedDateTime", DateTimeConverter::toISO8601LocalDateTime))
            .put("ADD_FIRST_NAME", new FieldMapper("addedByFirstName"))
            .put("ADD_LAST_NAME", new FieldMapper("addedByLastName"))
            .put("UPDATE_FIRST_NAME", new FieldMapper("expiredByFirstName"))
            .put("UPDATE_LAST_NAME", new FieldMapper("expiredByLastName"))
            .build();

    public Page<Alert> getAlerts(final long bookingId, final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = InmateAlertRepositorySql.FIND_INMATE_ALERTS.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);

        final var sql = builder
                .addRowCount()
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        final var alertMapper = Row2BeanRowMapper.makeMapping(Alert.class, alertMapping);
        final var paRowMapper = new PageAwareRowMapper<>(alertMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit, "alertStatus", null),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }
}
