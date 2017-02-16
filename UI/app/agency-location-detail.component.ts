import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router }   from '@angular/router';
import { Location }                 from '@angular/common';

import { AgencyLocation } from './agency-location';
import { AgencyLocationService } from './agency-location.service';
import { Inmate } from './inmate';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-agency-location-detail',
  template: `
  <div *ngIf="agencyLocation">
    <h2>{{agencyLocation.description}} Details</h2>
    <button (click)="goCount()">Physical Count</button>
    <div *ngIf="agencyLocation.currentOccupancy"><span><label>Current Occupancy:</label>{{agencyLocation.currentOccupancy}}</span></div>
    <div *ngIf="agencyLocation.operationalCapacity"><span><label>Operational Capacity:</label>{{agencyLocation.operationalCapacity}}</span></div>
    <h3>Inmates</h3>
    <div *ngIf="!agencyLocation.assignedInmates">Loading inmates...</div>
    <div *ngIf="agencyLocation.assignedInmates">
      <ul class="inmates">
    		<li *ngFor="let inmate of agencyLocation.assignedInmates" (click)="onSelect(inmate)">
    			{{inmate.firstName}} {{inmate.lastName}} ({{inmate.bookingId}})<span class="badge">&gt;</span>
    		</li>
    	</ul>
      <div *ngIf="agencyLocation.assignedInmates.length === 0">No inmates currently in the location.</div>
    <div>
  </div>
  <div *ngIf="!agencyLocation">Loading location...</div>
  <button (click)="goBack()">Back</button>
  `,
  styles: [`
    .selected {
    background-color: #CFD8DC !important;
    color: white;
    }
    .inmates {
    margin: 0 0 2em 0;
    list-style-type: none;
    padding: 0;
    width: auto;
    }
    .inmates li {
    cursor: pointer;
    position: relative;
    left: 0;
    background-color: #EEE;
    margin: .5em 0 .5em 0;
    padding: .3em 0;
    height: 1.6em;
    border-radius: 4px;
    }
    .inmates li.selected:hover {
    background-color: #BBD8DC !important;
    color: white;
    }
    .inmates li:hover {
    color: #607D8B;
    background-color: #DDD;
    left: .1em;
    }
    .inmates .text {
    position: relative;
    top: -3px;
    }
    .inmates .badge {
    display: inline-block;
    font-size: small;
    color: white;
    padding: 0.8em 0.7em 0 0.7em;
    background-color: #607D8B;
    line-height: 1em;
    position: relative;
    left: -1px;
    top: -4px;
    height: 1.8em;
    margin-left: .8em;
    border-radius: 0 4px 4px 0;
    float: right;
    }
  `],
})
export class AgencyLocationDetailComponent implements OnInit {
	@Input()
	agencyLocation: AgencyLocation;

    constructor(
      private agencyLocationService: AgencyLocationService,
      private route: ActivatedRoute,
      private router: Router,
      private location: Location
    ) { }

    ngOnInit(): void {
      this.route.params
        .switchMap((params: Params) => this.agencyLocationService.getAgencyLocation(+params['id']))
        .subscribe(agencyLocation => this.setAgencyLocation(agencyLocation));
    }

    setAgencyLocation(agyLoc: AgencyLocation): void {
      this.agencyLocation = agyLoc;

      if(!this.agencyLocation.assignedInmates) {
        this.agencyLocationService.getInmates(this.agencyLocation.locationId).then(inmates => this.agencyLocation.assignedInmates = inmates);
      }
    }

  onSelect(inmate: Inmate): void {
    this.router.navigate(['/inmates', inmate.inmateId]);
  }

  goCount(): void {
    this.router.navigate(['/count', this.agencyLocation.locationId])
  }

  goBack(): void {
    this.location.back();
  }

}
