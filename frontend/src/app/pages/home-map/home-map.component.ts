import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleMapsModule } from '@angular/google-maps';
import { HttpClient, HttpClientModule } from '@angular/common/http';

interface Evento {
  id: number;
  nombre: string;
  descripcion: string;
  fecha: string;
  ubicacion: {
    direccion: string;
    comuna: string;
    latitud: number;
    longitud: number;
  };
}

@Component({
  selector: 'app-home-map',
  standalone: true,
  imports: [CommonModule, GoogleMapsModule, HttpClientModule],
  templateUrl: './home-map.component.html',
  styleUrls: ['./home-map.component.css'],
})
export class HomeMapComponent implements OnInit {
  http = inject(HttpClient);

  // 📍 Centro fijo en Santiago
  center = { lat: -33.4489, lng: -70.6693 };

  // 🔍 Zoom fijo y reutilizable
  zoom = 12;

  // 📌 Marcadores personalizados desde backend
  markers: any[] = [];

  // 🎯 Evento seleccionado para mostrar detalles
  selectedEvento: Evento | null = null;

  // 🗺️ Configuración del mapa
  mapOptions: google.maps.MapOptions = {
    center: { lat: -33.4489, lng: -70.6693 },
    zoom: 12,
    styles: [
      {
        featureType: 'poi',
        stylers: [{ visibility: 'off' }]
      },
      {
        featureType: 'transit',
        stylers: [{ visibility: 'off' }]
      }
    ]
  };

  /*ngOnInit(): void {
    this.http.get<Evento[]>('http://18.235.227.189:8085/evento/disponibles').subscribe({
      next: (data) => {
        this.markers = data.map((evento) => ({
          position: {
            lat: evento.ubicacion.latitud,
            lng: evento.ubicacion.longitud
          },
          title: evento.nombre,
          evento: evento
        }));
      },
      error: (err) => {
        console.error('Error al cargar eventos:', err);
      }
    });
  }*/

  ngOnInit(): void {
  this.markers = [
    {
      position: { lat: -33.4372, lng: -70.6506 },
      title: 'Fiesta Drama',
      evento: {
        id: 1,
        nombre: 'FIESTA DRAMA',
        descripcion: 'Fiesta Privada de Reggaeton',
        fecha: '2025-07-10T22:00:00',
        ubicacion: {
          direccion: 'Calle Falsa 123',
          comuna: 'Santiago',
          latitud: -33.4372,
          longitud: -70.6506,
        }
      }
    }
  ];
}


  onMarkerClick(evento: Evento) {
    this.selectedEvento = evento;
  }

  cerrarInfo() {
    this.selectedEvento = null;
  }
}
