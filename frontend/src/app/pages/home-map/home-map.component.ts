import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleMapsModule } from '@angular/google-maps';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MapNavigationService, MapLocation } from '../../services/map-navigation.service';
import { UbicacionResponseDTO } from '../../services/ubicacion.service';
import { GoogleMapsLoaderService } from '../../services/google-maps-loader.service';
import { ReservaService, ReservaRequest } from '../../services/reserva.service';
import { EventNotificationService } from '../../services/event-notification.service';
import { Subscription } from 'rxjs';

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

interface UbicacionEvento {
  id: number;
  direccion: string;
  comuna: string;
  latitud: number;
  longitud: number;
  activo: number;
  fechaCreacion: string;
}

@Component({
  selector: 'app-home-map',
  standalone: true,
  imports: [CommonModule, GoogleMapsModule, HttpClientModule, FormsModule],
  templateUrl: './home-map.component.html',
  styleUrls: ['./home-map.component.css'],
})
export class HomeMapComponent implements OnInit, OnDestroy {
  http = inject(HttpClient);

  // 📍 Centro fijo en Santiago
  center = { lat: -33.4489, lng: -70.6693 };

  // 🔍 Zoom fijo y reutilizable
  zoom = 12;

  // 📌 Marcadores personalizados desde backend
  markers: any[] = [];

  // 🎯 Evento seleccionado para mostrar detalles
  selectedEvento: Evento | null = null;

  // 🗺️ Estado de carga del mapa
  mapLoaded = false;

  // 🔐 Propiedades para reserva
  puedeHacerReserva = false;
  isSubmittingReserva = false;
  usuarioInfo: any = null;
  reservaData: ReservaRequest = {
    cantidad: 2,
    comentarios: '',
    usuarioId: 0,
    eventoId: 0
  };

  // 🎯 Propiedades para modal de confirmación
  mostrarModalConfirmacion = false;
  reservaExistente: any = null;

  // 🗺️ Configuración del mapa
  mapOptions: google.maps.MapOptions = {
    center: { lat: -33.4489, lng: -70.6693 },
    zoom: 12,
    zoomControl: true,
    scrollwheel: true,
    disableDoubleClickZoom: false,
    draggable: true,
    streetViewControl: false,
    fullscreenControl: false
  };

  // 🔍 Suscripción para navegación del mapa
  private navigationSubscription?: Subscription;
  private eventNotificationSubscription?: Subscription;

  constructor(
    private mapNavigationService: MapNavigationService,
    private googleMapsLoader: GoogleMapsLoaderService,
    private reservaService: ReservaService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private eventNotificationService: EventNotificationService
  ) {}

  async ngOnInit(): Promise<void> {
    console.log('🚀 Inicializando componente del mapa...');
    
    // Agregar clase al body para prevenir scrollbars
    document.body.classList.add('map-page');
    
    // Asegurar que Google Maps esté cargado antes de inicializar el mapa
    try {
      console.log('🗺️ Cargando Google Maps...');
      await this.googleMapsLoader.load();
      
      // Verificar que Google Maps esté disponible
      if (!this.googleMapsLoader.isLoaded()) {
        throw new Error('Google Maps no se cargó correctamente');
      }
      
      console.log('✅ Google Maps cargado correctamente');
      
      // Configurar opciones del mapa
      this.configureMapOptions();
      
      // Marcar el mapa como cargado con un pequeño delay
      setTimeout(() => {
        this.mapLoaded = true;
        this.cdr.detectChanges();
        console.log('🗺️ Mapa marcado como cargado');
      }, 100);
      
      // Suscribirse a cambios de ubicación seleccionada
      this.navigationSubscription = this.mapNavigationService.selectedLocation$.subscribe(
        (ubicacion) => {
          if (ubicacion) {
            console.log('📍 Navegando a ubicación seleccionada:', ubicacion);
            this.navigateToLocation(ubicacion);
          }
        }
      );
      
      // Suscribirse a notificaciones de nuevos eventos
      this.eventNotificationSubscription = this.eventNotificationService.eventCreated$.subscribe(() => {
        console.log('📢 Recibida notificación de nuevo evento, recargando eventos...');
        this.loadEventLocations();
      });
      
      // Cargar ubicaciones de eventos desde el backend
      console.log('🔄 Cargando eventos iniciales...');
      this.loadEventLocations();
      
    } catch (error) {
      console.error('❌ Error al inicializar el mapa:', error);
      setTimeout(() => {
        this.retryMapLoad();
      }, 2000);
    }
  }

