package net.syscon.util;

/**
 * Created by andrewk on 13/06/2017.
 */
public interface IQueryBuilder {
    String SQL_PLACEHOLDER_AND_QUERY = ":andQuery";
    String SQL_PLACEHOLDER_OR_QUERY = ":orQuery";
    String SQL_PLACEHOLDER_WHERE_QUERY = ":whereQuery";
    String SQL_PLACEHOLDER_ORDER_BY = ":orderBy";

    IQueryBuilder addOrderBy(boolean isAscending, String... fields);
    IQueryBuilder addPagination();
    IQueryBuilder addQuery(String query);
    IQueryBuilder addRowCount();

    String build();
}
