package net.syscon.elite.service.support;

import lombok.Builder;
import lombok.Getter;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;

@Getter
public class SearchOffenderRequest extends PageRequest {
    private String username;
    private String keywords;
    private String locationPrefix;

    @Builder(toBuilder = true)
    public SearchOffenderRequest(String orderBy, Order order, long offset, long limit, String username, String keywords, String locationPrefix) {
        super(orderBy, order, offset, limit);

        this.username = username;
        this.keywords = keywords;
        this.locationPrefix = locationPrefix;
    }
}
