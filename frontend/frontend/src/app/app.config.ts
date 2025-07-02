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

const msalInstance = new PublicClientApplication(msalConfig);

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptorsFromDi()
    ),
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
    MsalService,
    MsalBroadcastService,
  ],
};
