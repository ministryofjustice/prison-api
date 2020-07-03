package uk.gov.justice.hmpps.prison.util;

import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

public interface IQueryBuilder {
    IQueryBuilder addOrderBy(boolean isAscending, String fields);

    IQueryBuilder addOrderBy(Order order, String fields);

    IQueryBuilder addOrderBy(PageRequest pageRequest);

    IQueryBuilder addPagination();

    IQueryBuilder addQuery(String query);

    IQueryBuilder addWhereClause(String whereClause);

    IQueryBuilder addRowCount();

    IQueryBuilder addDirectRowCount();

    IQueryBuilder removeSpecialChars();

    DatabaseDialect getDialect();

    String build();
}
