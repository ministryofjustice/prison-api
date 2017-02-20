import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import { ImageDetail } from './image-detail';
import { ENVIRONMENT } from './environment';

import 'rxjs/add/operator/toPromise';

@Injectable()
export class ImageService {
  private inmatesUrl = ENVIRONMENT.apiUrl + 'api/images';  // URL to web api

  constructor(private http: Http) { }

  getImage(id: number): Promise<ImageDetail> {
    const url = `${this.inmatesUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as ImageDetail)
      .catch(this.handleError);
  }

  getImageData(id: number): Promise<string> {
    const url = `${this.inmatesUrl}/${id}/data`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as string)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
     console.error('An error occurred', error); // for demo purposes only
     return Promise.reject(error.message || error);
  }
}
