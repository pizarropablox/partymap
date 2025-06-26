import { Routes } from '@angular/router';
import { HomeMapComponent } from './pages/home-map/home-map.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeMapComponent },
];
