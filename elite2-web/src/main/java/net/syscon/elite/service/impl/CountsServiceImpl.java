package net.syscon.elite.service.impl;

import net.syscon.elite.service.CountsService;
import net.syscon.elite.service.exception.LocationCountAlreadyExists;
import net.syscon.elite.web.api.model.LocationCount;
import org.springframework.stereotype.Service;

@Service
public class CountsServiceImpl implements CountsService {


	@Override
	public void createLocationCount(String countId, String locationId, LocationCount entity) throws LocationCountAlreadyExists {

	}
}
