import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiEndpoints } from '../config/api-endpoints';

export interface ReservaRequest {
  cantidad: number;
  comentarios?: string;
  usuarioId: number;
  eventoId: number;
}

export interface ReservaResponse {
  id: number;
  cantidad: number;
  fechaReserva: string;
  precioUnitario: number | null;
  precioTotal: number;
  comentarios: string | null;
  estado: string;
  activo: number;
  fechaCreacion: string;
  usuario: {
    id: number;
    nombre: string;
    email: string;
    tipoUsuario: string;
    activo: number;
    fechaCreacion: string;
  };
  evento: {
    id: number;
    nombre: string;
    descripcion: string;
    fecha: string;
    capacidadMaxima: number;
    precioEntrada: number;
    imagenUrl: string | null;
    activo: number;
    fechaCreacion: string;
    cuposDisponibles: number;
    disponible: boolean;
    eventoPasado: boolean;
    eventoProximo: boolean;
  };
}

export interface UsuarioInfo {
  id: number;
  nombre: string;
  email: string;
  tipoUsuario: string;
  activo: boolean;
  fechaCreacion?: string;
  extension_RUT?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservaService {
  private http = inject(HttpClient);

  constructor() {}

  puedeHacerReservas(): boolean {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    if (!token) {
      console.log('No hay token de autenticación');
      return false;
    }

    // Intentar obtener información del usuario desde el backend
    try {
      // Decodificar el token para obtener información del usuario
      const tokenData = this.decodeJwtToken(token);
      if (tokenData) {
        // Verificar diferentes campos posibles para el tipo de usuario
        const tipoUsuario = tokenData.tipoUsuario || tokenData.role || tokenData.extension_Roles || '';
        console.log('Tipo de usuario desde token:', tipoUsuario);
        
        if (tipoUsuario.toLowerCase() === 'cliente') {
          return true;
        }
      }
    } catch (error) {
      console.log('Error al decodificar token:', error);
    }

    // Si no se puede determinar desde el token, intentar obtener del backend
    // Nota: Este es un enfoque síncrono, pero el método es síncrono
    // En una implementación real, esto debería ser asíncrono
    return false;
  }

  /**
   * Método asíncrono para verificar si el usuario puede hacer reservas
   */
  puedeHacerReservasAsync(): Observable<boolean> {
    return new Observable(observer => {
      const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
      if (!token) {
        observer.next(false);
        observer.complete();
        return;
      }

      // Obtener información del usuario desde el backend
      this.obtenerUsuarioActual().subscribe({
        next: (usuario) => {
          const puedeReservar = usuario?.tipoUsuario?.toLowerCase() === 'cliente';
          console.log('Usuario desde backend:', usuario);
          console.log('¿Puede hacer reservas?', puedeReservar);
          observer.next(puedeReservar);
          observer.complete();
        },
        error: (error) => {
          console.error('Error al obtener usuario:', error);
          observer.next(false);
          observer.complete();
        }
      });
    });
  }

  obtenerUsuarioActual(): Observable<any> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get<any>(ApiEndpoints.USUARIO.CURRENT, { headers });
  }

  crearReserva(reserva: ReservaRequest): Observable<any> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const requestBody = JSON.stringify(reserva);

    return this.http.post<any>(ApiEndpoints.RESERVA.CREAR, requestBody, { headers });
  }

  obtenerReservas(): Observable<any[]> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get<any[]>(ApiEndpoints.RESERVA.ALL, { headers });
  }

  obtenerReservasUsuario(): Observable<any[]> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    // Endpoint específico para obtener reservas del usuario actual
    return this.http.get<any[]>(ApiEndpoints.RESERVA.USUARIO, { headers });
  }

  cancelarReserva(reservaId: number): Observable<any> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.put<any>(ApiEndpoints.RESERVA.CANCELAR(reservaId), {}, { headers });
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
      return null;
    }
  }
} 