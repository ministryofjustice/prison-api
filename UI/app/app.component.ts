import { Component } from '@angular/core';

@Component({
  selector: 'my-app',
  template: `
    <h1>{{title}}</h1>
      <a routerLink="/agencies">Agencies</a>
      <!--<a routerLink="/locations">Locations</a>--><i>Locations</i>
    	<a routerLink="/inmates">Inmates</a>
  	<router-outlet></router-outlet>
  `,
  },
)
export class AppComponent {
  title = 'Officer Desktop';
}
