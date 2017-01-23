import { Injectable } from '@angular/core';

import { Inmate } from './inmate';
import { INMATES } from './mock-inmates';

@Injectable()
export class InmateService {
  getInmates(): Promise<Inmate[]> {
	 return Promise.resolve(INMATES);
  }

  getInmate(inmateId: number): Promise<Inmate> {
  	return this.getInmates()
  		.then(inmates => inmates.find(inmate => inmate.inmateId === inmateId));
  }
}
