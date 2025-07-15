import { routes } from './app.routes';
import { HomeMapComponent } from './pages/home-map/home-map.component';
import { ContactComponent } from './pages/contact/contact';
import { AboutComponent } from './pages/about/about';
import { EventsComponent } from './pages/events/events.component';
import { ReportesComponent } from './pages/reportes/reportes.component';
import { ProductorComponent } from './pages/productor/productor.component';
import { ReservasComponent } from './pages/reservas/reservas.component';
import { AuthGuard } from './auth.guard';
import { RoleGuard } from './role.guard';

describe('app.routes', () => {
  it('should have routes array', () => {
    expect(Array.isArray(routes)).toBeTrue();
    expect(routes.length).toBeGreaterThan(0);
  });

  it('should have public routes without guards', () => {
    const publicRoutes = routes.filter(route => 
      route.path === '' || 
      route.path === 'home' || 
      route.path === 'about' || 
      route.path === 'contact'
    );

    expect(publicRoutes.length).toBe(4);
    
    // Verificar que las rutas públicas no tienen guards
    publicRoutes.forEach(route => {
      expect(route.canActivate).toBeUndefined();
      expect(route.data).toBeUndefined();
    });
  });

  it('should have protected routes with guards', () => {
    const protectedRoutes = routes.filter(route => 
      route.path === 'events' || 
      route.path === 'reservas' || 
      route.path === 'reportes' || 
      route.path === 'usuario'
    );

    expect(protectedRoutes.length).toBe(4);
    
    // Verificar que las rutas protegidas tienen guards
    protectedRoutes.forEach(route => {
      expect(route.canActivate).toBeDefined();
      expect(Array.isArray(route.canActivate)).toBeTrue();
      expect(route.canActivate).toContain(AuthGuard);
      expect(route.canActivate).toContain(RoleGuard);
    });
  });

  it('should have correct component mappings', () => {
    const routeMap = new Map(routes.map(route => [route.path, route.component]));
    
    expect(routeMap.get('')).toBe(HomeMapComponent);
    expect(routeMap.get('home')).toBe(HomeMapComponent);
    expect(routeMap.get('about')).toBe(AboutComponent);
    expect(routeMap.get('contact')).toBe(ContactComponent);
    expect(routeMap.get('events')).toBe(EventsComponent);
    expect(routeMap.get('reservas')).toBe(ReservasComponent);
    expect(routeMap.get('reportes')).toBe(ReportesComponent);
    expect(routeMap.get('usuario')).toBe(ProductorComponent);
  });

  it('should have correct role configurations for protected routes', () => {
    const eventsRoute = routes.find(route => route.path === 'events');
    const reservasRoute = routes.find(route => route.path === 'reservas');
    const reportesRoute = routes.find(route => route.path === 'reportes');
    const usuarioRoute = routes.find(route => route.path === 'usuario');

    function arraysContainSameElements(arr1: any[], arr2: any[]) {
      return arr1.length === arr2.length && arr1.every(val => arr2.includes(val));
    }

    expect(arraysContainSameElements(eventsRoute?.data?.['roles'] ?? [], ['administrador', 'productor'])).toBeTrue();
    expect(arraysContainSameElements(reservasRoute?.data?.['roles'] ?? [], ['cliente', 'administrador'])).toBeTrue();
    expect(arraysContainSameElements(reportesRoute?.data?.['roles'] ?? [], ['administrador'])).toBeTrue();
    expect(arraysContainSameElements(usuarioRoute?.data?.['roles'] ?? [], ['administrador'])).toBeTrue();
  });

  it('should have all required route properties', () => {
    routes.forEach(route => {
      expect(route.path).toBeDefined();
      
      // Skip redirect routes
      if (route.redirectTo) {
        return;
      }
      
      expect(route.component).toBeDefined();
      
      if (route.canActivate) {
        expect(Array.isArray(route.canActivate)).toBeTrue();
        expect(route.canActivate.length).toBeGreaterThan(0);
      }
      
      if (route.data?.['roles']) {
        expect(Array.isArray(route.data['roles'])).toBeTrue();
        expect(route.data['roles'].length).toBeGreaterThan(0);
      }
    });
  });

  it('should have unique paths', () => {
    const paths = routes.map(route => route.path);
    const uniquePaths = new Set(paths);
    expect(paths.length).toBe(uniquePaths.size);
  });

  it('should have wildcard route for redirect', () => {
    const wildcardRoute = routes.find(route => route.path === '**');
    expect(wildcardRoute).toBeDefined();
    expect(wildcardRoute?.redirectTo).toBe('/');
  });
}); 