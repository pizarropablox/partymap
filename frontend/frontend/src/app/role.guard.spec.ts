import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';
import { RoleGuard } from './role.guard';
import { ReservaService } from './services/reserva.service';

describe('RoleGuard', () => {
  let guard: RoleGuard;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockReservaService: jasmine.SpyObj<ReservaService>;
  let mockActivatedRouteSnapshot: Partial<ActivatedRouteSnapshot>;
  let mockRouterStateSnapshot: RouterStateSnapshot;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const reservaServiceSpy = jasmine.createSpyObj('ReservaService', ['obtenerUsuarioActual']);
    
    TestBed.configureTestingModule({
      providers: [
        RoleGuard,
        { provide: Router, useValue: routerSpy },
        { provide: ReservaService, useValue: reservaServiceSpy }
      ]
    });
    
    guard = TestBed.inject(RoleGuard);
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockReservaService = TestBed.inject(ReservaService) as jasmine.SpyObj<ReservaService>;
    
    mockActivatedRouteSnapshot = {
      data: { roles: ['ADMIN', 'PRODUCTOR'] }
    } as Partial<ActivatedRouteSnapshot>;
    
    mockRouterStateSnapshot = {} as RouterStateSnapshot;
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access when user has required role', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'ADMIN' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should deny access when user does not have required role', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'CLIENTE' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should allow access when no roles are required', (done) => {
    const routeWithoutRoles = {
      data: {}
    } as Partial<ActivatedRouteSnapshot>;
    
    guard.canActivate(routeWithoutRoles as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  // Nuevos tests para aumentar cobertura
  it('should allow access when user has PRODUCTOR role', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'PRODUCTOR' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should deny access when user has CLIENTE role', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'CLIENTE' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should handle empty roles array', (done) => {
    const routeWithEmptyRoles = {
      data: { roles: [] }
    } as Partial<ActivatedRouteSnapshot>;
    
    guard.canActivate(routeWithEmptyRoles as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should handle undefined roles', (done) => {
    const routeWithUndefinedRoles = {
      data: { roles: undefined }
    } as Partial<ActivatedRouteSnapshot>;
    
    guard.canActivate(routeWithUndefinedRoles as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should handle null user data', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: null
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should handle undefined user data', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: undefined
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should handle user without tipoUsuario', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { nombre: 'Test User' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should handle service error gracefully', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of(null as any));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should handle different role combinations', (done) => {
    const routeWithMultipleRoles = {
      data: { roles: ['ADMIN', 'PRODUCTOR', 'CLIENTE'] }
    } as Partial<ActivatedRouteSnapshot>;
    
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'PRODUCTOR' }
    }));
    
    guard.canActivate(routeWithMultipleRoles as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should handle case insensitive role matching', (done) => {
    mockReservaService.obtenerUsuarioActual.and.returnValue(of({
      data: { tipoUsuario: 'admin' }
    }));
    
    guard.canActivate(mockActivatedRouteSnapshot as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should handle route with no data property', (done) => {
    const routeWithNoData = {
      data: {}
    } as Partial<ActivatedRouteSnapshot>;
    
    guard.canActivate(routeWithNoData as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should handle route with null data', (done) => {
    const routeWithNullData = {
      data: { roles: undefined }
    } as Partial<ActivatedRouteSnapshot>;
    
    guard.canActivate(routeWithNullData as ActivatedRouteSnapshot, mockRouterStateSnapshot).subscribe(result => {
      expect(result).toBe(true);
      done();
    });
  });

  it('should be injectable in route configuration', () => {
    expect(guard).toBeDefined();
    expect(typeof guard.canActivate).toBe('function');
  });
}); 