import { Component, Input } from '@angular/core';
import { Location } from './location';

@Component({
  selector: 'my-location-detail',
  template: `
  <h2>{{location.description}} Details</h2>
  `,
})
export class LocationDetailComponent {
	@Input()
	location: Location;
}
