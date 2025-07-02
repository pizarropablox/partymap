import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { EndpointsService } from '../../services/endpoints.service';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../../config/api-endpoints';

interface EstadisticasReservas {
  totalReservas: number;
  reservasActivas: number;
  reservasCanceladas: number;
  porcentajeActivas: number;
  porcentajeCanceladas: number;
  totalIngresos: number;
  promedioPrecio: number;
  precioMaximo: number;
  precioMinimo: number;
  totalEntradas: number;
  promedioEntradas: number;
  maxEntradas: number;
  minEntradas: number;
}

interface EstadisticasEventos {
  totalEventos: number;
  eventosActivos: number;
  eventosInactivos: number;
  eventosDisponibles: number;
  eventosProximos: number;
  eventosPasados: number;
}

interface EstadisticasUsuarios {
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosInactivos: number;
  productores: number;
  clientes: number;
  administradores: number;
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit {
  // URL de Azure B2C para login
  private readonly AZURE_B2C_LOGIN_URL = 'https://duocdesarrollocloudnative.b2clogin.com/DuocDesarrolloCloudNative.onmicrosoft.com/oauth2/v2.0/authorize?p=B2C_1_DuocDesarrolloCloudNative_Login&client_id=ad16d15c-7d6e-4f58-8146-4b5b3d7b7124&nonce=defaultNonce&redirect_uri=http%3A%2F%2Flocalhost%3A4200&scope=openid&response_type=id_token&prompt=login';

  estadisticas: EstadisticasReservas | null = null;
  estadisticasEventos: EstadisticasEventos | null = null;
  estadisticasUsuarios: EstadisticasUsuarios | null = null;
  cargando = false;
  cargandoEventos = false;
  cargandoUsuarios = false;
  error = '';
  errorEventos = '';
  errorUsuarios = '';
  userRole: string = '';
  userName: string = '';

  constructor(private router: Router, private http: HttpClient, private endpointsService: EndpointsService) {}

  async ngOnInit(): Promise<void> {
    console.log('=== INICIO ngOnInit Reportes ===');
    this.obtenerUserRole();
    this.cargarEstadisticas();
    this.cargarEstadisticasEventos();
    this.cargarEstadisticasUsuarios();
    console.log('=== FIN ngOnInit Reportes ===');
  }

  obtenerUserRole(): void {
    try {
      const idToken = localStorage.getItem('idToken');
      if (idToken) {
        const tokenPayload = this.decodeJwtToken(idToken);
        this.userName = tokenPayload.name || tokenPayload.given_name || tokenPayload.preferred_username || 'Usuario';
        this.userRole = tokenPayload.extension_Roles || 'Usuario';
        console.log('Usuario:', this.userName, 'Rol:', this.userRole);
      }
    } catch (error) {
      console.error('Error al obtener información del usuario:', error);
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
      return {};
    }
  }

  async cargarEstadisticas(): Promise<void> {
    console.log('=== INICIO cargarEstadisticas ===');
    this.cargando = true;
    this.error = '';

    try {
      // Obtener token de autorización
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });

      console.log('Intentando cargar estadísticas desde:', ApiEndpoints.RESERVA.ESTADISTICAS);
      console.log('Headers:', headers);

      // Primero probar si el servidor está disponible
      try {
        const response = await this.http.get<EstadisticasReservas>(ApiEndpoints.RESERVA.ESTADISTICAS, { headers }).toPromise();
        
        console.log('Respuesta del servidor:', response);
        
        if (response) {
          this.estadisticas = response;
          console.log('Estadísticas cargadas:', this.estadisticas);
        } else {
          throw new Error('No se recibió respuesta del servidor');
        }
      } catch (httpError: any) {
        console.error('Error en primer intento:', httpError);
        
        // Si falla con autorización, intentar sin headers
        if (httpError.status === 401 || httpError.status === 403) {
          console.log('Probando sin headers de autorización...');
          
          try {
            const responseWithoutAuth = await this.http.get<EstadisticasReservas>(ApiEndpoints.RESERVA.ESTADISTICAS).toPromise();
            
            if (responseWithoutAuth) {
              this.estadisticas = responseWithoutAuth;
              console.log('Estadísticas cargadas sin autorización:', this.estadisticas);
              return; // Salir si funcionó sin autorización
            }
          } catch (secondError) {
            console.error('Error en segundo intento:', secondError);
          }
        }
        
        throw httpError; // Re-lanzar el error original si no funcionó
      }
    } catch (error: any) {
      console.error('Error detallado al cargar estadísticas:', error);
      
      // Determinar el tipo de error
      let mensajeError = 'Error al cargar las estadísticas. Por favor, inténtalo de nuevo.';
      
      if (error.status === 0) {
        mensajeError = 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose en ' + ApiEndpoints.getBaseUrl();
      } else if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'Acceso denegado. No tienes permisos para ver estas estadísticas.';
      } else if (error.status === 404) {
        mensajeError = 'El endpoint no fue encontrado. Verifica la URL del servidor.';
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.message) {
        mensajeError = `Error: ${error.message}`;
      }
      
