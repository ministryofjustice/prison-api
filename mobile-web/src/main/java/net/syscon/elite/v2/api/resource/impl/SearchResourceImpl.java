package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBookingImpl;
import net.syscon.elite.v2.api.resource.SearchResource;
import net.syscon.util.MetaDataFactory;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("v2/search-offenders")
public class SearchResourceImpl implements SearchResource {

    private final InmateService inmateService;

    public SearchResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public GetSearchResponse searchForOffendersLocationOnly(String locationId, String sortFields, String sortOrder, Long offset, Long limit) {
        List<OffenderBookingImpl> offenders = inmateService.findOffenders(null, locationId, sortFields, sortOrder, offset, limit);
        return GetSearchResponse.respond200WithApplicationJson(offenders, offset, limit, MetaDataFactory.getTotalRecords(offenders));
    }

    @Override
    public GetSearchResponse searchForOffendersLocationAndKeyword(String locationId, String keywords, String sortFields, String sortOrder, Long offset, Long limit) {
        List<OffenderBookingImpl> offenders = inmateService.findOffenders(keywords, locationId, sortFields, sortOrder, offset, limit);
        return GetSearchResponse.respond200WithApplicationJson(offenders, offset, limit, MetaDataFactory.getTotalRecords(offenders));
    }
}
