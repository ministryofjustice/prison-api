import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule }   from '@angular/router';
import { HttpModule, JsonpModule }    from '@angular/http';
import { FormsModule }   from '@angular/forms';

import { AppComponent }  from './app.component';

// Imports for loading & configuring the in-memory web api
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDBMockData }  from './in-memory-db-mock-data';

import { AgencyLocationDetailComponent } from './agency-location-detail.component';
import { AgencyLocationsComponent } from './agency-locations.component';
import { AgencyLocationCountComponent } from './agency-location-count.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';

import { AgencyLocationService } from './agency-location.service';
import { InmateService } from './inmate.service';
import { AgencyLocationCountService } from './agency-location-count.service';

import { AppRoutingModule }     from './app-routing.module';

@NgModule({
  imports:      [
  	BrowserModule,
    FormsModule,
    HttpModule,
    JsonpModule,
    InMemoryWebApiModule.forRoot( InMemoryDBMockData ),
    AppRoutingModule
  ],
  declarations: [
  	AppComponent,
  	AgencyLocationDetailComponent,
  	AgencyLocationsComponent,
    AgencyLocationCountComponent,
  	InmatesComponent,
  	InmateDetailComponent
  ],
  providers: [
    AgencyLocationService,
    InmateService,
    AgencyLocationCountService ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