      this.error = mensajeError;
    } finally {
      this.cargando = false;
    }
  }

  verificarAutenticacion(): boolean {
    const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
    if (!token) {
      return false;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      
      // Si el token tiene campo de expiración, verificarlo
      if (payload.exp) {
        const exp = payload.exp * 1000;
        const ahora = Date.now();
        
        if (ahora >= exp) {
          return false;
        }
      }
      
      return true;
    } catch (error) {
      console.error('Error al verificar token:', error);
      return false;
    }
  }

  limpiarSesion(): void {
    console.log('Limpiando sesión...');
    
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
    
    console.log('Sesión limpiada completamente');
  }

  irAlLogin(): void {
    console.log('Redirigiendo al login...');
    this.limpiarSesion();
    window.location.href = this.AZURE_B2C_LOGIN_URL;
  }

  formatearMoneda(valor: number): string {
    return new Intl.NumberFormat('es-CL', {
      style: 'currency',
      currency: 'CLP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(valor);
  }

  formatearPorcentaje(valor: number): string {
    return `${valor.toFixed(1)}%`;
  }

  formatearNumero(valor: number): string {
    return new Intl.NumberFormat('es-CL').format(valor);
  }

  async cargarEstadisticasEventos(): Promise<void> {
    console.log('=== INICIO cargarEstadisticasEventos ===');
    this.cargandoEventos = true;
    this.errorEventos = '';

    try {
      // Obtener token de autorización
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });

      console.log('Intentando cargar estadísticas de eventos desde:', ApiEndpoints.EVENTO.MIS_ESTADISTICAS);
      console.log('Headers:', headers);

      // Primero probar si el servidor está disponible
      try {
        const response = await this.http.get<EstadisticasEventos>(ApiEndpoints.EVENTO.MIS_ESTADISTICAS, { headers }).toPromise();
        
        console.log('Respuesta del servidor eventos:', response);
        
        if (response) {
          this.estadisticasEventos = response;
          console.log('Estadísticas de eventos cargadas:', this.estadisticasEventos);
        } else {
          throw new Error('No se recibió respuesta del servidor');
        }
      } catch (httpError: any) {
        console.error('Error en primer intento eventos:', httpError);
        
        // Si falla con autorización, intentar sin headers
        if (httpError.status === 401 || httpError.status === 403) {
          console.log('Probando sin headers de autorización para eventos...');
          
          try {
            const responseWithoutAuth = await this.http.get<EstadisticasEventos>(ApiEndpoints.EVENTO.MIS_ESTADISTICAS).toPromise();
            
            if (responseWithoutAuth) {
              this.estadisticasEventos = responseWithoutAuth;
              console.log('Estadísticas de eventos cargadas sin autorización:', this.estadisticasEventos);
              return; // Salir si funcionó sin autorización
            }
          } catch (secondError) {
            console.error('Error en segundo intento eventos:', secondError);
          }
        }
        
        throw httpError; // Re-lanzar el error original si no funcionó
      }
    } catch (error: any) {
      console.error('Error detallado al cargar estadísticas de eventos:', error);
      
      // Determinar el tipo de error
      let mensajeError = 'Error al cargar las estadísticas de eventos. Por favor, inténtalo de nuevo.';
      
      if (error.status === 0) {
        mensajeError = 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose en ' + ApiEndpoints.getBaseUrl();
      } else if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'Acceso denegado. No tienes permisos para ver estas estadísticas.';
      } else if (error.status === 404) {
        mensajeError = 'El endpoint no fue encontrado. Verifica la URL del servidor.';
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.message) {
        mensajeError = `Error: ${error.message}`;
      }
      
      this.errorEventos = mensajeError;
    } finally {
      this.cargandoEventos = false;
    }
  }

  async cargarEstadisticasUsuarios(): Promise<void> {
    console.log('=== INICIO cargarEstadisticasUsuarios ===');
    this.cargandoUsuarios = true;
    this.errorUsuarios = '';

    try {
      // Obtener token de autorización
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });

      console.log('Intentando cargar estadísticas de usuarios desde:', ApiEndpoints.USUARIO.ESTADISTICAS);
      console.log('Headers:', headers);

      // Primero probar si el servidor está disponible
      try {
        const response = await this.http.get<EstadisticasUsuarios>(ApiEndpoints.USUARIO.ESTADISTICAS, { headers }).toPromise();
        
        console.log('Respuesta del servidor usuarios:', response);
        
        if (response) {
          this.estadisticasUsuarios = response;
          console.log('Estadísticas de usuarios cargadas:', this.estadisticasUsuarios);
        } else {
          throw new Error('No se recibió respuesta del servidor');
        }
      } catch (httpError: any) {
        console.error('Error en primer intento usuarios:', httpError);
        
        // Si falla con autorización, intentar sin headers
        if (httpError.status === 401 || httpError.status === 403) {
          console.log('Probando sin headers de autorización para usuarios...');
          
          try {
            const responseWithoutAuth = await this.http.get<EstadisticasUsuarios>(ApiEndpoints.USUARIO.ESTADISTICAS).toPromise();
            
            if (responseWithoutAuth) {
              this.estadisticasUsuarios = responseWithoutAuth;
              console.log('Estadísticas de usuarios cargadas sin autorización:', this.estadisticasUsuarios);
              return; // Salir si funcionó sin autorización
            }
          } catch (secondError) {
            console.error('Error en segundo intento usuarios:', secondError);
          }
        }
        
        throw httpError; // Re-lanzar el error original si no funcionó
      }
    } catch (error: any) {
      console.error('Error detallado al cargar estadísticas de usuarios:', error);
      
      // Determinar el tipo de error
      let mensajeError = 'Error al cargar las estadísticas de usuarios. Por favor, inténtalo de nuevo.';
      
      if (error.status === 0) {
        mensajeError = 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose en ' + ApiEndpoints.getBaseUrl();
      } else if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'Acceso denegado. No tienes permisos para ver estas estadísticas.';
      } else if (error.status === 404) {
        mensajeError = 'El endpoint no fue encontrado. Verifica la URL del servidor.';
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.message) {
        mensajeError = `Error: ${error.message}`;
      }
      
      this.errorUsuarios = mensajeError;
    } finally {
      this.cargandoUsuarios = false;
    }
  }
} 