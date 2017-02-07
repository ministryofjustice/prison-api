import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { PhysicalInmateCount } from './agency-location-count';
import { AgencyLocationInmateCount } from './agency-location-count';

@Injectable()
export class AgencyLocationCountService {

  private headers = new Headers({'Content-Type': 'application/json'});
  private locationsUrl = 'api/counts';  // URL to web api

  constructor(private http: Http) { }

  getActiveCount(id: number): Promise<AgencyLocationInmateCount> {
    const url = `${this.locationsUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json().data as AgencyLocationInmateCount)
      .catch(this.handleError);
  }

  recordInitialCount(inmateCount: AgencyLocationInmateCount): Promise<AgencyLocationInmateCount> {
    const url = `${this.locationsUrl}/${inmateCount.locationId}`;
    return this.http
      .post(url, JSON.stringify(inmateCount), {headers: this.headers})
      .toPromise()
      .then(res => res.json().data)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
     console.error('An error occurred', error); // for demo purposes only
     return Promise.reject(error.message || error);
  }
}
