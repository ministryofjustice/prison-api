import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Agency } from './agency';
import { AgencyService } from './agency.service';

@Component({
  selector: 'my-agencies',
  template: `
	<h2>{{title}}</h2>
  <div *ngIf="agencies">
	<ul class="agencies">
		<li *ngFor="let agency of agencies" (click)="onSelect(agency)">
			{{agency.description}} ({{agency.agencyId}}) <span class="badge">&gt;</span>
		</li>
	</ul>
  </div>
  <div *ngIf="!agencies">Loading agencies...</div>
	`,
  styles: [`
	  .selected {
		background-color: #CFD8DC !important;
		color: white;
	  }
	  .agencies {
		margin: 0 0 2em 0;
		list-style-type: none;
		padding: 0;
		width: auto;
	  }
	  .agencies li {
		cursor: pointer;
		position: relative;
		left: 0;
		background-color: #EEE;
    margin: .5em 0 .5em 0;
		padding: .3em 0;
		height: 1.6em;
		border-radius: 4px;
	  }
	  .agencies li.selected:hover {
		background-color: #BBD8DC !important;
		color: white;
	  }
	  .agencies li:hover {
		color: #607D8B;
		background-color: #DDD;
		left: .1em;
	  }
	  .agencies .text {
		position: relative;
		top: -3px;
	  }
	  .agencies .badge {
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
		/*margin-left: .8em;*/
		border-radius: 0 4px 4px 0;
		float: right;
	  }
	`],
	providers: [AgencyService]
})
export class AgenciesComponent implements OnInit  {
	title = 'Agencies';
	agencies: Agency[];
	selectedAgency: Agency;

	constructor(
    private router: Router,
    private agencyService: AgencyService
  ) { }

	onSelect(agency: Agency): void {
    this.router.navigate(['/agencies', agency.agencyId]);
	}

	ngOnInit(): void {
	  this.getAgencies();
	}

	getAgencies(): void {
		this.agencyService.getAgencies().then(agencies => this.agencies = agencies);
	}
}
