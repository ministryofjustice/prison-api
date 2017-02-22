import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Inmate } from './inmate';
import { ENVIRONMENT } from './environment';
import { ImageSummary } from './image-summary';

@Injectable()
export class InmateService {
  private inmatesUrl = ENVIRONMENT.apiUrl + 'api/booking';  // URL to web api

  constructor(private http: Http) { }

  getInmates(): Promise<Inmate[]> {
    return this.http.get(this.inmatesUrl)
              .toPromise()
              .then(response => response.json() as Inmate[])
              .catch(this.handleError);
  }

  getInmate(id: number): Promise<Inmate> {
    const url = `${this.inmatesUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as Inmate)
      .catch(this.handleError);
  }

  getInmateImages(id: number): Promise<ImageSummary[]> {
    const url = `${this.inmatesUrl}/${id}/images`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as ImageSummary)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
     console.error('An error occurred', error); // for demo purposes only
     return Promise.reject(error.message || error);
  }
}
