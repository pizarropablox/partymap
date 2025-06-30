import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Evento {
  id?: number;
  nombre: string;
  descripcion: string;
  fecha: string;
  ubicacion: {
    direccion: string;
    comuna: string;
    latitud: number;
    longitud: number;
  };
  precioEntrada: number;
  capacidadMaxima: number;
  cuposDisponibles: number;
  disponible: boolean;
  activo: number;
  productorId: number;
}

export interface EventoConUbicacion {
  evento: {
    nombre: string;
    descripcion: string;
    fecha: string;
    capacidadMaxima: number;
    precioEntrada: number;
    imagenPath?: string;
    usuarioId: number;
    cuposDisponibles?: number;
    disponible?: boolean;
    activo?: number;
  };
  ubicacion: {
    direccion: string;
    comuna: string;
    latitud: number;
    longitud: number;
    activo?: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class EventoService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  constructor() {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'Error desconocido';
    
    if (error.error && typeof error.error === 'string') {
      errorMessage = error.error;
    } else if (error.error && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    throw new Error(errorMessage);
  }

  obtenerEventos(): Observable<Evento[]> {
    const headers = this.getHeaders();
    return this.http.get<Evento[]>(`${this.apiUrl}/evento/all`, { headers });
  }

  crearEvento(evento: Evento): Observable<Evento> {
    const headers = this.getHeaders();
    return this.http.post<Evento>(`${this.apiUrl}/evento/crear`, evento, { headers });
  }

  crearEventoConUbicacion(eventoConUbicacion: EventoConUbicacion): Observable<Evento> {
    const headers = this.getHeaders();
    return this.http.post<Evento>(`${this.apiUrl}/evento/con-ubicacion`, eventoConUbicacion, { headers });
  }

  obtenerEvento(id: number): Observable<Evento> {
    const headers = this.getHeaders();
    return this.http.get<Evento>(`${this.apiUrl}/evento/${id}`, { headers });
  }

  actualizarEvento(id: number, evento: Evento): Observable<Evento> {
    const headers = this.getHeaders();
    return this.http.put<Evento>(`${this.apiUrl}/evento/${id}`, evento, { headers });
  }

  actualizarEventoConUbicacion(id: number, eventoConUbicacion: EventoConUbicacion): Observable<Evento> {
    const headers = this.getHeaders();
    return this.http.put<Evento>(`${this.apiUrl}/evento/${id}/con-ubicacion`, eventoConUbicacion, { headers });
  }

  eliminarEvento(id: number): Observable<void> {
    const headers = this.getHeaders();
    return this.http.delete<void>(`${this.apiUrl}/evento/eliminar/${id}`, { headers });
  }

  obtenerEventosPorProductor(productorId: number): Observable<Evento[]> {
    const headers = this.getHeaders();
    return this.http.get<Evento[]>(`${this.apiUrl}/evento/productor/${productorId}`, { headers });
  }
} 