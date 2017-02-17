import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Params }   from '@angular/router';
import { Location }                 from '@angular/common';

import { Inmate } from './inmate';
import { InmateService } from './inmate.service';
import { ImageDetail } from './image-detail';
import { ImageSummary } from './image-summary';
import { ImageService } from './image.service';

import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'my-inmate-detail',
  template: `
  <div *ngIf="inmate">
    <h2>{{inmate.firstName}} {{inmate.lastName}} Details</h2>

    <div *ngIf="inmate.facialImageId"><img src="/api/images/{{inmate.facialImageId}}/data" alt="Inmate front facial image."/></div>
    <table>
    <tr><td><label>Fullname</label></td><td *ngIf="inmate.middleName">{{inmate.firstName}} {{inmate.middleName}} {{inmate.lastName}}</td><td *ngIf="!inmate.middleName">{{inmate.firstName}} {{inmate.lastName}}</td></tr>
    <tr *ngIf="inmate.bookingId"><td><label>Booking Id</label></td><td>{{inmate.bookingId}}</td></tr>
    <tr *ngIf="inmate.offenderId"><td><label>Offender Id</label></td><td>{{inmate.offenderId}}</td></tr>

    <tr *ngIf="inmate.dateOfBirth"><td><label>DoB</label></td><td>{{inmate.dateOfBirth}}</td></tr>
    <tr *ngIf="inmate.age"><td><label>Age</label></td><td>{{inmate.age}}</td></tr>
    <tr *ngIf="inmate.alertCodes"><td><label>Alerts</label></td><td><span *ngFor="let alert of inmate.alertCodes">{{alert}}</span></td></tr>
    <tr *ngIf="inmate.physicalAttributes">
      <td>Physical Attributes</td>
      <td><ul><li><label>Gender:</label>{{inmate.physicalAttributes.gender}}</li>
      <li><label>Ethnicity:</label>{{inmate.physicalAttributes.ethnicity}}</li>
      <li><span><label>Height:</label>{{inmate.physicalAttributes.heightInches}} inches ({{inmate.physicalAttributes.heightMeters}} m)</span></li>
      <li><span><label>Weight:</label>{{inmate.physicalAttributes.weightPounds}} lbs. ({{inmate.physicalAttributes.weightKg}} kg)</span></li>
      </ul>
      <td>
    </tr>
    <tr *ngIf="inmate.physicalCharacteristics">
      <td>Physical Characteristics</td>
      <td><ul>
        <li *ngFor="let pc of inmate.physicalCharacteristics"><span><label>{{pc.characteristic}}:</label>{{pc.detail}}</span></li>
      </ul></td>
    </tr>
    <tr *ngIf="inmate.physicalMarks">
      <td>Physical Marks</td>
      <td>
      <ul><li *ngFor="let pm of inmate.physicalMarks"><span>
      <label>{{pm.type}}</label> {{pm.side}}, {{pm.bodyPart}}, {{pm.orientation}}, {{pm.comment}}
      </span></li></ul>
      </td>
    </tr>
    </table>

  <div>
  <div *ngIf="!inmate">Loading inmate...</div>
  <button (click)="goBack()">Back</button>
  `
})
export class InmateDetailComponent implements OnInit {
	@Input() inmate: Inmate;

  constructor(
    private inmateService: InmateService,
    private imageService: ImageService,
    private route: ActivatedRoute,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.route.params
      .switchMap((params: Params) => this.inmateService.getInmate(+params['id']))
      .subscribe(inmate => this.inmate = inmate );
  }

  goBack(): void {
    this.location.back();
  }
}
