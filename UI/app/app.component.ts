import { Component } from '@angular/core';

@Component({
  selector: 'my-app',
  template: `
	<h1>{{title}}</h1>
	<h2>Select Location</h2>
	<ul class="locations">
		<li *ngFor="let location of locations" (click)="onSelect(location)">
			{{location.description}} <span class="badge">&gt;</span>
		</li>
	</ul>
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
	`]
})
export class AppComponent  {
	title = 'Location Info';
	locations = LOCATIONS;
	locationTypes = LOCATIONTYPES;
	
	selectedLocation: Location;
	
	onSelect(location: Location): void {
		this.selectedLocation = location;
	}
}

export class LocationType {
	locationType: string;
	description: string;
}

export class Location  {
	locationId: number;
	agencyId: string;
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

const LOCATIONS: Location[] = [
	{ locationId: 8210, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL',  },
	{ locationId: 8211, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-LIBRARY',  },
	{ locationId: 8212, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-CLASSRM',  },
	{ locationId: 8294, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CRAFT RM',  },
	{ locationId: 8295, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-BATHROOM',  },
	{ locationId: 8296, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CONTROL RM',  },
	{ locationId: 8290, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-HALLWAY',  },
	{ locationId: 8291, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-WEIGHT RM',  },
	{ locationId: 8292, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-MUSIC',  },
	{ locationId: 8293, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-GYM',  },
	{ locationId: 8724, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-PEN-DEATH ROW',  },
	{ locationId: 7018, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 1',  },
	{ locationId: 7032, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 2',  },
	{ locationId: 7110, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER EAST',  },
	{ locationId: 7120, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW WEST',  },
	{ locationId: 7130, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER WEST',  },
	{ locationId: 6341, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 1',  },
	{ locationId: 11734, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-TMPA-TMPA',  },
	{ locationId: 7061, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 3',  },
	{ locationId: 6651, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 3',  },
	{ locationId: 6662, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 4',  },
	{ locationId: 7080, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 4',  },
	{ locationId: 7100, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW EAST',  },
	{ locationId: 6999, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 4',  },
	{ locationId: 6362, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 2',  },
	{ locationId: 6383, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 3',  },
	{ locationId: 6624, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 2',  },
	{ locationId: 6600, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 5',  },
	{ locationId: 6673, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 5',  },
	{ locationId: 6684, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 1',  },
	{ locationId: 6689, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 2',  },
	{ locationId: 6421, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 4',  },
	{ locationId: 6442, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 5',  },
	{ locationId: 6484, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 1',  },
	{ locationId: 6505, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 2',  },
	{ locationId: 6526, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 3',  },
	{ locationId: 6547, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 4',  },
	{ locationId: 6700, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 3',  },
	{ locationId: 6711, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 4',  },
	{ locationId: 6722, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 5',  },
	{ locationId: 6482, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 1',  },
	{ locationId: 6947, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 1',  },
	{ locationId: 6961, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 2',  },
	{ locationId: 6980, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 3',  },
	{ locationId: 10148, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM_HOLIDAY',  },
	{ locationId: 10151, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_HOLIDAY',  },
	{ locationId: 11562, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CONV1',  },
	{ locationId: 8381, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_VISIT',  },
	{ locationId: 8207, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM',  },
	{ locationId: 8203, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-KITCHEN',  },
	{ locationId: 8209, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LAUNDRY',  },
	{ locationId: 8281, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LICENSE_PLATES',  },
	{ locationId: 8282, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-BRAILLE',  },
	{ locationId: 8283, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-CARPENTRY',  },
	{ locationId: 8284, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-PRINT_SHOP',  },
	{ locationId: 8285, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-DOT_SIGN_SHOP',  },
	{ locationId: 8286, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MACHINE_SHOP',  },
	{ locationId: 8287, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-WHEELCHAIR',  },
	{ locationId: 8288, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MCI',  },
];