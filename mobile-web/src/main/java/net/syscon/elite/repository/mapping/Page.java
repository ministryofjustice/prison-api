package net.syscon.elite.repository.mapping;

import java.util.List;

public final class Page<T> {
    private final List<T> items;
    private final long recordCount;
    private final long offset;
    private final long limit;

    public Page(List<T> items, long recordCount, long offset, long limit) {
        this.items = items;
        this.recordCount = recordCount;
        this.offset = offset;
        this.limit = limit;
    }

    public List<T> getItems() {
        return items;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public long getOffset() {
        return offset;
    }

    public long getLimit() {
        return limit;
    }
}
