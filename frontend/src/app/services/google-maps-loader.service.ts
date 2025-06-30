// src/app/services/google-maps-loader.service.ts

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GoogleMapsLoaderService {
  private googleMapsLoaded = false;
  private loadPromise: Promise<void> | null = null;

  constructor() {}

  load(): Promise<void> {
    if (this.googleMapsLoaded) {
      return Promise.resolve();
    }

    if (this.loadPromise) {
      return this.loadPromise;
    }

    this.loadPromise = new Promise<void>((resolve, reject) => {
      if (typeof google !== 'undefined' && google.maps) {
        this.googleMapsLoaded = true;
        resolve();
        return;
      }

      const script = document.createElement('script');
      script.type = 'text/javascript';
      script.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyCRQWonRBlilzJNvnyyFKaXmgn54yCL5EY&libraries=places,marker&language=es&region=CL';
      script.async = true;
      script.defer = true;

      script.onload = () => {
        this.googleMapsLoaded = true;
        resolve();
      };

      script.onerror = () => {
        reject(new Error('Failed to load Google Maps API'));
      };

      document.head.appendChild(script);
    });

    return this.loadPromise;
  }

  isLoaded(): boolean {
    return this.googleMapsLoaded && typeof google !== 'undefined' && !!google.maps;
  }
}
