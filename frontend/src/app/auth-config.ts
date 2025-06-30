// src/app/auth-config.ts
import { LogLevel, Configuration, BrowserCacheLocation } from '@azure/msal-browser';
import { environment } from '../environments/environment';

export const msalConfig: Configuration = {
  auth: {
    clientId: environment.msalConfig.auth.clientId,
    authority: environment.msalConfig.auth.authority,
    redirectUri: environment.msalConfig.auth.redirectUri,
    postLogoutRedirectUri: environment.msalConfig.auth.postLogoutRedirectUri,
  },
  cache: {
    cacheLocation: BrowserCacheLocation.LocalStorage,
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        // No console.log, console.warn or console.error in this file
      },
      logLevel: LogLevel.Verbose,
      piiLoggingEnabled: false,
    },
  },
};

// Configuración para los scopes y recursos protegidos
export const protectedResources = {
  api: {
    endpoint: environment.apiConfig.uri,
    scopes: environment.apiConfig.scopes,
  },
};

// Configuración para las peticiones de autenticación
export const loginRequest = {
  scopes: ['openid', 'profile', 'email'],
};

export const protectedResourceMap = new Map<string, Array<string>>();
protectedResourceMap.set(protectedResources.api.endpoint, protectedResources.api.scopes);
