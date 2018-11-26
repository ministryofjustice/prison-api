package net.syscon.elite.api.support;

import java.util.List;

public class Page<T> {
    private final List<T> items;
    private final long totalRecords;
    private final long pageOffset;
    private final long pageLimit;

    public Page(List<T> items, long totalRecords, long pageOffset, long pageLimit) {
        this.items = items;
        this.totalRecords = totalRecords;
        this.pageOffset = pageOffset;
        this.pageLimit = pageLimit;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getPageOffset() {
        return pageOffset;
    }

    public long getPageLimit() {
        return pageLimit;
    }
}
