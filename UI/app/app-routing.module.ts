import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AgencyLocationDetailComponent } from './agency-location-detail.component';
import { AgencyLocationsComponent } from './agency-locations.component';
import { InmatesComponent } from './inmates.component';
import { InmateDetailComponent } from './inmate-detail.component';
import { AgencyLocationCountComponent } from './agency-location-count.component';
import { AgenciesComponent } from './agencies.component';

const routes: Routes = [
  { path: '', redirectTo: '/agencies', pathMatch: 'full' },
  { path: 'agencies', component: AgenciesComponent },
  { path: 'locations', component: AgencyLocationsComponent },
  { path: 'inmates', component: InmatesComponent },
  { path: 'inmates/:id', component: InmateDetailComponent },
  { path: 'locations/:id', component: AgencyLocationDetailComponent },
  { path: 'count/:id', component: AgencyLocationCountComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}
