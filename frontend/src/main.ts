import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Declaración de tipos para Google Maps
declare global {
  interface Window {
    google: any;
  }
}

// Inicializar la aplicación
bootstrapApplication(App, appConfig)
  .catch((err) => console.error('Error al inicializar la aplicación:', err));
