import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, from } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { GoogleMapsLoaderService } from './google-maps-loader.service';

export interface GeocodingResult {
  lat: number;
  lng: number;
  direccion: string;
  comuna: string;
  formattedAddress: string;
}

@Injectable({
  providedIn: 'root'
})
export class GeocodingService {
  private http = inject(HttpClient);
  private googleMapsLoader = inject(GoogleMapsLoaderService);

  // Datos de fallback para comunas de Santiago
  private comunasFallback = [
    { nombre: 'Santiago', lat: -33.4489, lng: -70.6693 },
    { nombre: 'Providencia', lat: -33.4183, lng: -70.6062 },
    { nombre: 'Las Condes', lat: -33.4167, lng: -70.5833 },
    { nombre: 'Ñuñoa', lat: -33.4569, lng: -70.6483 },
    { nombre: 'Maipú', lat: -33.5167, lng: -70.7667 },
    { nombre: 'Puente Alto', lat: -33.6167, lng: -70.5833 },
    { nombre: 'La Florida', lat: -33.5333, lng: -70.5833 },
    { nombre: 'San Miguel', lat: -33.4833, lng: -70.6500 },
    { nombre: 'La Granja', lat: -33.5333, lng: -70.6333 },
    { nombre: 'La Pintana', lat: -33.5833, lng: -70.6333 },
    { nombre: 'San Ramón', lat: -33.4500, lng: -70.6500 },
    { nombre: 'Lo Espejo', lat: -33.5167, lng: -70.7000 },
    { nombre: 'Pedro Aguirre Cerda', lat: -33.4833, lng: -70.6833 },
    { nombre: 'San Joaquín', lat: -33.4667, lng: -70.6500 },
    { nombre: 'La Cisterna', lat: -33.5333, lng: -70.6667 },
    { nombre: 'El Bosque', lat: -33.5667, lng: -70.6833 },
    { nombre: 'Recoleta', lat: -33.4167, lng: -70.6333 },
    { nombre: 'Independencia', lat: -33.4167, lng: -70.6667 },
    { nombre: 'Conchalí', lat: -33.3833, lng: -70.6833 },
    { nombre: 'Huechuraba', lat: -33.3667, lng: -70.6500 },
    { nombre: 'Renca', lat: -33.4000, lng: -70.7167 },
    { nombre: 'Quilicura', lat: -33.3667, lng: -70.7333 },
    { nombre: 'Colina', lat: -33.2000, lng: -70.6833 },
    { nombre: 'Lampa', lat: -33.2833, lng: -70.8833 },
    { nombre: 'Tiltil', lat: -33.0833, lng: -70.9333 },
    { nombre: 'San Bernardo', lat: -33.6000, lng: -70.7167 },
    { nombre: 'Buin', lat: -33.7333, lng: -70.7500 },
    { nombre: 'Paine', lat: -33.8167, lng: -70.7500 },
    { nombre: 'Calera de Tango', lat: -33.6333, lng: -70.7833 },
    { nombre: 'Padre Hurtado', lat: -33.5667, lng: -70.8167 },
    { nombre: 'Pirque', lat: -33.6333, lng: -70.5500 },
    { nombre: 'Puente Alto', lat: -33.6167, lng: -70.5833 },
    { nombre: 'San José de Maipo', lat: -33.6333, lng: -70.3500 },
    { nombre: 'Cerrillos', lat: -33.5000, lng: -70.7167 },
    { nombre: 'Estación Central', lat: -33.4500, lng: -70.6833 },
    { nombre: 'Lo Prado', lat: -33.4500, lng: -70.7167 },
    { nombre: 'Quinta Normal', lat: -33.4333, lng: -70.6833 },
    { nombre: 'Cerro Navia', lat: -33.4167, lng: -70.7500 },
    { nombre: 'Lo Barnechea', lat: -33.3500, lng: -70.5167 },
    { nombre: 'Vitacura', lat: -33.3833, lng: -70.5667 },
    { nombre: 'Peñalolén', lat: -33.4833, lng: -70.5500 },
    { nombre: 'Macul', lat: -33.4667, lng: -70.6000 },
    { nombre: 'San Carlos', lat: -33.4000, lng: -70.5500 }
  ];

  constructor() {}

  geocodeAddress(address: string): Observable<GeocodingResult> {
    // Buscar en el fallback primero
    const comunaEncontrada = this.comunasFallback.find(comuna => 
      address.toLowerCase().includes(comuna.nombre.toLowerCase())
    );

    if (comunaEncontrada) {
      return of({
        lat: comunaEncontrada.lat,
        lng: comunaEncontrada.lng,
        direccion: address,
        comuna: comunaEncontrada.nombre,
        formattedAddress: `${address}, ${comunaEncontrada.nombre}, Chile`
      });
    }

    // Si no se encuentra en el fallback, usar Google Maps
    return from(this.googleMapsLoader.load().then(() => {
      if (typeof google === 'undefined' || !google.maps) {
        // Fallback a coordenadas de Santiago
        return {
          lat: -33.4489,
          lng: -70.6693,
          direccion: address,
          comuna: 'Santiago',
          formattedAddress: `${address}, Santiago, Chile`
        };
      }

      return new Promise<GeocodingResult>((resolve, reject) => {
        const geocoder = new google.maps.Geocoder();
        
        const geocodeRequest = {
          address: `${address}, Chile`,
          region: 'CL'
        };

        geocoder.geocode(geocodeRequest, (results, status) => {
          if (status === google.maps.GeocoderStatus.OK && results && results.length > 0) {
            const result = results[0];
            const location = result.geometry.location;
            
            // Extraer comuna de los componentes de dirección
            let comuna = 'Santiago';
            for (const component of result.address_components) {
              if (component.types.includes('sublocality_level_1') || 
                  component.types.includes('administrative_area_level_2')) {
                comuna = component.long_name;
                break;
              }
            }

            resolve({
              lat: location.lat(),
              lng: location.lng(),
              direccion: address,
              comuna: comuna,
              formattedAddress: result.formatted_address
            });
          } else {
            // Fallback a coordenadas de Santiago
            resolve({
              lat: -33.4489,
              lng: -70.6693,
              direccion: address,
              comuna: 'Santiago',
              formattedAddress: `${address}, Santiago, Chile`
            });
          }
        });
      });
    }));
  }

  // Método para probar el geocoding
  testGeocoding(): Observable<GeocodingResult> {
    const testAddress = 'Providencia 1234, Santiago';
    
    return this.geocodeAddress(testAddress).pipe(
      map(result => {
        return result;
      }),
      catchError(error => {
        return of({
          lat: -33.4489,
          lng: -70.6693,
          direccion: testAddress,
          comuna: 'Santiago',
          formattedAddress: `${testAddress}, Santiago, Chile`
        });
      })
    );
  }
} 