  /**
   * Reintenta cargar el mapa si falló la primera vez
   */
  private async retryMapLoad() {
    console.log('🔄 Reintentando carga del mapa...');
    
    try {
      console.log('🗺️ Reintentando carga de Google Maps...');
      await this.googleMapsLoader.load();
      
      // Verificar que Google Maps esté disponible
      if (!this.googleMapsLoader.isLoaded()) {
        throw new Error('Google Maps no se cargó correctamente en el reintento');
      }
      
      console.log('✅ Google Maps cargado correctamente en el reintento');
      
      // Configurar opciones del mapa
      this.configureMapOptions();
      
      // Marcar el mapa como cargado con un pequeño delay
      setTimeout(() => {
        this.mapLoaded = true;
        this.cdr.detectChanges();
        console.log('🗺️ Mapa marcado como cargado (reintento)');
      }, 100);
      
      // Suscribirse a cambios de ubicación seleccionada
      this.navigationSubscription = this.mapNavigationService.selectedLocation$.subscribe(
        (ubicacion) => {
          if (ubicacion) {
            console.log('📍 Navegando a ubicación seleccionada (reintento):', ubicacion);
            this.navigateToLocation(ubicacion);
          }
        }
      );
      
      // Suscribirse a notificaciones de nuevos eventos
      this.eventNotificationSubscription = this.eventNotificationService.eventCreated$.subscribe(() => {
        console.log('📢 Recibida notificación de nuevo evento, recargando eventos (reintento)...');
        this.loadEventLocations();
      });
      
      // Cargar ubicaciones de eventos desde el backend
      console.log('🔄 Cargando eventos después del reintento...');
      this.loadEventLocations();
      
    } catch (error) {
      console.error('❌ Error en el reintento de carga del mapa:', error);
      // Mostrar mensaje de error al usuario
      this.showErrorMessage();
    }
  }

  /**
   * Muestra un mensaje de error si no se puede cargar el mapa
   */
  private showErrorMessage() {
    // Crear un elemento de error temporal
    const errorDiv = document.createElement('div');
    errorDiv.innerHTML = `
      <div style="
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        padding: 2rem;
        border-radius: 10px;
        box-shadow: 0 0 20px rgba(0,0,0,0.3);
        text-align: center;
        z-index: 1001;
        max-width: 400px;
      ">
        <h3 style="color: #dc3545; margin-bottom: 1rem;">Error al cargar el mapa</h3>
        <p style="margin-bottom: 1rem;">No se pudo cargar Google Maps. Por favor, recarga la página.</p>
        <button onclick="location.reload()" style="
          background: #007bff;
          color: white;
          border: none;
          padding: 0.5rem 1rem;
          border-radius: 5px;
          cursor: pointer;
        ">Recargar página</button>
      </div>
    `;
    document.querySelector('.map-wrapper')?.appendChild(errorDiv);
  }

  /**
   * Reinicializa completamente el mapa
   */
  public reinitializeMap() {
    console.log('🔄 Reinicializando mapa completo...');
    
    // Limpiar marcadores actuales
    this.markers = [];
    
    // Forzar detección de cambios
    this.cdr.detectChanges();
    
    // Recargar eventos después de un breve delay
    setTimeout(() => {
      console.log('🔄 Recargando eventos después de reinicialización...');
      this.loadEventLocations();
    }, 500);
  }

  /**
   * Recarga los eventos en el mapa (método público)
   */
  public reloadEvents() {
    console.log('Recargando eventos manualmente...');
    this.loadEventLocations();
  }

