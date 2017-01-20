import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule }   from '@angular/router';

import { AppComponent }  from './app.component';

import { LocationDetailComponent } from './location-detail.component';
import { LocationsComponent } from './locations.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';

@NgModule({
  imports:      [ 
	BrowserModule,
	RouterModule.forRoot([
	  {
		path: 'locations',
		component: LocationsComponent
	  },
	  {
		path: 'inmates',
		component: InmatesComponent
	  }
	])
  ],
  declarations: [ 
	AppComponent,
	LocationDetailComponent,
	LocationsComponent,
	InmatesComponent,
	InmateDetailComponent
  ],
  bootstrap:    [ AppComponent ]
})
export class AppModule { }
