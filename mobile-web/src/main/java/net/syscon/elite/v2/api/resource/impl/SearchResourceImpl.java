package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.resource.SearchOffenderResource;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("v2/search-offenders")
public class SearchResourceImpl implements SearchOffenderResource {

    private final InmateService inmateService;

    @Autowired
    public SearchResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public SearchForOffendersLocationOnlyResponse searchForOffendersLocationOnly(String locationPrefix, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        List<OffenderBooking> offenders = inmateService.findOffenders(null, locationPrefix, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return SearchForOffendersLocationOnlyResponse.respond200WithApplicationJson(offenders, MetaDataFactory.getTotalRecords(offenders), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public SearchForOffendersLocationAndKeywordResponse searchForOffendersLocationAndKeyword(String locationPrefix, String keywords, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        List<OffenderBooking> offenders = inmateService.findOffenders(keywords, locationPrefix, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return SearchForOffendersLocationAndKeywordResponse.respond200WithApplicationJson(offenders, MetaDataFactory.getTotalRecords(offenders), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }
}
