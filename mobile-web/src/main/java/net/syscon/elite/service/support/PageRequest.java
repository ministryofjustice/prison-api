package net.syscon.elite.service.support;

import net.syscon.elite.api.support.Order;

public class PageRequest {
    private long offset;
    private long limit;
    private String orderBy;
    private Order order;

    public PageRequest(long offset, long limit, String orderBy, Order order) {
        super();
        this.offset = offset;
        this.limit = limit;
        this.orderBy = orderBy;
        this.order = order;
    }

    public long getOffset() {
        return offset;
    }

    public long getLimit() {
        return limit;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Order getOrder() {
        return order;
    }
}
