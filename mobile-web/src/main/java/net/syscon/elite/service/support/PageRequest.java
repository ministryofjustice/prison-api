package net.syscon.elite.service.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.syscon.elite.api.support.Order;

@Getter
@AllArgsConstructor
public class PageRequest {
    protected final String orderBy;
    protected final Order order;
    protected final long offset;
    protected final long limit;
}
