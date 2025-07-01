/**
 * Configuración de rutas de la aplicación Angular
 * Define las rutas disponibles y sus componentes asociados
 * Incluye protección de rutas con guards de autenticación y roles
 */
import { Routes } from '@angular/router';
import { HomeMapComponent } from './pages/home-map/home-map.component';
import { ContactComponent } from './pages/contact/contact';
import { AboutComponent } from './pages/about/about';
import { EventsComponent } from './pages/events/events.component';
import { ReportesComponent } from './pages/reportes/reportes.component';
import { ProductorComponent } from './pages/productor/productor.component';
import { ReservasComponent } from './pages/reservas/reservas.component';
import { AuthGuard } from './auth.guard';
import { RoleGuard } from './role.guard';

export const routes: Routes = [
  // Rutas públicas (accesibles sin autenticación)
  { path: '', component: HomeMapComponent },           // Página principal con mapa
  { path: 'home', component: HomeMapComponent },       // Página principal (alias)
  { path: 'about', component: AboutComponent },        // Página de información
  { path: 'contact', component: ContactComponent },    // Página de contacto
  
  // Rutas protegidas que requieren autenticación y roles específicos
  { 
    path: 'events', 
    component: EventsComponent, 
    canActivate: [AuthGuard, RoleGuard],               // Requiere login y roles específicos
    data: { roles: ['administrador', 'productor'] }    // Solo administradores y productores
  },
  { 
    path: 'reservas', 
    component: ReservasComponent, 
    canActivate: [AuthGuard, RoleGuard],               // Requiere login y roles específicos
    data: { roles: ['cliente', 'administrador'] }      // Solo clientes y administradores
  },
  { 
    path: 'reportes', 
    component: ReportesComponent, 
    canActivate: [AuthGuard, RoleGuard],               // Requiere login y roles específicos
    data: { roles: ['administrador'] }                 // Solo administradores
  },
  { 
    path: 'usuario', 
    component: ProductorComponent, 
    canActivate: [AuthGuard, RoleGuard],               // Requiere login y roles específicos
    data: { roles: ['administrador'] }                 // Solo administradores
  },
  
  // Ruta catch-all: redirige cualquier ruta no encontrada a la página principal
  { path: '**', redirectTo: '/' }
];
