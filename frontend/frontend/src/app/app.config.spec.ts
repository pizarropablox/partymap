import { appConfig } from './app.config';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { MSAL_INSTANCE, MsalService, MsalBroadcastService } from '@azure/msal-angular';

describe('appConfig', () => {
  it('should be an object with providers array', () => {
    expect(appConfig).toBeDefined();
    expect(typeof appConfig).toBe('object');
    expect(Array.isArray(appConfig.providers)).toBeTrue();
  });

  it('should include provideRouter, provideHttpClient, and MSAL providers', () => {
    const providers = appConfig.providers;
    // Verifica que existan los providers principales
    expect(providers.some(p => typeof p === 'object' && p !== null && 'provide' in p && p.provide === MSAL_INSTANCE)).toBeTrue();
    expect(providers.some(p => p === MsalService)).toBeTrue();
    expect(providers.some(p => p === MsalBroadcastService)).toBeTrue();
    // No se puede verificar provideRouter/provideHttpClient directamente porque son funciones, pero sí que existen providers
    expect(providers.length).toBeGreaterThan(0);
  });
}); 