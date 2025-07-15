/**
 * Guard de autenticación para proteger rutas
 * Verifica si el usuario está autenticado antes de permitir acceso a rutas protegidas
 * Redirige a Azure AD B2C si el usuario no está autenticado
 */
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { NavigationService } from './services/navigation.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private router: Router,
    private msalService: MsalService,
    private navigation: NavigationService
  ) {}

  /**
   * Verifica si el usuario puede acceder a una ruta protegida
   * @returns true si el usuario está autenticado, false en caso contrario
   */
  canActivate(): boolean {
    // Verificar si hay tokens de autenticación en el almacenamiento local
    const jwt = localStorage.getItem('jwt');
    const idToken = localStorage.getItem('idToken');
    const userInfo = localStorage.getItem('userInfo');
    
    // El usuario está autenticado si tiene al menos un token válido
    const isAuthenticated = !!(jwt || idToken);
    
    if (isAuthenticated) {
      // Verificar que la información del usuario sea válida
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          // Aquí se podría agregar validación adicional del usuario
        } catch (error) {
          // Error al parsear información del usuario, pero permitir acceso
        }
      }
      
      return true; // Permitir acceso a la ruta
    } else {
      // Usuario no autenticado, redirigir a Azure AD B2C
      this.redirectToAzureADB2C();
      return false; // Denegar acceso a la ruta
    }
  }

  /**
   * Redirige al usuario a Azure AD B2C para autenticación
   * Limpia el cache de MSAL y inicia el flujo de login
   */
  private redirectToAzureADB2C() {
    try {
      // Limpiar cualquier estado anterior de autenticación
      this.msalService.instance.clearCache();
      
      // Redirigir a Azure AD B2C usando MSAL
      this.msalService.loginRedirect({
        scopes: ['openid', 'profile', 'email']
      });
      
    } catch (error) {
      // Fallback: redirigir directamente a la URL de Azure AD B2C si MSAL falla
      const loginUrl = 'https://duocdesarrollocloudnative.b2clogin.com/DuocDesarrolloCloudNative.onmicrosoft.com/oauth2/v2.0/authorize?p=B2C_1_DuocDesarrolloCloudNative_Login&client_id=ad16d15c-7d6e-4f58-8146-4b5b3d7b7124&nonce=defaultNonce&redirect_uri=http%3A%2F%2Flocalhost%3A4200&scope=openid&response_type=id_token&prompt=login';
      
      this.navigation.goTo(loginUrl);
    }
  }
} 