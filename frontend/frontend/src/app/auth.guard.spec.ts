import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { AuthGuard } from './auth.guard';
import { NavigationService } from './services/navigation.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockMsalService: jasmine.SpyObj<MsalService>;
  let mockNavigationService: jasmine.SpyObj<NavigationService>;

  beforeEach(() => {
    // Mock window.location methods to prevent page reloads during tests
    try {
      spyOn(window.location, 'assign').and.callFake(() => {});
      spyOn(window.location, 'replace').and.callFake(() => {});
      spyOn(window.location, 'reload').and.callFake(() => {});
    } catch (error) {
      // Ignore errors if methods are already mocked
    }

    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const msalServiceSpy = jasmine.createSpyObj('MsalService', ['loginRedirect', 'instance']);
    const navigationSpy = jasmine.createSpyObj('NavigationService', ['goTo']);
    
    msalServiceSpy.instance = jasmine.createSpyObj('instance', ['clearCache']);
    
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: Router, useValue: routerSpy },
        { provide: MsalService, useValue: msalServiceSpy },
        { provide: NavigationService, useValue: navigationSpy }
      ]
    });
    
    guard = TestBed.inject(AuthGuard);
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockMsalService = TestBed.inject(MsalService) as jasmine.SpyObj<MsalService>;
    mockNavigationService = TestBed.inject(NavigationService) as jasmine.SpyObj<NavigationService>;
  });

  afterEach(() => {
    // Restaurar todos los spies después de cada test
    jasmine.getEnv().allowRespy(true);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access when token exists', () => {
    spyOn(localStorage, 'getItem').and.returnValue('fake-token');
    const result = guard.canActivate();
    expect(result).toBe(true);
  });

  it('should deny access when no token exists', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);
    const result = guard.canActivate();
    expect(result).toBe(false);
  });

  it('should call MSAL loginRedirect when no token exists', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);
    mockMsalService.instance.clearCache = jasmine.createSpy('clearCache');
    mockMsalService.loginRedirect = jasmine.createSpy('loginRedirect');
    
    guard.canActivate();
    
    expect(mockMsalService.instance.clearCache).toHaveBeenCalled();
    expect(mockMsalService.loginRedirect).toHaveBeenCalledWith({
      scopes: ['openid', 'profile', 'email']
    });
  });

  it('should use NavigationService as fallback when MSAL fails', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);
    mockMsalService.instance.clearCache = jasmine.createSpy('clearCache').and.throwError('MSAL error');
    mockMsalService.loginRedirect = jasmine.createSpy('loginRedirect').and.throwError('MSAL error');
    
    guard.canActivate();
    
    expect(mockNavigationService.goTo).toHaveBeenCalledWith(jasmine.any(String));
  });

  it('should handle userInfo parsing error gracefully', () => {
    spyOn(localStorage, 'getItem').and.callFake((key: string) => {
      if (key === 'userInfo') return 'invalid-json';
      return 'fake-token';
    });
    
    const result = guard.canActivate();
    expect(result).toBe(true);
  });
}); 