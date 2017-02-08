import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Inmate } from './inmate';
import { InmateService } from './inmate.service';

@Component({
  moduleId: module.id,
  selector: 'my-inmates',
  template: `
	<h2>{{title}}</h2>
	<ul class="inmates">
		<li *ngFor="let inmate of inmates" (click)="onSelect(inmate)">
			{{inmate.firstName}} {{inmate.lastName}} ({{inmate.bookingId}})<span class="badge">&gt;</span>
		</li>
	</ul>
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
	providers: [InmateService]
})
export class InmatesComponent implements OnInit  {
	title = 'Select Inmate';
	inmates: Inmate[];
	selectedInmate: Inmate;

	constructor(
		private router: Router,
		private inmateService: InmateService )
	{ }

	onSelect(inmate: Inmate): void {
		this.router.navigate(['/inmates', inmate.inmateId]);
	}

	ngOnInit(): void {
	  this.getInmates();
	}

	getInmates(): void {
		this.inmateService.getInmates().then(inmates => this.inmates = inmates);
	}
}