  /**
   * Carga las ubicaciones de eventos desde el backend
   */
  loadEventLocations() {
    console.log('🔄 Cargando eventos desde el backend...');
    
    // Cargar eventos desde el endpoint de eventos
    this.http.get<any[]>('http://localhost:8085/evento/all').subscribe({
      next: (eventos) => {
        console.log('📦 Eventos recibidos del backend:', eventos);
        console.log('📊 Total de eventos:', eventos.length);
        
        if (eventos.length === 0) {
          console.log('⚠️ No hay eventos en el backend');
          this.markers = [];
          this.cdr.detectChanges();
          return;
        }
        
        // Mostrar información detallada de cada evento
        eventos.forEach((evento, index) => {
          console.log(`📍 Evento ${index + 1} (ID: ${evento.id}):`, {
            nombre: evento.nombre,
            activo: evento.activo,
            disponible: evento.disponible,
            cuposDisponibles: evento.cuposDisponibles,
            ubicacion: evento.ubicacion,
            tieneUbicacion: !!(evento.ubicacion && evento.ubicacion.latitud && evento.ubicacion.longitud),
            latitud: evento.ubicacion?.latitud,
            longitud: evento.ubicacion?.longitud,
            coordenadasExactas: {
              lat: evento.ubicacion?.latitud,
              lng: evento.ubicacion?.longitud,
              tipoLat: typeof evento.ubicacion?.latitud,
              tipoLng: typeof evento.ubicacion?.longitud
            }
          });
        });
        
        // Filtrar eventos - ser más permisivo
        const eventosDisponibles = eventos.filter((evento: any) => {
          const tieneUbicacion = evento.ubicacion && 
                                evento.ubicacion.latitud && 
                                evento.ubicacion.longitud &&
                                evento.ubicacion.latitud !== 0 && 
                                evento.ubicacion.longitud !== 0;
          
          const esActivo = evento.activo === 1 || evento.activo === true;
          
          console.log(`🔍 Filtro para evento ${evento.id}:`, {
            nombre: evento.nombre,
            tieneUbicacion,
            esActivo,
            pasaFiltro: tieneUbicacion && esActivo
          });
          
          // Solo requerir que tenga ubicación válida y sea activo
          return tieneUbicacion && esActivo;
        });
        
        console.log('✅ Eventos que pasan el filtro:', eventosDisponibles.length);
        console.log('📍 Eventos filtrados:', eventosDisponibles);
        
        // Crear marcadores para los eventos disponibles
        this.markers = eventosDisponibles.map((evento: any) => {
          const marker = {
            position: {
              lat: evento.ubicacion.latitud,
              lng: evento.ubicacion.longitud,
            },
            title: evento.nombre,
            options: {
              icon: {
                url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
                scaledSize: new google.maps.Size(32, 32),
                anchor: new google.maps.Point(16, 32)
              }
            },
            evento: evento, // Guardar el evento completo
            info: {
              direccion: evento.ubicacion?.direccion || 'Dirección no disponible',
              comuna: evento.ubicacion?.comuna || 'Comuna no disponible',
              fechaCreacion: evento.fechaCreacion,
              nombre: evento.nombre,
              descripcion: evento.descripcion,
              fecha: evento.fecha,
              cuposDisponibles: evento.cuposDisponibles,
              precioEntrada: evento.precioEntrada
            }
          };
          
          console.log(`📍 Marcador creado para evento ${evento.id}:`, {
            nombre: evento.nombre,
            posicion: marker.position,
            titulo: marker.title,
            coordenadasExactas: {
              lat: evento.ubicacion.latitud,
              lng: evento.ubicacion.longitud,
              tipoLat: typeof evento.ubicacion.latitud,
              tipoLng: typeof evento.ubicacion.longitud
            }
          });
          
          return marker;
        });
        
        console.log('🎯 Marcadores finales creados:', this.markers.length);
        console.log('📍 Marcadores:', this.markers);
        
        // Si hay eventos, centrar el mapa en el primer evento
        if (eventosDisponibles.length > 0) {
          const primerEvento = eventosDisponibles[0];
          const nuevaLat = primerEvento.ubicacion.latitud;
          const nuevaLng = primerEvento.ubicacion.longitud;
          
          console.log('🎯 Centrando mapa en el evento:', {
            nombre: primerEvento.nombre,
            lat: nuevaLat,
            lng: nuevaLng
          });
          
          // Actualizar el centro del mapa
          this.center = { lat: nuevaLat, lng: nuevaLng };
          
          // Forzar la actualización del mapa
          setTimeout(() => {
            this.mapOptions = {
              ...this.mapOptions,
              center: this.center,
              zoom: 14 // Zoom más cercano para ver mejor el evento
            };
            this.cdr.detectChanges();
            console.log('✅ Mapa centrado en el evento');
          }, 100);
        }
        
        // Agregar un marcador de prueba temporal para verificar que el mapa funciona
        if (this.markers.length === 0) {
          console.log('⚠️ No hay marcadores de eventos, agregando marcador de prueba...');
          const marcadorPrueba = {
            position: {
              lat: -33.4489,
              lng: -70.6693,
            },
            title: 'Marcador de Prueba - Centro de Santiago',
            options: {
              icon: {
                url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                scaledSize: new google.maps.Size(32, 32),
                anchor: new google.maps.Point(16, 32)
              }
            },
            evento: null,
            info: {
              direccion: 'Centro de Santiago',
              comuna: 'Santiago',
              nombre: 'Marcador de Prueba',
              descripcion: 'Este es un marcador de prueba para verificar que el mapa funciona'
            }
          };
          this.markers = [marcadorPrueba];
          console.log('✅ Marcador de prueba agregado');
        }
        
        // Forzar la detección de cambios
        this.cdr.detectChanges();
        
        // Verificar si los marcadores se aplicaron correctamente
        setTimeout(() => {
          console.log('🔍 Verificación post-render - Marcadores en el DOM:', this.markers.length);
        }, 100);
      },
      error: (err) => {
        console.error('❌ Error al cargar eventos:', err);
        // Mostrar mensaje de error al usuario
        this.mostrarMensaje('No se pudieron cargar los eventos. Por favor, intenta de nuevo más tarde.', 'error');
        // Dejar el array de marcadores vacío
        this.markers = [];
        this.cdr.detectChanges();
      },
    });
  }

