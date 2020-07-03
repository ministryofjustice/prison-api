package uk.gov.justice.hmpps.prison.api.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

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

    public HttpHeaders getPaginationHeaders() {
        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(totalRecords));
        responseHeaders.set("Page-Offset",   String.valueOf(pageOffset));
        responseHeaders.set("Page-Limit",    String.valueOf(pageLimit));
        return responseHeaders;
    }

    public ResponseEntity<List<T>> getResponse() {
        return ResponseEntity.ok()
                .headers(getPaginationHeaders())
                .body(items);
    }

}
