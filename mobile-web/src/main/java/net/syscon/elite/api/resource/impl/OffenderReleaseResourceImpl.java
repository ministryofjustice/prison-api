package net.syscon.elite.api.resource.impl;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;

import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/offender-sentences")
public class OffenderReleaseResourceImpl implements OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;

    public OffenderReleaseResourceImpl(AuthenticationFacade authenticationFacade, BookingService bookingService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
    }

    @Override
    public GetOffenderSentencesResponse getOffenderSentences(String query, List<String> offenderNos, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<OffenderSentenceDetail> sentences = bookingService.getOffenderSentencesSummary(
                authenticationFacade.getCurrentUsername(),
                buildOffenderInQuery(query,offenderNos),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                StringUtils.defaultIfBlank(sortFields, "offenderNo"),
                sortOrder != null ? sortOrder : Order.ASC);

        return GetOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }

    private String buildOffenderInQuery(final String query, List<String> offenderNos) {
        StringBuilder newQuery = new StringBuilder(StringUtils.trimToEmpty(query));

        if (!offenderNos.isEmpty()) {
            if (newQuery.length() > 0) {
                newQuery.append(",and:");
            }
            final String ids = offenderNos.stream().map(offenderNo -> "'"+offenderNo+"'").collect(Collectors.joining("|"));
            newQuery.append("offenderNo:in:").append(ids);
        }
        return newQuery.toString();
    }
}
