package net.syscon.elite.service;

import net.syscon.elite.exception.LocationCountAlreadyExists;
import net.syscon.elite.web.api.model.LocationCount;

public interface CountsService {
	void createLocationCount(String countId, String locationId, LocationCount entity) throws LocationCountAlreadyExists;
}
