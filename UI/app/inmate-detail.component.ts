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
    </table>
    <div *ngIf="inmate.physicalAttributes">
      <h3>Physical Attributes</h3>
      <table>
        <tr>
          <td><label>Gender:</label></td>
          <td>{{inmate.physicalAttributes.gender}}</td>
        </tr>
        <tr>
          <td><label>Ethnicity:</label></td>
          <td>{{inmate.physicalAttributes.ethnicity}}</td>
        </tr>
        <tr>
          <td><label>Height:</label></td>
          <td>{{inmate.physicalAttributes.heightInches}} inches ({{inmate.physicalAttributes.heightMeters}} m)</td>
        </tr>
        <tr>
          <td><label>Weight:</label></td>
          <td>{{inmate.physicalAttributes.weightPounds}} lbs. ({{inmate.physicalAttributes.weightKg}} kg)</td>
        </tr>
      </table>
    </div>
    <div *ngIf="inmate.physicalCharacteristics">
      <h3>Physical Characteristics</h3>
      <table>
        <tr *ngFor="let pc of inmate.physicalCharacteristics">
          <td><label>{{pc.characteristic}}:</label></td>
          <td>{{pc.detail}}</td>
          <td><img *ngIf="pc.imageId" src="/api/images/{{pc.imageId}}/data"/></td>
        </tr>
      </table>
    </div>
    <div *ngIf="inmate.physicalMarks">
      <h3>Physical Marks</h3>
      <table>
        <tr *ngFor="let pm of inmate.physicalMarks">
          <td><label>{{pm.type}}</label></td>
          <td>{{pm.side}}</td>
          <td>{{pm.bodyPart}}</td>
          <td>{{pm.orientation}}</td>
          <td>{{pm.comment}}</td>
          <td><img *ngIf="pm.imageId" src="/api/images/{{pm.imageId}}/data"/></td>
        </tr>
      </table>
    </div>
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
