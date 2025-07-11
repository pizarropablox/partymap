import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { routes } from './app.routes';

import { 
  MSAL_INSTANCE, 
  MsalModule, 
  MsalService, 
  MsalBroadcastService
} from '@azure/msal-angular';
import { PublicClientApplication, InteractionType } from '@azure/msal-browser';
import { msalConfig } from './auth-config';

// Crear instancia de MSAL de forma segura
let msalInstance: PublicClientApplication;

try {
  // Verificar que estamos en un entorno de navegador
  if (typeof window !== 'undefined' && window.crypto) {
    msalInstance = new PublicClientApplication(msalConfig);
  } else {
    // Fallback para entornos sin crypto
    msalInstance = null as any;
  }
} catch (error) {
  console.warn('MSAL initialization failed:', error);
  msalInstance = null as any;
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptorsFromDi()
    ),
    // Proporcionar MsalService de forma condicional
    {
      provide: MsalService,
      useFactory: () => {
        if (msalInstance) {
          return new MsalService(msalInstance, null as any, null as any);
        }
        return null;
      }
    },
    // Solo incluir MSAL si la instancia se creó correctamente
    ...(msalInstance ? [
      importProvidersFrom(
        MsalModule.forRoot(
          msalInstance,
          { interactionType: InteractionType.Redirect },
          { 
            interactionType: InteractionType.Redirect, 
            protectedResourceMap: new Map() // Deshabilitar interceptación automática
          }
        )
      ),
      {
        provide: MSAL_INSTANCE,
        useValue: msalInstance,
      },
      MsalBroadcastService,
    ] : []),
  ],
};
