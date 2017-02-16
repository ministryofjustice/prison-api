import { Component } from '@angular/core';

@Component({
  selector: 'my-app',
  template: `
    <h1>{{title}}</h1>
    <nav>
      <a routerLink="/agencies">Agencies</a>
      <!--<a routerLink="/locations">Locations</a>-->
    	<a routerLink="/inmates">Inmates</a>
    </nav>
  	<router-outlet></router-outlet>
  `,
  // styleUrls: [ './app/app.component.css' ]
  },
)
export class AppComponent {
  title = 'Officer Desktop';
}
