package net.syscon.util;

/**
 * Created by andrewk on 13/06/2017.
 */
public interface IQueryBuilder {
    IQueryBuilder addOrderBy(boolean isAscending, String fields);
    IQueryBuilder addPagination();
    IQueryBuilder addQuery(String query);
    IQueryBuilder addRowCount();
    IQueryBuilder removeSpecialChars();
    String build();
}
