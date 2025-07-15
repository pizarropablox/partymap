import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NavigationService {
  goTo(url: string) {
    window.location.href = url;
  }
} 