/**
 * Componente principal de la aplicación Angular
 * Maneja la autenticación con Azure AD (MSAL), navegación y estado global de la app
 * Incluye gestión de tokens JWT, redirecciones y control de sesión de usuario
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { NavbarComponent } from './components/navbar/navbar';
import { FooterComponent } from './components/footer/footer';
import { MensajeModalComponent } from './shared/mensaje-modal.component';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { InteractionStatus, EventMessage, EventType, AuthenticationResult } from '@azure/msal-browser';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'html-app',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, FooterComponent, MensajeModalComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App implements OnInit, OnDestroy {
  // Propiedades para controlar la interfaz de usuario
  isMapPage = false;        // Indica si estamos en la página del mapa principal
  isIframe = false;         // Indica si la app está corriendo dentro de un iframe
  loginDisplay = false;     // Controla la visualización del estado de login
  private readonly _destroying$ = new Subject<void>(); // Para limpiar suscripciones

  constructor(
    private msalService: MsalService, 
    private msalBroadcastService: MsalBroadcastService,
    private router: Router,
    private authService: AuthService
  ) {
    // Inicializar el servicio de autenticación de Microsoft
    this.authService.initialize();
    
    // Escuchar cambios de ruta para detectar cuando estamos en la página del mapa
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.isMapPage = event.url === '/' || event.url === '/home';
    });
  }

  ngOnInit(): void {
    // Detectar si la aplicación está corriendo en un iframe
    this.isIframe = window !== window.parent && !window.opener;

    // Verificar inmediatamente si hay un token en la URL (caso de redirección desde Azure AD)
    this.checkForTokenInUrl();

    // Manejar la respuesta de autenticación al cargar la aplicación
    this.handleAuthResponse();

    // Habilitar eventos de almacenamiento de cuentas para sincronización entre pestañas
    this.authService.enableAccountStorageEvents();
    
    // Escuchar eventos de agregar/remover cuentas (solo si MSAL está disponible)
    if (this.msalBroadcastService) {
      this.msalBroadcastService.msalSubject$
        .pipe(
          filter((msg: EventMessage) => msg.eventType === EventType.ACCOUNT_ADDED || msg.eventType === EventType.ACCOUNT_REMOVED),
          takeUntil(this._destroying$)
        )
        .subscribe((result: EventMessage) => {
          if (this.authService.getAllAccounts().length === 0) {
            // Si no hay cuentas, redirigir al inicio
            window.location.pathname = '/';
          } else {
            this.setLoginDisplay();
          }
        });
    }

    // Escuchar cambios en el estado de interacción de autenticación (solo si MSAL está disponible)
    if (this.msalBroadcastService) {
      this.msalBroadcastService.inProgress$
        .pipe(
          filter((status: InteractionStatus) => status === InteractionStatus.None),
          takeUntil(this._destroying$)
        )
        .subscribe(() => {
          this.setLoginDisplay();
          this.checkAndSetActiveAccount();
          // Obtener y guardar el token después de que la interacción se complete
          this.acquireAndSaveToken();
        });
    }
  }

  /**
   * Verificar si hay un token en la URL al inicializar la aplicación
   * Esto ocurre cuando el usuario es redirigido desde Azure AD después del login
   */
  private checkForTokenInUrl() {
    const currentUrl = window.location.href;
    if (currentUrl.includes('#id_token=')) {
      // Procesar el token inmediatamente
      this.handleTokenInUrlFragment();
    }
  }

  /**
   * Manejar la respuesta de autenticación de Azure AD
   * Procesa tokens recibidos por redirección y configura la sesión del usuario
   */
  private handleAuthResponse() {
    // Verificar si hay un token en el fragmento de la URL
    const currentUrl = window.location.href;
    if (currentUrl.includes('#id_token=')) {
      this.authService.handleRedirectPromise().then((response: AuthenticationResult | null) => {
        if (response) {
          // Configurar la cuenta activa
          this.authService.setActiveAccount(response.account);
          
          // Guardar el token en localStorage inmediatamente
          this.saveTokensToLocalStorage(response);
          
          this.setLoginDisplay();
          
          // Limpiar la URL después de procesar el token
          this.cleanUrlAfterAuth();
        } else {
          // Verificar si hay un token en el fragmento pero MSAL no lo procesó
          if (currentUrl.includes('#id_token=')) {
            this.handleTokenInUrlFragment();
          } else {
            // Verificar si ya hay tokens guardados
            this.checkExistingTokens();
          }
        }
      }).catch(() => {
        // Si hay un token en la URL pero hubo error, intentar procesarlo manualmente
        if (currentUrl.includes('#id_token=')) {
          this.handleTokenInUrlFragment();
        }
      });
    }
  }

  /**
   * Procesar manualmente un token JWT en el fragmento de la URL
   * Extrae y decodifica el token para obtener información del usuario
   */
  private handleTokenInUrlFragment() {
    try {
      const urlParams = new URLSearchParams(window.location.hash.substring(1));
      const idToken = urlParams.get('id_token');
      
      if (idToken) {
        // Guardar el token en almacenamiento local y de sesión
        localStorage.setItem('idToken', idToken);
        sessionStorage.setItem('idToken', idToken);
        
        // Intentar decodificar el token para obtener información del usuario
        try {
          const tokenPayload = JSON.parse(atob(idToken.split('.')[1]));
          
          const userInfo = {
            name: tokenPayload.name || tokenPayload.given_name + ' ' + tokenPayload.family_name,
            username: tokenPayload.emails?.[0] || tokenPayload.sub,
            homeAccountId: tokenPayload.sub,
            localAccountId: tokenPayload.sub
          };
          
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          sessionStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          // Actualizar el estado de login
          this.setLoginDisplay();
          
        } catch (decodeError) {
          // Error al decodificar el token, continuar sin información del usuario
        }
        
        // Limpiar la URL
        this.cleanUrlAfterAuth();
      }
    } catch (error) {
      // Error al procesar el token, continuar sin autenticación
    }
  }

  /**
   * Limpiar la URL después de procesar la autenticación
   * Remueve los fragmentos de token para una URL más limpia
   */
  private cleanUrlAfterAuth() {
    // Remover el fragmento de la URL para limpiarla
    if (window.location.hash) {
      // Obtener la URL base sin el fragmento
      const baseUrl = window.location.href.split('#')[0];
      
      // Limpiar la URL usando history API
      window.history.replaceState({}, document.title, baseUrl);
    }
  }

  /**
   * Guardar tokens de autenticación en el almacenamiento local
   * Incluye access token, ID token e información del usuario
   */
  private saveTokensToLocalStorage(response: AuthenticationResult) {
    if (response.accessToken) {
      localStorage.setItem('jwt', response.accessToken);
      sessionStorage.setItem('jwt', response.accessToken); // Backup en sessionStorage
    }
    
    if (response.idToken) {
      localStorage.setItem('idToken', response.idToken);
      sessionStorage.setItem('idToken', response.idToken); // Backup en sessionStorage
    }
    
    // Guardar información del usuario
    if (response.account) {
      const userInfo = {
        name: response.account.name,
        username: response.account.username,
        homeAccountId: response.account.homeAccountId,
        localAccountId: response.account.localAccountId
      };
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo)); // Backup en sessionStorage
    }
  }

  /**
   * Verificar si ya existen tokens guardados en el almacenamiento
   * Restaura la sesión del usuario si encuentra tokens válidos
   */
  private checkExistingTokens() {
    const jwt = localStorage.getItem('jwt');
    const idToken = localStorage.getItem('idToken');
    const userInfo = localStorage.getItem('userInfo');
    
    if (jwt && idToken && userInfo) {
      this.setLoginDisplay();
    }
  }

  /**
   * Obtener y guardar token de acceso de forma silenciosa
   * Intenta renovar el token sin interacción del usuario
   */
  private acquireAndSaveToken() {
    const account = this.authService.getActiveAccount();
    if (account) {
      this.authService.acquireTokenSilent({
        scopes: ['openid', 'profile', 'email'],
        account: account
      }).subscribe({
        next: (response: AuthenticationResult) => {
          // Guardar tokens en localStorage
          this.saveTokensToLocalStorage(response);
        },
        error: (error) => {
          // Si el error es de interacción requerida, intentar login interactivo
          if (error.name === 'InteractionRequiredAuthError') {
            this.msalService.loginRedirect({
              scopes: ['openid', 'profile', 'email']
            });
          }
        }
      });
    }
  }

  /**
   * Actualizar el estado de visualización del login
   * Determina si mostrar elementos de usuario autenticado o no
   */
  setLoginDisplay() {
    const accounts = this.authService.getAllAccounts();
    this.loginDisplay = accounts.length > 0;
  }

  /**
   * Verificar y establecer la cuenta activa
   * Asegura que siempre haya una cuenta activa si existen cuentas disponibles
   */
  checkAndSetActiveAccount() {
    let activeAccount = this.authService.getActiveAccount();

    if (!activeAccount && this.authService.getAllAccounts().length > 0) {
      let accounts = this.authService.getAllAccounts();
      this.authService.setActiveAccount(accounts[0]);
    }
  }

  /**
   * Limpiar recursos al destruir el componente
   * Cancela todas las suscripciones para evitar memory leaks
   */
  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }
}
