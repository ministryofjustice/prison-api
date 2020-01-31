package net.syscon.elite.api.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;


@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Page<T> {
    private final List<T> items;
    private final long totalRecords;
    private final long pageOffset;
    private final long pageLimit;

    public Page(final List<T> items, final long totalRecords, final PageRequest pageRequest) {
        this(items, totalRecords, pageRequest.getOffset(), pageRequest.getLimit());
    }
}
