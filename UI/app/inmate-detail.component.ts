import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';

import { Inmate } from './inmate';
import {InmateService } from './inmate.service';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-inmate-detail',
  template: `
  <div *ngIf="inmate">
    <h2>{{inmate.firstName}} {{inmate.lastName}} Details</h2>
  <div>
  <div *ngIf="!inmate">Loading inmate...</div>
  `,
})
export class InmateDetailComponent implements OnInit {
	@Input() inmate: Inmate;

  constructor(
    private inmateService: InmateService,
    private route: ActivatedRoute,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.route.params
      .switchMap((params: Params) => this.inmateService.getInmate(+params['inmateId']))
      .subscribe(inmate => this.inmate = inmate);
  }
}
