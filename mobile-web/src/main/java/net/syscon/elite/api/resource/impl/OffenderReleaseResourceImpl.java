package net.syscon.elite.api.resource.impl;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.syscon.elite.api.model.OffenderRelease;
import net.syscon.elite.api.resource.OffenderReleaseResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/offender-releases")
public class OffenderReleaseResourceImpl implements OffenderReleaseResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;

    public OffenderReleaseResourceImpl(AuthenticationFacade authenticationFacade, BookingService bookingService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0")
    public GetOffenderReleasesResponse getOffenderReleases( List<String> offenderNos, String toDate, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<OffenderRelease> releaseResponse = bookingService.getOffenderReleaseSummary(
                fromISO8601DateString(toDate),
                authenticationFacade.getCurrentUsername(),
                buildOffenderInQuery(offenderNos),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                StringUtils.defaultIfBlank(sortFields, "offenderNo"),
                sortOrder != null ? sortOrder : Order.ASC, false);

        return GetOffenderReleasesResponse.respond200WithApplicationJson(releaseResponse);
    }

    private String buildOffenderInQuery(List<String> offenderNos) {
        String query = null;
        if (!offenderNos.isEmpty()) {
            final String ids = offenderNos.stream().map(offenderNo -> "'"+offenderNo+"'").collect(Collectors.joining("|"));
            query = "offenderNo:in:" + ids + "";
        }
        return query;
    }
}
