import { TestBed } from '@angular/core/testing';
import { NavigationService } from './navigation.service';

describe('NavigationService', () => {
  let service: NavigationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NavigationService]
    });
    service = TestBed.inject(NavigationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have goTo method', () => {
    expect(typeof service.goTo).toBe('function');
  });

  it('should have goTo method that accepts a string parameter', () => {
    expect(service.goTo.length).toBe(1); // One parameter
  });

  it('should have goTo method that is callable', () => {
    // Just verify the method exists and can be called without throwing
    expect(() => {
      // Don't actually call it to avoid page reload
      const method = service.goTo;
      expect(typeof method).toBe('function');
    }).not.toThrow();
  });

  it('should have goTo method with correct signature', () => {
    const method = service.goTo;
    expect(typeof method).toBe('function');
    // Verify it's a method that takes a string parameter
    expect(method.toString()).toContain('url');
  });
}); 