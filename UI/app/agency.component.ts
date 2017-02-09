import { Injectable, Component, Input, OnInit } from '@angular/core';
import { Headers, Http } from '@angular/http';
import { ActivatedRoute, Params, Router }   from '@angular/router';
import { Location } from '@angular/common';

import { Agency } from './agency';
import { AgencyService } from './agency.service';
import { AgencyLocation } from './agency-location';
import { AgencyLocationService } from './agency-location.service';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-agency',
  template: `
  <div *ngIf="agency">
  	<h2>{{agency.description}}</h2>
    <div *ngIf="agencyLocations">
    	<ul class="agencyLocations">
    		<li *ngFor="let agencyLocation of agencyLocations" (click)="onSelect(agencyLocation)">
    			{{agencyLocation.description}} <span class="badge">&gt;</span>
    		</li>
    	</ul>
      <div *ngIf="agencyLocations.length === 0">No locations for this agency.</div>
    </div>
    <div *ngIf="!agencyLocations">Loading internal locations...</div>
  </div>
  <div *ngIf="!agency">Loading agency details...</div>
	`,
	styles: [`
	  .selected {
		background-color: #CFD8DC !important;
		color: white;
	  }
	  .agencyLocations {
		margin: 0 0 2em 0;
		list-style-type: none;
		padding: 0;
		width: auto;
	  }
	  .agencyLocations li {
		cursor: pointer;
		position: relative;
		left: 0;
		background-color: #EEE;
    margin: .5em 0 .5em 0;
		padding: .3em 0;
		height: 1.6em;
		border-radius: 4px;
	  }
	  .agencyLocations li.selected:hover {
		background-color: #BBD8DC !important;
		color: white;
	  }
	  .agencyLocations li:hover {
		color: #607D8B;
		background-color: #DDD;
		left: .1em;
	  }
	  .agencyLocations .text {
		position: relative;
		top: -3px;
	  }
	  .agencyLocations .badge {
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
export class AgencyComponent implements OnInit  {
  @Input()
  agency: Agency;
	agencyLocations: AgencyLocation[];

	constructor(
    private agencyService: AgencyService,
    private agencyLocationService: AgencyLocationService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.route.params
      .switchMap((params: Params) => this.agencyService.getAgency(params['name']))
      .subscribe(agency => this.setAgency(agency));
  }

  setAgency(agency: Agency): void {
    this.agency = agency;
    this.agencyService.getAgencyLocations(agency.agencyId)
      .then(agencyLocations => this.agencyLocations = agencyLocations);
  }

	onSelect(agencyLocation: AgencyLocation): void {
    this.router.navigate(['/locations', agencyLocation.locationId]);
	}
}
