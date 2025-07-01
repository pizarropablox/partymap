import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { UbicacionResponseDTO } from './ubicacion.service';
import { GeocodingService, GeocodingResult } from './geocoding.service';
import { HttpClient } from '@angular/common/http';
import { map, catchError } from 'rxjs/operators';

export interface MapLocation {
  id?: number;
  nombre: string;
  direccion: string;
  comuna: string;
  latitud: number;
  longitud: number;
  descripcion?: string;
  isGeocoded?: boolean; // Indica si fue obtenido por geocoding
}

export interface NavigationResult {
  success: boolean;
  distance?: string;
  duration?: string;
  route?: any[];
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MapNavigationService {
  private selectedLocationSubject = new BehaviorSubject<MapLocation | null>(null);
  public selectedLocation$ = this.selectedLocationSubject.asObservable();
  private http = inject(HttpClient);

  constructor(private geocodingService: GeocodingService) {}

  /**
   * Navega a una ubicación específica en el mapa
   */
  navigateToLocation(ubicacion: UbicacionResponseDTO | MapLocation) {
    this.selectedLocationSubject.next(ubicacion);
  }

  /**
   * Navega a una dirección o comuna usando geocoding
   */
  navigateToAddress(address: string): Observable<boolean> {
    return new Observable(observer => {
      this.geocodingService.geocodeAddress(address).subscribe({
        next: (result: GeocodingResult) => {
          if (result) {
            const geocodedLocation: MapLocation = {
              nombre: address,
              direccion: result.formattedAddress,
              comuna: this.extractComuna(result.formattedAddress),
              latitud: result.lat,
              longitud: result.lng,
              isGeocoded: true
            };
            
            this.navigateToLocation(geocodedLocation);
            observer.next(true);
          } else {
            observer.next(false);
          }
          observer.complete();
        },
        error: (error) => {
          observer.next(false);
          observer.complete();
        }
      });
    });
  }

  /**
   * Extrae la comuna de una dirección formateada
   */
  private extractComuna(formattedAddress: string): string {
    // Buscar patrones comunes de comunas en Chile
    const comunas = [
      'Providencia', 'Las Condes', 'Ñuñoa', 'Santiago', 'Maipú', 'Puente Alto',
      'La Florida', 'Peñalolén', 'La Reina', 'Macul', 'San Miguel', 'La Cisterna',
      'El Bosque', 'Pedro Aguirre Cerda', 'Lo Espejo', 'Estación Central',
      'Cerrillos', 'Quinta Normal', 'Renca', 'Conchalí', 'Independencia',
      'Recoleta', 'Huechuraba', 'Colina', 'Lampa', 'Tiltil', 'Pudahuel',
      'Lo Prado', 'Cerro Navia', 'Quilicura', 'Vitacura', 'Lo Barnechea'
    ];

    for (const comuna of comunas) {
      if (formattedAddress.toLowerCase().includes(comuna.toLowerCase())) {
        return comuna;
      }
    }

    // Si no encuentra una comuna específica, devolver la primera parte de la dirección
    const parts = formattedAddress.split(',');
    return parts[0] || 'Santiago';
  }

  /**
   * Limpia la ubicación seleccionada
   */
  clearSelectedLocation() {
    this.selectedLocationSubject.next(null);
  }

  /**
   * Obtiene la ubicación actualmente seleccionada
   */
  getSelectedLocation(): MapLocation | null {
    return this.selectedLocationSubject.value;
  }

  getDirections(origin: string, destination: string): Observable<NavigationResult> {
    const apiKey = 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg';
    const url = `https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}&key=${apiKey}`;

    return this.http.get(url).pipe(
      map((response: any) => {
        if (response.status === 'OK' && response.routes && response.routes.length > 0) {
          const route = response.routes[0];
          const leg = route.legs[0];
          
          return {
            success: true,
            distance: leg.distance.text,
            duration: leg.duration.text,
            route: route.overview_polyline.points
          };
        } else {
          return {
            success: false,
            error: 'No se encontró una ruta válida'
          };
        }
      }),
      catchError(error => {
        return of({
          success: false,
          error: 'Error al obtener direcciones'
        });
      })
    );
  }

  calculateDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371; // Radio de la Tierra en kilómetros
    const dLat = this.deg2rad(lat2 - lat1);
    const dLng = this.deg2rad(lng2 - lng1);
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) * 
      Math.sin(dLng/2) * Math.sin(dLng/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    const distance = R * c;
    return distance;
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI/180);
  }

  getEstimatedTime(distance: number, averageSpeed: number = 30): string {
    const timeInHours = distance / averageSpeed;
    const timeInMinutes = Math.round(timeInHours * 60);
    
    if (timeInMinutes < 60) {
      return `${timeInMinutes} minutos`;
    } else {
      const hours = Math.floor(timeInMinutes / 60);
      const minutes = timeInMinutes % 60;
      return `${hours}h ${minutes}min`;
    }
  }
} 