package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.service.CountsService;
import net.syscon.elite.service.exception.LocationCountAlreadyExists;
import net.syscon.elite.web.api.model.LocationCount;
import net.syscon.elite.web.api.resource.CountsResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class CountsResourceImpl implements CountsResource {


	private CountsService countsService;

	@Inject
	public void setCountsService(final CountsService countsService) { this.countsService = countsService; }

	@Override
	public GetCountsByCountIdResponse getCountsByCountId(String countId) throws Exception {
		return null;
	}

	@Override
	public GetCountsByCountIdLocationsByLocationIdResponse getCountsByCountIdLocationsByLocationId(String countId, String locationId) throws Exception {
		return null;
	}

	@Override
	public PostCountsByCountIdLocationsByLocationIdResponse postCountsByCountIdLocationsByLocationId(String countId, String locationId, LocationCount entity) throws Exception {
		return null;
	}

	@Override
	public PutCountsByCountIdLocationsByLocationIdResponse putCountsByCountIdLocationsByLocationId(String countId, String locationId, LocationCount entity) throws Exception {
		PutCountsByCountIdLocationsByLocationIdResponse result = null;
		try {
			countsService.createLocationCount(countId, locationId, entity);
			result = PutCountsByCountIdLocationsByLocationIdResponse.withJsonCreated(entity);
		} catch (LocationCountAlreadyExists ex) {
			result = PutCountsByCountIdLocationsByLocationIdResponse.withJsonConflict(entity);
		}
		return result;
	}

}
