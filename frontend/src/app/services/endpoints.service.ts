/**
 * Servicio centralizado para manejar todas las llamadas a la API
 * Proporciona métodos genéricos y específicos para interactuar con el backend
 * Incluye gestión automática de tokens de autenticación y headers
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiEndpoints } from '../config/api-endpoints';

/**
 * Interfaz para definir una petición a un endpoint
 */
export interface EndpointRequest {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';  // Método HTTP a usar
  path: string;                               // Ruta del endpoint
  params?: any;                               // Parámetros de consulta (para GET)
  body?: any;                                 // Cuerpo de la petición (para POST/PUT)
}

/**
 * Interfaz para la respuesta estándar de los endpoints
 */
export interface EndpointResponse {
  success: boolean;    // Indica si la operación fue exitosa
  data?: any;          // Datos de la respuesta
  error?: string;      // Mensaje de error si aplica
  timestamp: string;   // Timestamp de la respuesta
}

@Injectable({
  providedIn: 'root'
})
export class EndpointsService {
  private baseUrl = ApiEndpoints.getBaseUrl();

  constructor(private http: HttpClient) {}

  /**
   * Método genérico para llamar cualquier endpoint de la API
   * Maneja automáticamente la autenticación y los headers
   * @param request Configuración de la petición HTTP
   * @returns Observable con la respuesta del endpoint
   */
  callEndpoint(request: EndpointRequest): Observable<EndpointResponse> {
    // Obtener token de autenticación (JWT o ID token)
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No se encontró token de autenticación');
    }

    // Configurar headers con autenticación
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    let url = `${this.baseUrl}${request.path}`;
    let params = new HttpParams();

    // Agregar parámetros de consulta para peticiones GET
    if (request.method === 'GET' && request.params) {
      Object.keys(request.params).forEach(key => {
        if (request.params[key] !== null && request.params[key] !== undefined) {
          params = params.set(key, request.params[key]);
        }
      });
    }

