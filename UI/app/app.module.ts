import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule }   from '@angular/router';
import { HttpModule, JsonpModule }    from '@angular/http';
import { FormsModule }   from '@angular/forms';

import { AppComponent }  from './app.component';

// Imports for loading & configuring the in-memory web api
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDBMockData }  from './in-memory-db-mock-data';

import { AgenciesComponent } from './agencies.component';
import { AgencyLocationDetailComponent } from './agency-location-detail.component';
import { AgencyComponent } from './agency.component';
import { AgencyLocationCountComponent } from './agency-location-count.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';

import { AgencyLocationService } from './agency-location.service';
import { InmateService } from './inmate.service';
import { AgencyLocationCountService } from './agency-location-count.service';
import { AgencyService } from './agency.service';

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
    AgenciesComponent,
  	AgencyLocationDetailComponent,
  	AgencyComponent,
    AgencyLocationCountComponent,
  	InmatesComponent,
  	InmateDetailComponent
  ],
  providers: [
    AgencyLocationService,
    InmateService,
    AgencyLocationCountService,
    AgencyService ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
