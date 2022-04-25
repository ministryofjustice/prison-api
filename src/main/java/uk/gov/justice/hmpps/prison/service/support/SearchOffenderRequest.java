package uk.gov.justice.hmpps.prison.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class SearchOffenderRequest extends PageRequest {
    private String username;
    private String keywords;
    private boolean returnAlerts;
    private boolean returnCategory;
    private String convictedStatus;
    private String locationPrefix;
    private List<String> alerts;
    private LocalDate fromDob;
    private LocalDate toDob;

    @Builder(toBuilder = true)
    public SearchOffenderRequest(final String orderBy, final Order order, final long offset, final long limit, final String username, final String keywords,
                                 final String locationPrefix, final List<String> alerts, final boolean returnAlerts, final boolean returnCategory, final String convictedStatus,
                                 final LocalDate fromDob, final LocalDate toDob) {
        super(orderBy, order, offset, limit);

        this.username = username;
        this.keywords = keywords;
        this.locationPrefix = locationPrefix;
        this.alerts = alerts;
        this.returnAlerts = returnAlerts;
        this.returnCategory = returnCategory;
        this.convictedStatus = convictedStatus;
        this.fromDob = fromDob;
        this.toDob = toDob;
    }
}
