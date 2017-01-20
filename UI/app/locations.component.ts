import { Component } from '@angular/core';
import { OnInit } from '@angular/core';

import { Location } from './location';
import { LocationService } from './location.service';

@Component({
  selector: 'my-locations',
  template: `
	<h2>{{title}}</h2>
	<ul class="locations">
		<li *ngFor="let location of locations" (click)="onSelect(location)">
			{{location.description}} <span class="badge">&gt;</span>
		</li>
	</ul>
	<div *ngIf="selectedLocation">
	<my-location-detail [location]="selectedLocation"></my-location-detail>
	</div>
	`,
	styles: [`
	  .selected {
		background-color: #CFD8DC !important;
		color: white;
	  }
	  .locations {
		margin: 0 0 2em 0;
		list-style-type: none;
		padding: 0;
		width: 30em;
	  }
	  .locations li {
		cursor: pointer;
		position: relative;
		left: 0;
		background-color: #EEE;
		margin: .5em;
		padding: .3em 0;
		height: 1.6em;
		border-radius: 4px;
	  }
	  .locations li.selected:hover {
		background-color: #BBD8DC !important;
		color: white;
	  }
	  .locations li:hover {
		color: #607D8B;
		background-color: #DDD;
		left: .1em;
	  }
	  .locations .text {
		position: relative;
		top: -3px;
	  }
	  .locations .badge {
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
	providers: [LocationService]
})
export class LocationsComponent implements OnInit  {
	title = 'Select Location';
	locations: Location[];
	selectedLocation: Location;
	
	constructor(private locationService: LocationService) { }
		
	onSelect(location: Location): void {
		this.selectedLocation = location;
	}
	
	ngOnInit(): void {
	  this.getLocations();
	}
	
	getLocations(): void {
		this.locationService.getLocations().then(locations => this.locations = locations);
	}
}

export class LocationType {
	locationType: string;
	description: string;
}


const LOCATIONTYPES: LocationType[] = [
	{ locationType: 'CLAS', description: 'Classroom',  },
	{ locationType: 'EXER', description: 'Exercise Area',  },
	{ locationType: 'TIER', description: 'Tier',  },
	{ locationType: 'VISIT', description: 'Visitation Room',  },
	{ locationType: 'WORK', description: 'Work Area',  },
]