  ngOnDestroy(): void {
    // Remover clase del body cuando el componente se destruye
    document.body.classList.remove('map-page');
    
    // Limpiar suscripciones
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
    if (this.eventNotificationSubscription) {
      this.eventNotificationSubscription.unsubscribe();
    }
  }

  /**
   * Navega a una ubicación específica en el mapa
   */
  navigateToLocation(ubicacion: MapLocation) {
    // Actualizar el centro del mapa
    this.center = {
      lat: ubicacion.latitud,
      lng: ubicacion.longitud
    };
    // Actualizar las opciones del mapa
    this.mapOptions = {
      ...this.mapOptions,
      center: this.center,
      zoom: ubicacion.isGeocoded ? 16 : 15 // Zoom más cercano para ubicaciones geocodificadas
    };
    // Determinar el icono según el tipo de ubicación
    let iconUrl: string;
    if (ubicacion.isGeocoded) {
      // Icono verde para ubicaciones geocodificadas (no del endpoint)
      iconUrl = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 10C21 17 12 23 12 23S3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" fill="#28a745" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="12" cy="10" r="3" fill="#ffffff"/>
        </svg>
      `);
    } else {
      // Icono azul para ubicaciones del endpoint
      iconUrl = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 10C21 17 12 23 12 23S3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" fill="#007bff" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="12" cy="10" r="3" fill="#ffffff"/>
        </svg>
      `);
    }
    // Agregar un marcador temporal para la ubicación seleccionada
    const selectedMarker = {
      position: {
        lat: ubicacion.latitud,
        lng: ubicacion.longitud
      },
      title: ubicacion.nombre,
      options: {
        icon: {
          url: iconUrl,
          scaledSize: new google.maps.Size(32, 32),
          anchor: new google.maps.Point(16, 32)
        }
      },
      selectedLocation: true,
      isGeocoded: ubicacion.isGeocoded
    };
    // Agregar el marcador seleccionado al inicio del array
    this.markers = [selectedMarker, ...this.markers];
    // Limpiar la ubicación seleccionada después de un tiempo
    setTimeout(() => {
      this.mapNavigationService.clearSelectedLocation();
      // Remover el marcador temporal
      this.markers = this.markers.filter(marker => !marker.selectedLocation);
    }, 8000); // 8 segundos para ubicaciones geocodificadas
  }

  onMarkerClick(marker: any) {
    if (marker.info && marker.evento) {
      this.selectedEvento = {
        id: marker.evento.id,
        nombre: marker.info.nombre || `Evento en ${marker.info.comuna}`,
        descripcion: marker.info.descripcion || `Evento ubicado en ${marker.info.direccion}, ${marker.info.comuna}`,
        fecha: marker.info.fecha || marker.info.fechaCreacion,
        ubicacion: {
          direccion: marker.info.direccion,
          comuna: marker.info.comuna,
          latitud: marker.position.lat,
          longitud: marker.position.lng
        }
      };
      this.reservaData.eventoId = this.selectedEvento.id;
      this.verificarPermisosReserva();
    }
  }

  cerrarInfo() {
    this.selectedEvento = null;
  }

  /**
   * Verifica si el usuario actual puede hacer reservas y obtiene su información
   */
  verificarPermisosReserva() {
    // Obtener información del usuario si está autenticado
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    if (token) {
      this.reservaService.obtenerUsuarioActual().subscribe({
        next: (userInfo) => {
          this.usuarioInfo = userInfo;
          
          // Verificar si el usuario puede hacer reservas basado en la información del backend
          this.puedeHacerReserva = userInfo?.tipoUsuario?.toLowerCase() === 'cliente';
          
          // Mostrar información de debugging
          const userType = this.getCurrentUserType();
          console.log(`Tipo de usuario actual: ${userType}`);
          console.log(`Usuario desde backend:`, userInfo);
          console.log(`¿Puede hacer reservas? ${this.puedeHacerReserva}`);
          
          // Mostrar mensaje informativo según el tipo de usuario
          if (userInfo.tipoUsuario) {
            const tipoUsuario = userInfo.tipoUsuario.toLowerCase();
            if (tipoUsuario === 'productor') {
              console.log('Productor: Puede ver eventos pero no hacer reservas');
            } else if (tipoUsuario === 'administrador') {
              console.log('Administrador: Puede ver eventos pero no hacer reservas');
            } else if (tipoUsuario === 'cliente') {
              console.log('Cliente: Puede ver eventos y hacer reservas');
            }
          }
        },
        error: (error) => {
          console.log('Usuario no autenticado o error al obtener información:', error);
          this.usuarioInfo = null;
          this.puedeHacerReserva = false;
        }
      });
    } else {
      console.log('No hay token de autenticación');
      this.usuarioInfo = null;
      this.puedeHacerReserva = false;
    }
  }

  /**
   * Verifica si una reserva está activa (no cancelada)
   */
  private esReservaActiva(reserva: any): boolean {
    return reserva && reserva.estado !== 'CANCELADA' && reserva.estado !== 'cancelada';
  }

  /**
   * Verifica si el usuario ya tiene una reserva activa para este evento
   */
  async verificarReservaExistente(): Promise<{ existe: boolean; reserva?: any }> {
    if (!this.selectedEvento) {
      return { existe: false };
    }

    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    if (!token) {
      return { existe: false };
    }

    try {
      const response = await fetch(`http://localhost:8085/reserva/evento/${this.selectedEvento.id}/usuario`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json'
        }
      });

      if (response.ok) {
        const reserva = await response.json();
        if (this.esReservaActiva(reserva)) {
          return { existe: true, reserva };
        }
      }
      return { existe: false };
    } catch (error) {
      console.error('Error al verificar reserva existente:', error);
      return { existe: false };
    }
  }

  /**
   * Crea una nueva reserva para el evento seleccionado
   */
  async crearReserva() {
    if (!this.selectedEvento) {
      this.mostrarMensaje('No hay evento seleccionado', 'error');
      return;
    }

    // Verificar si ya existe una reserva activa
    const { existe, reserva } = await this.verificarReservaExistente();
    
    if (existe && reserva) {
      this.reservaExistente = reserva;
      this.mostrarModalConfirmacion = true;
      return;
    }

    // Proceder con la creación de la reserva
    await this.procesarCreacionReserva();
  }

  /**
   * Redirige al usuario al login
   */
  irALogin() {
    // Cerrar el modal actual
    this.cerrarInfo();
    
    // Redirigir al login (esto activará el AuthGuard)
    this.router.navigate(['/events']);
  }

  private mostrarMensaje(mensaje: string, tipo: 'success' | 'error' | 'warning' = 'error') {
    // Crear un mensaje más informativo según el tipo
    let mensajeCompleto = mensaje;
    
    switch (tipo) {
      case 'success':
        mensajeCompleto = `✅ ${mensaje}`;
        break;
      case 'warning':
        mensajeCompleto = `⚠️ ${mensaje}`;
        break;
      case 'error':
        mensajeCompleto = `❌ ${mensaje}`;
        break;
    }

    // Solo log en consola, sin alert
    console.log(`📢 Mensaje mostrado al usuario [${tipo}]:`, mensajeCompleto);
  }

  /**
   * Resetea manualmente el estado de carga (método de emergencia)
   */
  resetearEstadoCarga() {
    this.isSubmittingReserva = false;
    this.mostrarMensaje('Estado de carga reseteado manualmente', 'warning');
  }

  /**
   * Cancela una reserva existente
   */
  async cancelarReservaExistente(reservaId: number): Promise<boolean> {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    try {
      const response = await fetch(`http://localhost:8085/reserva/${reservaId}/cancelar`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json'
        }
      });
      
      if (response.ok) {
        return true;
      } else {
        return false;
      }
    } catch (error) {
      return false;
    }
  }

  /**
   * Navega a la página de reservas
   */
  irAReservas() {
    this.router.navigate(['/reservas']);
  }

  /**
   * Confirma la cancelación de la reserva existente y procede con la nueva
   */
  async confirmarCancelacionReserva() {
    if (!this.reservaExistente) {
      return;
    }

    // Ocultar modal
    this.mostrarModalConfirmacion = false;
    
    // Reactivar estado de carga
    this.isSubmittingReserva = true;
    
    try {
      // Cancelar la reserva anterior
      const cancelacionExitosa = await this.cancelarReservaExistente(this.reservaExistente.id);
      
      if (cancelacionExitosa) {
        this.mostrarMensaje('Reserva anterior cancelada exitosamente');
        
        // Proceder con la creación de la nueva reserva
        await this.procesarCreacionReserva();
      } else {
        this.isSubmittingReserva = false;
        this.mostrarMensaje('Error cancelando la reserva anterior', 'error');
      }
    } catch (error) {
      this.isSubmittingReserva = false;
      this.mostrarMensaje('Error en el proceso de cancelación', 'error');
    }
  }

  /**
   * Cancela la operación y cierra el modal
   */
  cancelarOperacion() {
    this.mostrarModalConfirmacion = false;
    this.reservaExistente = null;
    this.isSubmittingReserva = false;
    this.mostrarMensaje('Operación cancelada', 'warning');
  }

  /**
   * Procesa la creación de la reserva (método auxiliar)
   */
  private async procesarCreacionReserva() {
    // Obtener información del usuario actual
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    this.http.get<any>('http://localhost:8085/usuario/current', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).subscribe({
      next: (userInfo) => {
        // Actualizar datos de reserva con información del usuario
        this.reservaData.usuarioId = userInfo.id;
        this.reservaData.eventoId = this.selectedEvento!.id;
        
        // Crear la reserva
        this.reservaService.crearReserva(this.reservaData).subscribe({
          next: (response) => {
            this.isSubmittingReserva = false;
            
            // Mostrar mensaje de éxito
            this.mostrarMensaje('Reserva creada exitosamente');
            
            // Cerrar el modal
            this.cerrarInfo();
            
            // Resetear el formulario
            this.reservaData = {
              cantidad: 2,
              comentarios: '',
              usuarioId: 0,
              eventoId: 0
            };
          },
          error: (error) => {
            this.isSubmittingReserva = false;
            this.mostrarMensaje('Error al crear la nueva reserva', 'error');
      }
        });
      },
      error: (error) => {
        this.isSubmittingReserva = false;
        this.mostrarMensaje('Error al obtener información del usuario', 'error');
    }
    });
  }

  /**
   * Obtiene el tipo de usuario actual desde el token
   */
  getCurrentUserType(): string {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    if (!token) {
      return 'No autenticado';
    }

    try {
      const tokenData = this.decodeJwtToken(token);
      return tokenData?.tipoUsuario || tokenData?.role || 'Desconocido';
    } catch (error) {
      return 'Error al decodificar token';
    }
  }

  /**
   * Decodifica un token JWT
   */
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

  /**
   * Notifica al mapa que debe recargar los eventos
   */
  notificarMapaRecargar() {
    console.log('🔄 Recargando eventos en el mapa...');
    this.reloadEvents();
  }

  /**
   * Configura las opciones del mapa después de que Google Maps esté cargado
   */
  private configureMapOptions() {
    if (typeof google !== 'undefined' && google.maps) {
      this.mapOptions = {
        ...this.mapOptions,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      console.log('🗺️ Opciones del mapa configuradas correctamente');
    }
  }

  /**
   * Maneja la inicialización del mapa
   */
  onMapInitialized(map: google.maps.Map) {
    console.log('🗺️ Mapa inicializado correctamente:', map);
    
    // Verificar que el mapa esté funcionando
    if (map) {
      console.log('✅ Mapa cargado y funcionando');
      
      // Forzar un refresh del mapa después de un breve delay
      setTimeout(() => {
        google.maps.event.trigger(map, 'resize');
        
        // Centrar el mapa en las coordenadas por defecto
        map.setCenter({ lat: -33.4489, lng: -70.6693 });
        map.setZoom(12);
        
        console.log('🎯 Mapa centrado y configurado');
        
        // Forzar detección de cambios
        this.cdr.detectChanges();
      }, 500);
    }
  }
}
