import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AgencyLocation } from './agency-location';
import { AgencyLocationService } from './agency-location.service';

@Component({
  selector: 'my-agency-locations',
  template: `
	<h2>{{title}}</h2>
	<ul class="agencyLocations">
		<li *ngFor="let agencyLocation of agencyLocations" (click)="onSelect(agencyLocation)">
			{{agencyLocation.description}} <span class="badge">&gt;</span>
		</li>
	</ul>
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
		margin: .5em;
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
	providers: [AgencyLocationService]
})
export class AgencyLocationsComponent implements OnInit  {
	title = 'Internal Locations';
	agencyLocations: AgencyLocation[];
	selectedAgencyLocation: AgencyLocation;

	constructor(
    private router: Router,
    private agencyLocationService: AgencyLocationService
  ) { }

	onSelect(agencyLocation: AgencyLocation): void {
///		this.selectedAgencyLocation = agencyLocation;
    this.router.navigate(['/locations', agencyLocation.id]);
	}

	ngOnInit(): void {
	  this.getAgencyLocations();
	}

	getAgencyLocations(): void {
		this.agencyLocationService.getAgencyLocations().then(agencyLocations => this.agencyLocations = agencyLocations);
	}
}
