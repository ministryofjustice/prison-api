import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule }   from '@angular/router';
import { HttpModule, JsonpModule }    from '@angular/http';

import { AppComponent }  from './app.component';

// Imports for loading & configuring the in-memory web api
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InmateData }  from './inmate-data';

import { AgencyLocationDetailComponent } from './agency-location-detail.component';
import { AgencyLocationsComponent } from './agency-locations.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';
import { AgencyLocationService } from './agency-location.service';
import { InmateService } from './inmate.service';

@NgModule({
  imports:      [
	BrowserModule,
  HttpModule,
  JsonpModule,
  InMemoryWebApiModule.forRoot(InmateData),
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
	  path: 'inmates/:id',
		component: InmateDetailComponent
	  },
	  {
	  path: 'locations/:locationId',
		component: AgencyLocationDetailComponent
	  },
    {
    path: '',
    redirectTo: '/locations',
    pathMatch: 'full'
    }
	]),
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
