import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router }   from '@angular/router';
import { Location }                 from '@angular/common';

import { AgencyLocation } from './agency-location';
import { PhysicalInmateCount } from './agency-location-count';
import { AgencyLocationInmateCount } from './agency-location-count';

import { AgencyLocationService } from './agency-location.service';
import { AgencyLocationCountService } from './agency-location-count.service';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-agency-location-count',
  template: `
  <div *ngIf="agencyLocation">
    <h2>Physical Count</h2>
    <h3>{{agencyLocation.description}}</h3>
    <div *ngIf="!recordedCount">
      <label>Head count:</label><input #locationCount/>
      <button (click)="submitCount(locationCount.value)">Submit</button><button (click)="goBack()">Cancel</button>
    </div>
    <div *ngIf="recordedCount">
      <label>Count Status:</label>{{recordedCount.status}}
      <h4>Initial Count</h4>
      <table>
        <tr><td><label>Conducted By:</label></td><td>{{recordedCount.initialCount.conductUserId}}</td></tr>
        <tr><td><label>Reason:</label></td><td>{{recordedCount.initialCount.countReason}}</td></tr>
        <tr><td><label>Count:</label></td><td>{{recordedCount.initialCount.count}}</td></tr>
      </table>
      <button (click)="goBack()">Done</button>
    </div>
  </div>
  <div *ngIf="!agencyLocation">Loading location...
    <button (click)="goBack()">Back</button>
  </div>
  `
})
export class AgencyLocationCountComponent implements OnInit {
	@Input()
	agencyLocation: AgencyLocation;
  locationCount: Number;
  recordedCount: AgencyLocationInmateCount;

  constructor(
    private agencyLocationService: AgencyLocationService,
    private agencyLocationCountService: AgencyLocationCountService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.route.params
      .switchMap((params: Params) => this.agencyLocationService.getAgencyLocation(+params['id']))
      .subscribe(agencyLocation => this.agencyLocation = agencyLocation);
  }

  submitCount(count: number): void {
    const inmateCount: AgencyLocationInmateCount = {
      id: this.agencyLocation.id,
      locationId: this.agencyLocation.id,
      status: 'Completed',
      initialCount: {
        conductUserId: '(mobile User)',
        countReason: 'Adhoc',
        count: count,
      }
    };

    this.agencyLocationCountService.recordInitialCount(inmateCount)
      .then(inmateCount => this.recordedCount = inmateCount);
  }

  goBack(): void {
    this.location.back();
  }

}
