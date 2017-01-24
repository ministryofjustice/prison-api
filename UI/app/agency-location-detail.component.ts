import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';

import { AgencyLocation } from './agency-location';
import { AgencyLocationService } from './agency-location.service';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-agency-location-detail',
  template: `
  <div *ngIf="agencyLocation">
  <h2>{{agencyLocation.description}} Details</h2>
  </div>
  <div *ngIf="!agencyLocation">Loading location...</div>
  <button (click)="goBack()">Back</button>
  `,
})
export class AgencyLocationDetailComponent implements OnInit {
	@Input()
	agencyLocation: AgencyLocation;

    constructor(
      private agencyLocationService: AgencyLocationService,
      private route: ActivatedRoute,
      private location: Location
    ) { }

    ngOnInit(): void {
      this.route.params
        .switchMap((params: Params) => this.agencyLocationService.getAgencyLocation(+params['locationId']))
        .subscribe(agencyLocation => this.agencyLocation = agencyLocation);
    }

    goBack(): void {
      this.location.back();
    }
}
