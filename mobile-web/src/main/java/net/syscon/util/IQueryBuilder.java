package net.syscon.util;

import net.syscon.elite.api.support.Order;

/**
 * Created by andrewk on 13/06/2017.
 */
public interface IQueryBuilder {
    IQueryBuilder addOrderBy(boolean isAscending, String fields);
    IQueryBuilder addOrderBy(Order order, String fields);
    IQueryBuilder addPagination();
    IQueryBuilder addQuery(String query);
    IQueryBuilder addRowCount();
    IQueryBuilder removeSpecialChars();
    String build();
}
