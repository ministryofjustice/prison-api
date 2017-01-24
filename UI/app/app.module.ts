import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule }   from '@angular/router';

import { AppComponent }  from './app.component';

import { AgencyLocationDetailComponent } from './agency-location-detail.component';
import { AgencyLocationsComponent } from './agency-locations.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';
import { AgencyLocationService } from './agency-location.service';
import { InmateService } from './inmate.service';

@NgModule({
  imports:      [
	BrowserModule,
	RouterModule.forRoot([
	  {
		path: 'locations',
		component: AgencyLocationsComponent
	  },
	  {
		path: 'inmates',
		component: InmatesComponent
	  },
	  {
	  path: 'inmates/:inmateId',
		component: InmateDetailComponent
	  },
	  {
	  path: 'locations/:locationId',
		component: AgencyLocationDetailComponent
	  }
	])
  ],
  declarations: [
	AppComponent,
	AgencyLocationDetailComponent,
	AgencyLocationsComponent,
	InmatesComponent,
	InmateDetailComponent
  ],
  providers: [ AgencyLocationService, InmateService ],
  bootstrap:    [ AppComponent ]
})
export class AppModule { }
