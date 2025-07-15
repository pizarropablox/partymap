import { msalConfig, protectedResources, loginRequest, protectedResourceMap } from './auth-config';
import { environment } from '../environments/environment';
import { LogLevel, BrowserCacheLocation } from '@azure/msal-browser';

describe('auth-config', () => {
  it('should have correct msalConfig structure', () => {
    expect(msalConfig.auth.clientId).toBe(environment.msalConfig.auth.clientId);
    expect(msalConfig.auth.authority).toBe(environment.msalConfig.auth.authority);
    expect(msalConfig.auth.redirectUri).toBe(environment.msalConfig.auth.redirectUri);
    expect(msalConfig.auth.postLogoutRedirectUri).toBe(environment.msalConfig.auth.postLogoutRedirectUri);
    expect(msalConfig.cache?.cacheLocation).toBe(BrowserCacheLocation.LocalStorage);
    expect(msalConfig.cache?.storeAuthStateInCookie).toBe(false);
    expect(msalConfig.system?.loggerOptions?.logLevel).toBe(LogLevel.Verbose);
    expect(msalConfig.system?.loggerOptions?.piiLoggingEnabled).toBe(false);
    expect(typeof msalConfig.system?.loggerOptions?.loggerCallback).toBe('function');
  });

  it('should have correct protectedResources structure', () => {
    expect(protectedResources.api.endpoint).toBe(environment.apiConfig.uri);
    expect(protectedResources.api.scopes).toEqual(environment.apiConfig.scopes);
  });

  it('should have correct loginRequest', () => {
    expect(loginRequest.scopes).toEqual(['openid', 'profile', 'email']);
  });

  it('should have protectedResourceMap with correct mapping', () => {
    expect(protectedResourceMap instanceof Map).toBeTrue();
    expect(protectedResourceMap.has(protectedResources.api.endpoint)).toBeTrue();
    expect(protectedResourceMap.get(protectedResources.api.endpoint)).toEqual(protectedResources.api.scopes);
  });
}); 