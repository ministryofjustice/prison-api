package net.syscon.elite.api.support;

import java.util.Objects;

public class PageRequest {
    protected final String orderBy;
    protected final Order order;
    protected final Long offset;
    protected final Long limit;

    /**
     * Constructor.
     *
     * @param orderBy comma-seperated list of fields to order results by.
     * @param order   sort order (see {@link Order}). Defaults to {@link Order#ASC} if not specified.
     * @param offset  page offset. Defaults to 0 if not specified.
     * @param limit   page limit. Defaults to 10 if not specified.
     */
    public PageRequest(final String orderBy, final Order order, final Long offset, final Long limit) {
        this.orderBy = orderBy;
        this.order = (order == null) ? Order.ASC : order;
        this.offset = (offset == null) ? 0 : offset;
        this.limit = (limit == null) ? 10 : limit;
    }

    /**
     * Constructor. Constructs page request with specified sort fields and sort order, and an offset of 0 and limit of 10.
     *
     * @param orderBy comma-seperated list of fields to order results by.
     * @param order   sort order (see {@link Order}). Defaults to {@link Order#ASC} if not specified.
     */
    public PageRequest(final String orderBy, final Order order) {
        this(orderBy, order, null, null);
    }

    /**
     * Constructor. Constructs page request without sorting directives but with specified offset and limit.
     *
     * @param offset page offset. Defaults to 0 if not specified.
     * @param limit  page limit. Defaults to 10 if not specified.
     */
    public PageRequest(final Long offset, final Long limit) {
        this.orderBy = null;
        this.order = null;
        this.offset = (offset == null) ? 0 : offset;
        this.limit = (limit == null) ? 10 : limit;
    }

    /**
     * Constructor. Constructs page request with specified sort fields, ascending sort order, offset of 0 and limit of 10.
     *
     * @param orderBy comma-seperated list of fields to order results by.
     */
    public PageRequest(final String orderBy) {
        this(orderBy, null, null, null);
    }

    /**
     * Constructor. Constructs page request without sorting directives but with offset of 0 and limit of 10.
     */
    public PageRequest() {
        this.orderBy = null;
        this.order = null;
        this.offset = 0L;
        this.limit = 10L;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Order getOrder() {
        return order;
    }

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }

    public boolean isAscendingOrder() {
        return Order.ASC == order;
    }

    /**
     * Applies specified default order by fields (if not null) if this PageRequest does not have any order by fields
     * defined.
     *
     * @param defaultOrderBy default order by fields to be applied.
     * @return new {@code PageRequest} if default order by fields are applied, otherwise this unaltered {@code PageRequest}.
     */
    public PageRequest withDefaultOrderBy(final String defaultOrderBy) {
        var response = this;

        if ((defaultOrderBy != null) && (defaultOrderBy.length() > 0) && ((orderBy == null) || "".equals(orderBy.trim()))) {
            response = new PageRequest(defaultOrderBy, this.order, this.offset, this.limit);
        }

        return response;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PageRequest)) return false;
        final var that = (PageRequest) o;
        return Objects.equals(getOrderBy(), that.getOrderBy()) &&
                getOrder() == that.getOrder() &&
                Objects.equals(getOffset(), that.getOffset()) &&
                Objects.equals(getLimit(), that.getLimit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderBy(), getOrder(), getOffset(), getLimit());
    }
}
