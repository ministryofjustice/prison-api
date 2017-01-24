import { Injectable } from '@angular/core';

import { AgencyLocation } from './agency-location';
import { AGENCYLOCATIONS } from './mock-agency-locations';

@Injectable()
export class AgencyLocationService {
  getAgencyLocations(): Promise<AgencyLocation[]> {
    return Promise.resolve(AGENCYLOCATIONS);
  }

  getAgencyLocation(locationId: number): Promise<AgencyLocation> {
    return this.getAgencyLocations()
      .then(agencyLocations => agencyLocations.find(agencyLocation => agencyLocation.locationId === locationId));
  }
}