    // Ejecutar la petición según el método HTTP
    switch (request.method) {
      case 'GET':
        return this.http.get<any>(url, { headers, params });
      
      case 'POST':
        return this.http.post<any>(url, request.body, { headers });
      
      case 'PUT':
        return this.http.put<any>(url, request.body, { headers });
      
      case 'DELETE':
        return this.http.delete<any>(url, { headers });
      
      default:
        throw new Error(`Método HTTP no soportado: ${request.method}`);
    }
  }

  /**
   * Obtiene los headers de autenticación para peticiones HTTP
   * @returns HttpHeaders con el token de autorización
   */
  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Construye una URL completa para un endpoint
   * @param path Ruta relativa del endpoint
   * @returns URL completa del endpoint
   */
  buildUrl(path: string): string {
    return ApiEndpoints.buildUrl(path);
  }

  // ===== ENDPOINTS DE RESERVAS =====
  
  /**
   * Obtiene la cantidad mínima de reservas requeridas
   */
  getCantidadMinima(): Observable<EndpointResponse> {
    return this.callEndpoint({
      method: 'GET',
      path: '/reserva/cantidad-minima'
    });
  }

  /**
   * Busca reservas según criterios específicos
   * @param criterios Criterios de búsqueda (fechas, estado, etc.)
   */
  buscarReservas(criterios: any): Observable<EndpointResponse> {
    return this.callEndpoint({
      method: 'GET',
      path: '/reserva/buscar',
      params: criterios
    });
  }

  /**
   * Obtiene estadísticas detalladas de reservas
   */
  getEstadisticas(): Observable<EndpointResponse> {
    return this.callEndpoint({
      method: 'GET',
      path: '/reserva/estadisticas'
    });
  }

  /**
   * Obtiene estadísticas básicas de reservas
   */
  getEstadisticasBasicas(): Observable<EndpointResponse> {
    return this.callEndpoint({
      method: 'GET',
      path: '/reserva/estadisticas-basicas'
    });
  }

  /**
   * Obtiene todas las reservas de un evento específico
   * @param eventoId ID del evento
   */
  getReservasPorEvento(eventoId: number): Observable<any> {
    return this.http.get<any>(ApiEndpoints.RESERVA.POR_EVENTO(eventoId), {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Cancela una reserva específica
   * @param reservaId ID de la reserva a cancelar
   */
  cancelarReserva(reservaId: number): Observable<any> {
    return this.http.put<any>(ApiEndpoints.RESERVA.CANCELAR(reservaId), {}, {
      headers: this.getAuthHeaders()
    });
  }

  // ===== ENDPOINTS DE USUARIOS =====
  
  /**
   * Obtiene información del usuario actualmente autenticado
   */
  getUsuarioActual(): Observable<any> {
    return this.http.get<any>(ApiEndpoints.USUARIO.CURRENT, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Obtiene todos los usuarios del sistema
   */
  getAllUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.USUARIO.ALL, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Obtiene información de productor asociada a un usuario
   * @param usuarioId ID del usuario
   */
  getProductorPorUsuario(usuarioId: string): Observable<any> {
    return this.http.get<any>(ApiEndpoints.USUARIO.PRODUCTOR(usuarioId), {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Crea un nuevo productor en el sistema
   * @param productorData Datos del productor a crear
   */
  crearProductor(productorData: any): Observable<any> {
    return this.http.post<any>(ApiEndpoints.USUARIO.CREAR_PRODUCTOR, productorData, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Actualiza información de un usuario existente
   * @param usuarioId ID del usuario a actualizar
   * @param userData Nuevos datos del usuario
   */
  actualizarUsuario(usuarioId: number, userData: any): Observable<any> {
    return this.http.put<any>(ApiEndpoints.USUARIO.ACTUALIZAR(usuarioId), userData, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Elimina un usuario del sistema
   * @param usuarioId ID del usuario a eliminar
   */
  eliminarUsuario(usuarioId: number): Observable<any> {
    return this.http.delete<any>(ApiEndpoints.USUARIO.ELIMINAR(usuarioId), {
      headers: this.getAuthHeaders()
    });
  }

  // ===== ENDPOINTS DE EVENTOS =====
  
  /**
   * Obtiene todos los eventos disponibles
   */
  getAllEventos(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.EVENTO.ALL);
  }

  /**
   * Obtiene eventos creados por un usuario específico
   * @param productorId ID del productor
   */
  getEventosPorUsuario(productorId: number): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.EVENTO.POR_USUARIO(productorId), {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Crea un nuevo evento
   * @param eventoData Datos del evento a crear
   */
  crearEvento(eventoData: any): Observable<any> {
    return this.http.post<any>(ApiEndpoints.EVENTO.CREAR, eventoData, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Actualiza un evento existente
   * @param eventoId ID del evento a actualizar
   * @param eventoData Nuevos datos del evento
   */
  actualizarEvento(eventoId: number, eventoData: any): Observable<any> {
    return this.http.put<any>(ApiEndpoints.EVENTO.ACTUALIZAR(eventoId), eventoData, {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Elimina un evento del sistema
   * @param eventoId ID del evento a eliminar
   */
  eliminarEvento(eventoId: number): Observable<any> {
    return this.http.delete<any>(ApiEndpoints.EVENTO.ELIMINAR(eventoId), {
      headers: this.getAuthHeaders()
    });
  }

  /**
   * Obtiene estadísticas de eventos del usuario actual
   */
  getEstadisticasEventos(): Observable<any> {
    return this.http.get<any>(ApiEndpoints.EVENTO.MIS_ESTADISTICAS, {
      headers: this.getAuthHeaders()
    });
  }

  // ===== ENDPOINTS DE UBICACIONES =====
  getAllUbicaciones(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.UBICACION.ALL);
  }

  // ===== ENDPOINTS DE ESTADÍSTICAS =====
  getEstadisticasUsuarios(): Observable<any> {
    return this.http.get<any>(ApiEndpoints.USUARIO.ESTADISTICAS, {
      headers: this.getAuthHeaders()
    });
  }
} 