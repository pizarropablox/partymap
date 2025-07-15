// test.ts - Setup global para tests en Angular

import 'zone.js/testing';
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

// Mock global de los métodos de window.location para evitar recargas en tests
try {
  // Mock window.location.href to prevent direct assignments
  let mockHref = 'http://localhost:4200';
  Object.defineProperty(window.location, 'href', {
    get: () => mockHref,
    set: (value: string) => {
      mockHref = value;
      // Don't actually navigate, just update the mock value
    },
    configurable: true
  });

  Object.defineProperty(window.location, 'assign', {
    value: () => {},
    writable: true,
    configurable: true
  });
  Object.defineProperty(window.location, 'replace', {
    value: () => {},
    writable: true,
    configurable: true
  });
  Object.defineProperty(window.location, 'reload', {
    value: () => {},
    writable: true,
    configurable: true
  });
  Object.defineProperty(window.location, 'pathname', {
    get: () => '/',
    set: () => {},
    configurable: true
  });
  Object.defineProperty(window.location, 'origin', {
    get: () => 'http://localhost:4200',
    configurable: true
  });
  Object.defineProperty(window.location, 'search', {
    get: () => '',
    configurable: true
  });
  Object.defineProperty(window.location, 'hash', {
    get: () => '',
    set: () => {},
    configurable: true
  });

  // Mock window.open to prevent popup windows
  Object.defineProperty(window, 'open', {
    value: () => null,
    writable: true,
    configurable: true
  });
} catch (error) {
  // Ignore errors if properties are already defined
}

getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
); 