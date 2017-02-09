import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Agency } from './agency';
import { AgencyLocation } from './agency-location';

@Injectable()
export class AgencyService {
//  private agenciesUrl = 'api/agencies';  // URL to web api
// TEMP - Access external API server
  private agenciesUrl = 'http://10.10.117.47:8080/api/agencies';  // Wellington's dev box

  constructor(private http: Http) { }

  getAgencies(): Promise<Agency[]> {
    return this.http.get(this.agenciesUrl)
              .toPromise()
              .then(response => response.json() as Agency[])
              .catch(this.handleError);
  }

  getAgency(id: string): Promise<Agency> {
    const url = `${this.agenciesUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as Agency)
      .catch(this.handleError);
  }

  getAgencyLocations(id: string): Promise<AgencyLocation[]> {
    const url = `${this.agenciesUrl}/${id}/locations`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as AgencyLocation[])
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
     console.error('An error occurred', error); // for demo purposes only
     return Promise.reject(error.message || error);
  }
}
