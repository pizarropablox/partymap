/**
 * Componente de barra de navegación principal
 * Maneja la autenticación, búsqueda de ubicaciones, navegación y control de acceso
 * Incluye funcionalidades de login/logout, búsqueda con autocompletado y menú móvil
 */
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UbicacionService, UbicacionResponseDTO } from '../../services/ubicacion.service';
import { MapNavigationService } from '../../services/map-navigation.service';
import { GeocodingService } from '../../services/geocoding.service';
import { ReservaService, UsuarioInfo } from '../../services/reserva.service';
import { debounceTime, distinctUntilChanged, switchMap, filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { InteractionStatus, EventMessage, EventType, AuthenticationResult } from '@azure/msal-browser';
import { MensajeService } from '../../shared/mensaje.service';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit, OnDestroy {
  // URL de Azure B2C para login directo
  private readonly AZURE_B2C_LOGIN_URL = 'https://duocdesarrollocloudnative.b2clogin.com/DuocDesarrolloCloudNative.onmicrosoft.com/oauth2/v2.0/authorize?p=B2C_1_DuocDesarrolloCloudNative_Login&client_id=ad16d15c-7d6e-4f58-8146-4b5b3d7b7124&nonce=defaultNonce&redirect_uri=http%3A%2F%2Flocalhost%3A4200&scope=openid&response_type=id_token&prompt=login';

  // Propiedades de estado de la interfaz
  isMobileMenuOpen = false;                    // Controla la visibilidad del menú móvil
  searchTerm = '';                            // Término de búsqueda actual
  searchResults: UbicacionResponseDTO[] = []; // Resultados de búsqueda de ubicaciones
  isSearching = false;                        // Indica si se está realizando una búsqueda
  showResults = false;                        // Controla la visibilidad de los resultados
  loginDisplay = false;                       // Indica si el usuario está autenticado
  userName = '';                              // Nombre del usuario autenticado
  userRole = '';                              // Rol del usuario autenticado
  
  // Propiedades privadas para gestión de observables
  private searchSubject = new Subject<string>();           // Para búsqueda con debounce
  private readonly _destroying$ = new Subject<void>();     // Para limpiar suscripciones

  constructor(
    private ubicacionService: UbicacionService,        // Servicio de ubicaciones
    private mapNavigationService: MapNavigationService, // Servicio de navegación del mapa
    private geocodingService: GeocodingService,         // Servicio de geocodificación
    private router: Router,                             // Router de Angular
    private msalService: MsalService,                   // Servicio de autenticación MSAL
    private msalBroadcastService: MsalBroadcastService, // Servicio de eventos MSAL
    private reservaService: ReservaService,             // Servicio de reservas
    private cdr: ChangeDetectorRef,                     // Para detección de cambios manual
    private mensajeService: MensajeService,              // Servicio de mensajes
    private navigation: NavigationService
  ) {
  }

  ngOnInit(): void {
    // Verificar inmediatamente si hay un token en la URL (redirección desde Azure AD)
    this.checkForTokenInUrl();
    
    // Probar geocoding al inicializar para verificar conectividad
    this.testGeocodingOnInit();
    
    // Configurar búsqueda con debounce para mejorar rendimiento
    this.searchSubject.pipe(
      debounceTime(300), // Esperar 300ms después de que el usuario deje de escribir
      distinctUntilChanged(), // Solo buscar si el término cambió
      switchMap(term => {
        if (term.trim().length === 0) {
          this.searchResults = [];
          return [];
        }
        this.isSearching = true;
        return this.ubicacionService.buscarUbicaciones(term, term);
      })
    ).subscribe({
      next: (results) => {
        this.searchResults = results;
        this.isSearching = false;
        this.showResults = results.length > 0;
      },
      error: (error) => {
        this.searchResults = [];
        this.isSearching = false;
        this.showResults = false;
      }
    });

    // Configurar autenticación con Azure AD
    this.msalService.instance.enableAccountStorageEvents();
    
    // Verificar estado inicial de autenticación
    const accounts = this.msalService.instance.getAllAccounts();
    
    // Manejar respuesta de autenticación al cargar
    this.handleAuthResponse();
    
    // Escuchar eventos de agregar/remover cuentas
    this.msalBroadcastService.msalSubject$
      .pipe(
        filter((msg: EventMessage) => msg.eventType === EventType.ACCOUNT_ADDED || msg.eventType === EventType.ACCOUNT_REMOVED),
        takeUntil(this._destroying$)
      )
      .subscribe((result: EventMessage) => {
        if (this.msalService.instance.getAllAccounts().length === 0) {
          window.location.pathname = '/'; // Redirigir al inicio si no hay cuentas
        } else {
          this.setLoginDisplay();
        }
      });

    // Escuchar cambios en el estado de interacción de autenticación
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this._destroying$)
      )
      .subscribe(() => {
        this.setLoginDisplay();
        this.checkAndSetActiveAccount();
        // Obtener y guardar token después de la interacción
        this.acquireAndSaveToken();
      });

    // Verificar permisos de acceso a diferentes secciones
    this.checkEventsAccess();
    this.checkReportesAccess();
    this.checkUsuarioAccess();
    this.checkReservasAccess();
    
    // Verificar si ya hay tokens y cargar información del usuario
    const hasTokens = !!(localStorage.getItem('jwt') || localStorage.getItem('idToken') || localStorage.getItem('accessToken'));
    if (hasTokens) {
      console.log('Tokens encontrados en ngOnInit, cargando información del usuario...');
      this.loadUserInfoFromBackend();
      // Verificar disponibilidad del backend
      this.checkBackendAvailability();
    }
  }

  /**
   * Limpiar recursos al destruir el componente
   */
  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }

  /**
   * Actualizar el estado de visualización del login
   * Determina si mostrar elementos de usuario autenticado y carga información del usuario
   */
  setLoginDisplay() {
    const accounts = this.msalService.instance.getAllAccounts();
    const previousState = this.loginDisplay;
    
    // Verificar si hay tokens en localStorage
    const jwt = localStorage.getItem('jwt');
    const idToken = localStorage.getItem('idToken');
    const userInfo = localStorage.getItem('userInfo');
    
    // El usuario está autenticado si tiene cuentas de MSAL O tokens en localStorage
    const isAuthenticated = accounts.length > 0 || !!(jwt || idToken);
    
    // Forzar la actualización del estado
    this.loginDisplay = isAuthenticated;
    
    // Extraer información del usuario si está autenticado
    if (this.loginDisplay) {
      // Siempre recargar desde el backend para asegurar información actualizada
      this.loadUserInfoFromBackend();
      
      // Debuggear si el nombre es "unknown" o está vacío
      if (!this.userName || this.userName === 'Usuario' || this.userName === 'unknown') {
        this.debugTokenInfo();
      }
    } else {
      this.userName = '';
      this.userRole = '';
    }
    
    // Si hay tokens pero no hay cuenta activa, intentar establecer una
    if (this.loginDisplay && accounts.length === 0 && (jwt || idToken)) {
      this.attemptToSetActiveAccountFromTokens();
    }
  }

  /**
   * Cargar información del usuario desde el backend
   * Obtiene datos actualizados del usuario autenticado
   */
  private loadUserInfoFromBackend() {
    // Inicializar variables antes de la llamada
    this.userName = '';
    this.userRole = '';
    
    // Forzar detección de cambios inicial
    this.cdr.detectChanges();
    
    this.reservaService.obtenerUsuarioActual().subscribe({
      next: (response) => {
        if (response && response.data) {
          const usuario = response.data;
          
          this.userName = usuario.nombre || usuario.email || 'Usuario';
          this.userRole = usuario.tipoUsuario || 'Usuario';
          
          // Actualizar permisos basados en el rol del backend
          this.updatePermissions();
          
          // Forzar detección de cambios inmediatamente
          this.cdr.detectChanges();
        } else if (response && response.tipoUsuario) {
          // Caso donde la respuesta viene directamente
          this.userName = response.nombre || response.email || 'Usuario';
          this.userRole = response.tipoUsuario || 'Usuario';
          
          // Actualizar permisos basados en el rol del backend
          this.updatePermissions();
          
          // Forzar detección de cambios inmediatamente
          this.cdr.detectChanges();
        } else {
          // Fallback si no hay datos del backend
          this.extractUserInfoFromToken();
        }
      },
      error: (error) => {
        if (error.status === 401) {
          this.mensajeService.mostrarAdvertencia('Tu sesión ha expirado o no tienes permisos. Por favor, inicia sesión nuevamente.');
          // Limpiar tokens y redirigir al login
          localStorage.removeItem('jwt');
          localStorage.removeItem('idToken');
          localStorage.removeItem('accessToken');
          setTimeout(() => {
            this.navigation.goTo('/login');
          }, 1500);
        } else {
          // Fallback al método anterior si falla el backend
          this.extractUserInfoFromToken();
          // Asegurar que los permisos se actualicen incluso con fallback
          setTimeout(() => {
            this.updatePermissions();
            this.cdr.detectChanges();
          }, 50);
        }
      }
    });
  }

  // Método centralizado para actualizar permisos
  private updatePermissions() {
    this.checkEventsAccess();
    this.checkReportesAccess();
    this.checkUsuarioAccess();
    this.checkReservasAccess();
  }

  // Verificar si el backend está disponible
  private checkBackendAvailability() {
    // Solo verificar si hay tokens (usuario autenticado)
    const hasTokens = !!(localStorage.getItem('idToken') || localStorage.getItem('accessToken') || localStorage.getItem('jwt'));
    if (!hasTokens) {
      return;
    }

    // Intentar hacer una petición simple al backend
    this.reservaService.obtenerUsuarioActual().subscribe({
      next: () => {
        // Backend está funcionando, no hacer nada
      },
      error: (error) => {
        console.warn('Backend no disponible:', error);
        // Mostrar mensaje informativo solo si es un error de conexión
        if (error.status === 0 || error.status >= 500) {
          this.mensajeService.mostrarAdvertencia('El servidor no está disponible en este momento. Algunas funciones pueden no estar disponibles.');
        }
      }
    });
  }

  // Extraer información del usuario del token
  private extractUserInfoFromToken() {
    try {
      // Intentar obtener información del ID token primero
      const idToken = localStorage.getItem('idToken');
      if (idToken) {
        const tokenPayload = this.decodeJwtToken(idToken);
        
        // Buscar el nombre del usuario en múltiples campos posibles
        this.userName = tokenPayload.name || 
                       tokenPayload.given_name || 
                       tokenPayload.preferred_username || 
                       tokenPayload.email ||
                       tokenPayload.upn ||
                       tokenPayload.unique_name ||
                       tokenPayload.sub ||
                       'Usuario';
        
        // Si tenemos given_name y family_name, combinarlos
        if (tokenPayload.given_name && tokenPayload.family_name) {
          this.userName = `${tokenPayload.given_name} ${tokenPayload.family_name}`;
        }
        
        // Extraer rol del usuario - buscar específicamente en extension_Roles
        this.userRole = tokenPayload.extension_Roles || 
                       tokenPayload.role || 
                       tokenPayload.roles?.[0] || 
                       'Usuario';
        
        // Actualizar permisos
        this.updatePermissions();
        this.cdr.detectChanges();
        
        return;
      }
      
      // Si no hay ID token, intentar con JWT
      const jwt = localStorage.getItem('jwt');
      if (jwt) {
        const tokenPayload = this.decodeJwtToken(jwt);
        
        this.userName = tokenPayload.name || 
                       tokenPayload.given_name || 
                       tokenPayload.preferred_username || 
                       tokenPayload.email ||
                       tokenPayload.upn ||
                       tokenPayload.unique_name ||
                       tokenPayload.sub ||
                       'Usuario';
        
        if (tokenPayload.given_name && tokenPayload.family_name) {
          this.userName = `${tokenPayload.given_name} ${tokenPayload.family_name}`;
        }
        
        this.userRole = tokenPayload.extension_Roles || 
                       tokenPayload.role || 
                       tokenPayload.roles?.[0] || 
                       'Usuario';
        
        // Actualizar permisos
        this.updatePermissions();
        this.cdr.detectChanges();
        
        return;
      }
      
      // Si no hay tokens, intentar con userInfo en localStorage
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          this.userName = user.name || user.displayName || user.username || 'Usuario';
          this.userRole = user.role || user.roles?.[0] || 'Usuario';
          
          // Actualizar permisos
          this.updatePermissions();
          this.cdr.detectChanges();
        } catch (e) {
          console.error('Error parsing userInfo:', e);
        }
      }
      
    } catch (error) {
      console.error('Error extracting user info from token:', error);
      this.userName = 'Usuario';
      this.userRole = 'Usuario';
      
      // Actualizar permisos incluso con valores por defecto
      this.updatePermissions();
      this.cdr.detectChanges();
    }
  }

  private decodeJwtToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding JWT token:', error);
      return {};
    }
  }

  private attemptToSetActiveAccountFromTokens() {
    try {
      const idToken = localStorage.getItem('idToken');
      if (idToken) {
        const tokenPayload = this.decodeJwtToken(idToken);
        // Intentar establecer la cuenta activa basada en el token
        const accounts = this.msalService.instance.getAllAccounts();
        const matchingAccount = accounts.find(account => 
          account.username === tokenPayload.preferred_username ||
          account.username === tokenPayload.email ||
          account.username === tokenPayload.upn
        );
        
        if (matchingAccount) {
          this.msalService.instance.setActiveAccount(matchingAccount);
        }
      }
    } catch (error) {
      console.error('Error attempting to set active account from tokens:', error);
    }
  }

  checkAndSetActiveAccount() {
    const activeAccount = this.msalService.instance.getActiveAccount();
    const accounts = this.msalService.instance.getAllAccounts();

    if (!activeAccount && accounts.length > 0) {
      this.msalService.instance.setActiveAccount(accounts[0]);
    }
  }

  login() {
    this.navigation.goTo(this.AZURE_B2C_LOGIN_URL);
  }

  private handleInteractionInProgress() {
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this._destroying$)
      )
      .subscribe(() => {
        this.setLoginDisplay();
        this.checkAndSetActiveAccount();
      });
  }

  logout() {
    console.log('Iniciando proceso de logout...');
    
    // Limpiar todos los tokens del localStorage
    const tokensToRemove = [
      'jwt', 'idToken', 'userInfo', 'accessToken', 
      'msal.access_token.key', 'msal.id_token.key',
      'msal.client.info', 'msal.nonce.idtoken',
      'msal.state.login', 'msal.session.state',
      'msal.error', 'msal.error.description'
    ];
    
    tokensToRemove.forEach(token => {
      localStorage.removeItem(token);
      sessionStorage.removeItem(token);
    });
    
    // Limpiar también cualquier token en sessionStorage
    sessionStorage.clear();
    
    // Resetear estado del componente
    this.loginDisplay = false;
    this.userName = '';
    this.userRole = '';
    
    console.log('Tokens limpiados, redirigiendo a home...');
    
    // Redirigir a home inmediatamente
    this.router.navigate(['/']);
    
    // También intentar logout de MSAL si está disponible
    try {
      this.msalService.logoutPopup({
        postLogoutRedirectUri: window.location.origin,
      }).subscribe({
        next: () => {
          console.log('MSAL logout successful');
        },
        error: (error) => {
          console.log('MSAL logout failed, pero logout local exitoso:', error);
        }
      });
    } catch (error) {
      console.log('MSAL no disponible, logout local completado');
    }
  }

  private testGeocodingOnInit() {
    // Test geocoding functionality on component init
    this.testGeocoding();
  }

  onSearchInput(event: any) {
    const value = event.target.value;
    this.searchSubject.next(value);
  }

  onSearchFocus() {
    if (this.searchResults.length > 0) {
      this.showResults = true;
    }
  }

  onSearchBlur() {
    // Delay hiding results to allow for clicks
    setTimeout(() => {
      this.showResults = false;
    }, 200);
  }

  selectResult(ubicacion: UbicacionResponseDTO) {
    this.searchTerm = ubicacion.nombre;
    this.showResults = false;
    
    // Navegar a la ubicación en el mapa
    this.mapNavigationService.navigateToLocation(ubicacion);
    
    // Cerrar menú móvil si está abierto
    if (this.isMobileMenuOpen) {
      this.isMobileMenuOpen = false;
    }
  }

  onSearchEnter(event: any) {
    if (event.key === 'Enter' && this.searchTerm.trim()) {
      // Intentar geocodificar la dirección ingresada
      this.geocodingService.geocodeAddress(this.searchTerm)
        .subscribe({
          next: (coordinates) => {
            if (coordinates) {
              const location = {
                nombre: this.searchTerm,
                direccion: this.searchTerm,
                comuna: 'Santiago',
                latitud: coordinates.lat,
                longitud: coordinates.lng
              };
              this.mapNavigationService.navigateToLocation(location);
              this.showResults = false;
            }
          },
          error: (error) => {
            console.error('Error geocoding address:', error);
          }
        });
    }
  }

  testGeocoding() {
    // Test geocoding with a sample address
    this.geocodingService.geocodeAddress('Santiago, Chile')
      .subscribe({
        next: (result) => {
          // Geocoding test completed successfully
        },
        error: (error) => console.error('Geocoding test error:', error)
      });
  }

  private navigateToAddress(address: string) {
    this.geocodingService.geocodeAddress(address)
      .subscribe({
        next: (coordinates) => {
          if (coordinates) {
            const location = {
              nombre: address,
              direccion: address,
              comuna: 'Santiago',
              latitud: coordinates.lat,
              longitud: coordinates.lng
            };
            this.mapNavigationService.navigateToLocation(location);
          }
        },
        error: (error) => {
          console.error('Error navigating to address:', error);
        }
      });
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
  }

  private handleAuthResponse() {
    this.msalBroadcastService.msalSubject$
      .pipe(
        filter((msg: EventMessage) => msg.eventType === EventType.LOGIN_SUCCESS),
        takeUntil(this._destroying$)
      )
      .subscribe((result: EventMessage) => {
        this.setLoginDisplay();
        this.acquireAndSaveToken();
      });

    this.msalBroadcastService.msalSubject$
      .pipe(
        filter((msg: EventMessage) => msg.eventType === EventType.ACQUIRE_TOKEN_SUCCESS),
        takeUntil(this._destroying$)
      )
      .subscribe((result: EventMessage) => {
        this.acquireAndSaveToken();
      });
  }

  private forceUpdateLoginState() {
    // Forzar actualización del estado de login
    setTimeout(() => {
      this.setLoginDisplay();
      this.checkEventsAccess();
      this.checkReportesAccess();
      this.checkUsuarioAccess();
      this.checkReservasAccess();
    }, 100);
  }

  private handleTokenInUrlFragment() {
    // Verificar si hay un token en el fragmento de la URL
    const hash = window.location.hash;
    if (hash && hash.includes('access_token')) {
      // Extraer el token del fragmento
      const params = new URLSearchParams(hash.substring(1));
      const accessToken = params.get('access_token');
      const idToken = params.get('id_token');
      
      if (accessToken) {
        localStorage.setItem('accessToken', accessToken);
      }
      
      if (idToken) {
        localStorage.setItem('idToken', idToken);
        
        // Decodificar y extraer información del usuario
        try {
          const tokenPayload = this.decodeJwtToken(idToken);
          
          // Guardar información del usuario
          const userInfo = {
            name: tokenPayload.name || tokenPayload.given_name || tokenPayload.preferred_username,
            email: tokenPayload.email || tokenPayload.upn,
            role: tokenPayload.extension_Roles || tokenPayload.role,
            sub: tokenPayload.sub
          };
          
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          // Actualizar estado del componente
          this.setLoginDisplay();
          
        } catch (error) {
          console.error('Error processing ID token:', error);
        }
      }
      
      // Limpiar la URL
      this.cleanUrlAfterAuth();
    }
  }

  private cleanUrlAfterAuth() {
    // Remover el fragmento de la URL después de procesar la autenticación
    if (window.location.hash) {
      const cleanUrl = window.location.pathname + window.location.search;
      window.history.replaceState({}, document.title, cleanUrl);
    }
  }

  private acquireAndSaveToken() {
    const accounts = this.msalService.instance.getAllAccounts();
    if (accounts.length > 0) {
      const account = accounts[0];
      this.msalService.instance.setActiveAccount(account);
      
      // Solicitar token de acceso
      this.msalService.acquireTokenSilent({
        scopes: ['user.read'],
        account: account
      }).subscribe({
        next: (response) => {
          // Guardar tokens
          if (response.accessToken) {
            localStorage.setItem('accessToken', response.accessToken);
          }
          
          if (response.idToken) {
            localStorage.setItem('idToken', response.idToken);
          }
          
          // Actualizar estado
          this.setLoginDisplay();
          
        },
        error: (error) => {
          console.error('Error acquiring token:', error);
          
          // Si falla el token silencioso, intentar con popup
          this.msalService.acquireTokenPopup({
            scopes: ['user.read']
          }).subscribe({
            next: (response) => {
              if (response.accessToken) {
                localStorage.setItem('accessToken', response.accessToken);
              }
              
              if (response.idToken) {
                localStorage.setItem('idToken', response.idToken);
              }
              
              this.setLoginDisplay();
            },
            error: (popupError) => {
              console.error('Error acquiring token via popup:', popupError);
            }
          });
        }
      });
    }
  }

  private checkAuthStatusFromLocalStorage() {
    const jwt = localStorage.getItem('jwt');
    const idToken = localStorage.getItem('idToken');
    const accessToken = localStorage.getItem('accessToken');
    
    if (jwt || idToken || accessToken) {
      this.setLoginDisplay();
      return true;
    }
    
    return false;
  }

  private checkForTokenInUrl() {
    // Verificar si hay un token en la URL al cargar la página
    const hash = window.location.hash;
    if (hash && (hash.includes('access_token') || hash.includes('id_token'))) {
      this.handleTokenInUrlFragment();
    } else {
      // Si no hay token en URL, verificar localStorage
      this.checkAuthStatusFromLocalStorage();
    }
  }

  private checkEventsAccess() {
    const userRoleLower = this.userRole.toLowerCase();
    const hasAccess = userRoleLower === 'administrador' || userRoleLower === 'productor';
  }

  private checkReportesAccess() {
    const userRoleLower = this.userRole.toLowerCase();
    const hasAccess = userRoleLower === 'administrador';
  }

  private checkUsuarioAccess() {
    const userRoleLower = this.userRole.toLowerCase();
    const hasAccess = userRoleLower === 'administrador';
  }

  private checkReservasAccess() {
    const userRoleLower = this.userRole.toLowerCase();
    const hasAccess = userRoleLower === 'cliente' || userRoleLower === 'administrador';
  }

  private debugTokenInfo() {
    // Método de debug removido para limpiar la consola
  }

  // Métodos getter para los permisos
  get canAccessEvents(): boolean {
    const userRoleLower = this.userRole.toLowerCase();
    return userRoleLower === 'administrador' || userRoleLower === 'productor';
  }

  get canAccessReportes(): boolean {
    const userRoleLower = this.userRole.toLowerCase();
    return userRoleLower === 'administrador';
  }

  get canAccessProductor(): boolean {
    const userRoleLower = this.userRole.toLowerCase();
    return userRoleLower === 'productor' || userRoleLower === 'administrador';
  }

  get canAccessReservas(): boolean {
    const userRoleLower = this.userRole.toLowerCase();
    return userRoleLower === 'cliente' || userRoleLower === 'administrador';
  }

  get canAccessUsuario(): boolean {
    const userRoleLower = this.userRole.toLowerCase();
    return userRoleLower === 'administrador';
  }

  checkAuthBeforeNavigate(route: string): void {
    if (!this.loginDisplay) {
      this.mensajeService.mostrarAdvertencia('Debes iniciar sesión para acceder a esta página.');
      return;
    }
    
    // La navegación se realizará automáticamente con routerLink
    this.closeMobileMenu();
  }
}
