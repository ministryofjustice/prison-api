package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBooking;
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
    public GetSearchResponse searchForOffendersLocationOnly(String locationPrefix, String sortFields, String sortOrder, Long offset, Long limit) {
        List<OffenderBooking> offenders = inmateService.findOffenders(null, locationPrefix, sortFields, sortOrder, offset, limit);
        return GetSearchResponse.respond200WithApplicationJson(offenders, offset, limit, MetaDataFactory.getTotalRecords(offenders));
    }

    @Override
    public GetSearchResponse searchForOffendersLocationAndKeyword(String locationPrefix, String keywords, String sortFields, String sortOrder, Long offset, Long limit) {
        List<OffenderBooking> offenders = inmateService.findOffenders(keywords, locationPrefix, sortFields, sortOrder, offset, limit);
        return GetSearchResponse.respond200WithApplicationJson(offenders, offset, limit, MetaDataFactory.getTotalRecords(offenders));
    }
}
