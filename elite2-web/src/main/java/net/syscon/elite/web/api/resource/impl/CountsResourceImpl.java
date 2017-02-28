package net.syscon.elite.web.api.resource.impl;


import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.syscon.elite.exception.LocationCountAlreadyExists;
import net.syscon.elite.service.CountsService;
import net.syscon.elite.web.api.model.LocationCount;
import net.syscon.elite.web.api.resource.CountsResource;

@Component
public class CountsResourceImpl implements CountsResource {


	private CountsService countsService;

	@Inject
	public void setCountsService(final CountsService countsService) { this.countsService = countsService; }

	@Override
	public GetCountsByCountIdResponse getCountsByCountId(final String countId) throws Exception {
		return null;
	}

	@Override
	public GetCountsByCountIdLocationsByLocationIdResponse getCountsByCountIdLocationsByLocationId(final String countId, final String locationId) throws Exception {
		return null;
	}

	@Override
	public PostCountsByCountIdLocationsByLocationIdResponse postCountsByCountIdLocationsByLocationId(final String countId, final String locationId, final LocationCount entity) throws Exception {
		return null;
	}

	@Override
	@SuppressWarnings("squid:S1166")
	public PutCountsByCountIdLocationsByLocationIdResponse putCountsByCountIdLocationsByLocationId(final String countId, final String locationId, final LocationCount entity) throws Exception {
		PutCountsByCountIdLocationsByLocationIdResponse result = null;
		try {
			countsService.createLocationCount(countId, locationId, entity);
			result = PutCountsByCountIdLocationsByLocationIdResponse.withJsonCreated(entity);
		} catch (final LocationCountAlreadyExists ex) {
			result = PutCountsByCountIdLocationsByLocationIdResponse.withJsonConflict(entity);
		}
		return result;
	}

}
