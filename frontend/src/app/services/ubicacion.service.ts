import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UbicacionResponseDTO {
  id: number;
  nombre: string;
  direccion: string;
  comuna: string;
  latitud: number;
  longitud: number;
  descripcion?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UbicacionService {
  private baseUrl = `${environment.apiUrl}/ubicacion`;

  constructor(private http: HttpClient) { }

  /**
   * Busca ubicaciones por comuna y/o dirección
   */
  buscarUbicaciones(comuna?: string, direccion?: string): Observable<UbicacionResponseDTO[]> {
    let params = new HttpParams();
    
    if (comuna) {
      params = params.set('comuna', comuna);
    }
    
    if (direccion) {
      params = params.set('direccion', direccion);
    }

    return this.http.get<UbicacionResponseDTO[]>(`${this.baseUrl}/buscar`, { params });
  }

  /**
   * Obtiene todas las ubicaciones
   */
  getAllUbicaciones(): Observable<UbicacionResponseDTO[]> {
    return this.http.get<UbicacionResponseDTO[]>(`${this.baseUrl}/buscar`);
  }
} 