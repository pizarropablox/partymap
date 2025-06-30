/**
 * Guard de roles para control de acceso basado en roles
 * Verifica si el usuario tiene los roles necesarios para acceder a rutas específicas
 * Obtiene el rol del usuario desde el backend o desde el token JWT
 */
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { ReservaService } from './services/reserva.service';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  
  constructor(
    private router: Router,
    private reservaService: ReservaService
  ) {}

  /**
   * Verifica si el usuario tiene los roles requeridos para acceder a una ruta
   * @param route Información de la ruta activa
   * @param state Estado actual del router
   * @returns Observable<boolean> true si tiene permisos, false en caso contrario
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    // Obtener los roles requeridos desde la configuración de la ruta
    const requiredRoles = route.data['roles'] as string[];
    if (!requiredRoles || requiredRoles.length === 0) {
      return of(true); // Sin roles requeridos, permitir acceso
    }
    
    // Intentar obtener el rol del usuario desde el backend
    return this.getUserRoleFromBackend().pipe(
      map(userRole => {
        // Verificar si el usuario tiene alguno de los roles requeridos
        const hasRequiredRole = requiredRoles.some(role => (userRole || '').toLowerCase() === (role || '').toLowerCase());
        if (hasRequiredRole) {
          return true; // Permitir acceso
        } else {
          this.router.navigate(['/']); // Redirigir al inicio si no tiene permisos
          return false;
        }
      }),
      catchError(error => {
        // Si falla el backend, intentar obtener el rol desde el token JWT
        const userRole = this.getUserRoleFromToken();
        const hasRequiredRole = requiredRoles.some(role => (userRole || '').toLowerCase() === (role || '').toLowerCase());
        if (hasRequiredRole) {
          return of(true); // Permitir acceso
        } else {
          this.router.navigate(['/']); // Redirigir al inicio si no tiene permisos
          return of(false);
        }
      })
    );
  }

  /**
   * Obtiene el rol del usuario desde el backend
   * @returns Observable<string> Rol del usuario
   */
  private getUserRoleFromBackend(): Observable<string> {
    return this.reservaService.obtenerUsuarioActual().pipe(
      map(response => {
        // Extraer el tipo de usuario de la respuesta del backend
        if (response && response.data && response.data.tipoUsuario) {
          return response.data.tipoUsuario;
        }
        if (response && response.tipoUsuario) {
          return response.tipoUsuario;
        }
        return 'Usuario'; // Rol por defecto
      }),
      catchError(error => {
        return of('Usuario'); // Rol por defecto en caso de error
      })
    );
  }

  /**
   * Obtiene el rol del usuario desde el token JWT almacenado
   * @returns string Rol del usuario extraído del token
   */
  private getUserRoleFromToken(): string {
    try {
      // Intentar obtener el rol desde el ID token
      const idToken = localStorage.getItem('idToken');
      if (idToken) {
        const tokenPayload = this.decodeJwtToken(idToken);
        return tokenPayload.extension_Roles || 'Usuario';
      }
      
      // Si no hay ID token, intentar con el JWT
      const jwt = localStorage.getItem('jwt');
      if (jwt) {
        const tokenPayload = this.decodeJwtToken(jwt);
        return tokenPayload.extension_Roles || 'Usuario';
      }
      
      return 'Usuario'; // Rol por defecto
    } catch (error) {
      return 'Usuario'; // Rol por defecto en caso de error
    }
  }

  /**
   * Decodifica un token JWT para extraer su payload
   * @param token Token JWT a decodificar
   * @returns any Payload del token decodificado
   */
  private decodeJwtToken(token: string): any {
    try {
      // Extraer la parte del payload del token (segunda parte separada por puntos)
      const base64Url = token.split('.')[1];
      // Convertir de base64url a base64
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      // Decodificar el payload
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      return {}; // Retornar objeto vacío en caso de error
    }
  }
} 