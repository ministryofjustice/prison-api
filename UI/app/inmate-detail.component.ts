import { Component, Input } from '@angular/core';
import { Inmate } from './inmate';

@Component({
  selector: 'my-inmate-detail',
  template: `
  <h2>{{inmate.firstName}} {{inmate.lastName}} Details</h2>
  `,
})
export class InmateDetailComponent {
	@Input()
	inmate: Inmate;
}
