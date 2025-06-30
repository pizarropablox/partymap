import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './about.html',
  styleUrl: './about.css'
})
export class AboutComponent {
  // Información sobre la aplicación
  appInfo = {
    name: 'PartyMap',
    version: '1.0.0',
    description: 'Plataforma integral para la gestión y descubrimiento de eventos sociales y fiestas',
    features: [
      'Descubrimiento de eventos en tiempo real',
      'Sistema de reservas integrado',
      'Gestión de productores de eventos',
      'Mapas interactivos con ubicaciones',
      'Reportes y análisis detallados',
      'Interfaz moderna y responsive'
    ],
    technologies: [
      'Angular 17',
      'TypeScript',
      'Google Maps API',
      'Azure AD B2C',
      'Material Design',
      'Responsive Design'
    ]
  };
} 