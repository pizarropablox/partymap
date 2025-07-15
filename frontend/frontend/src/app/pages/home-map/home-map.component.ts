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
  precioEntrada?: number;
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
    // Agregar clase al body para prevenir scrollbars
    document.body.classList.add('map-page');
    
    // Asegurar que Google Maps esté cargado antes de inicializar el mapa
    try {
      await this.googleMapsLoader.load();
      
      // Verificar que Google Maps esté disponible
      if (!this.googleMapsLoader.isLoaded()) {
        throw new Error('Google Maps no se cargó correctamente');
      }
      
      // Configurar opciones del mapa
      this.configureMapOptions();
      
      // Marcar el mapa como cargado con un pequeño delay
      setTimeout(() => {
        this.mapLoaded = true;
        this.cdr.detectChanges();
      }, 100);
      
      // Suscribirse a cambios de ubicación seleccionada
      this.navigationSubscription = this.mapNavigationService.selectedLocation$.subscribe(
        (ubicacion) => {
          if (ubicacion) {
            this.navigateToLocation(ubicacion);
          }
        }
      );
      
      // Suscribirse a notificaciones de nuevos eventos
      this.eventNotificationSubscription = this.eventNotificationService.eventCreated$.subscribe(() => {
        this.loadEventLocations();
      });
      
      // Cargar ubicaciones de eventos desde el backend
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
    try {
      await this.googleMapsLoader.load();
      
      // Verificar que Google Maps esté disponible
      if (!this.googleMapsLoader.isLoaded()) {
        throw new Error('Google Maps no se cargó correctamente en el reintento');
      }
      
      // Configurar opciones del mapa
      this.configureMapOptions();
      
      // Marcar el mapa como cargado con un pequeño delay
      setTimeout(() => {
        this.mapLoaded = true;
        this.cdr.detectChanges();
      }, 100);
      
      // Suscribirse a cambios de ubicación seleccionada
      this.navigationSubscription = this.mapNavigationService.selectedLocation$.subscribe(
        (ubicacion) => {
          if (ubicacion) {
            this.navigateToLocation(ubicacion);
          }
        }
      );
      
      // Suscribirse a notificaciones de nuevos eventos
      this.eventNotificationSubscription = this.eventNotificationService.eventCreated$.subscribe(() => {
        this.loadEventLocations();
      });
      
      // Cargar ubicaciones de eventos desde el backend
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
    // Limpiar marcadores actuales
    this.markers = [];
    
    // Forzar detección de cambios
    this.cdr.detectChanges();
    
    // Recargar eventos después de un breve delay
    setTimeout(() => {
      this.loadEventLocations();
    }, 500);
  }

  /**
   * Recarga los eventos en el mapa (método público)
   */
  public reloadEvents() {
    this.loadEventLocations();
  }

  /**
   * Carga las ubicaciones de eventos desde el backend
   */
  loadEventLocations() {
    // Cargar eventos desde el endpoint de eventos
    this.http.get<any[]>('http://localhost:8085/evento/all').subscribe({
      next: (eventos) => {
        if (eventos.length === 0) {
          this.markers = [];
          this.cdr.detectChanges();
          return;
        }
        
        // Mostrar información detallada de cada evento
        eventos.forEach((evento, index) => {
          // Procesar cada evento
        });
        
        // Filtrar eventos - ser más permisivo
        const eventosDisponibles = eventos.filter((evento: any) => {
          const tieneUbicacion = evento.ubicacion && 
                                evento.ubicacion.latitud && 
                                evento.ubicacion.longitud &&
                                evento.ubicacion.latitud !== 0 && 
                                evento.ubicacion.longitud !== 0;
          
          const esActivo = evento.activo === 1 || evento.activo === true;
          

          
          // Solo requerir que tenga ubicación válida y sea activo
          return tieneUbicacion && esActivo;
        });
        

        
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
          

          
          return marker;
        });
        

        
        // Si hay eventos, centrar el mapa en el primer evento
        if (eventosDisponibles.length > 0) {
          const primerEvento = eventosDisponibles[0];
          const nuevaLat = primerEvento.ubicacion.latitud;
          const nuevaLng = primerEvento.ubicacion.longitud;
          

          
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

          }, 100);
        }
        
        // Agregar un marcador de prueba temporal para verificar que el mapa funciona
        if (this.markers.length === 0) {
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

        }
        
        // Forzar la detección de cambios
        this.cdr.detectChanges();
        

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
        precioEntrada: marker.info.precioEntrada,
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
          
          
          // Mostrar mensaje informativo según el tipo de usuario
          if (userInfo.tipoUsuario) {
            const tipoUsuario = userInfo.tipoUsuario.toLowerCase();
            if (tipoUsuario === 'productor') {
              // Productor: Puede ver eventos pero no hacer reservas
            } else if (tipoUsuario === 'administrador') {
              // Administrador: Puede ver eventos pero no hacer reservas
            } else if (tipoUsuario === 'cliente') {
              // Cliente: Puede ver eventos y hacer reservas
            }
          }
        },
        error: (error) => {
          this.usuarioInfo = null;
          this.puedeHacerReserva = false;
        }
      });
    } else {
      this.usuarioInfo = null;
      this.puedeHacerReserva = false;
    }
  }

  /**
   * Verifica si una reserva está activa (no cancelada)
   */
  private esReservaActiva(reserva: any): boolean {
    if (!reserva) {
      return false;
    }
    
    const estado = reserva.estado?.toLowerCase() || '';
    
    // Estados que indican que la reserva NO está activa
    const estadosInactivos = ['cancelada', 'cancelado', 'cancelled', 'inactiva', 'inactivo'];
    
    const esActiva = !estadosInactivos.includes(estado);
    
    return esActiva;
  }



  /**
   * Verifica si el usuario ya tiene una reserva activa para este evento
   * NOTA: Este método usa el endpoint /reserva/usuario que SÍ existe en el backend
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
      return new Promise((resolve) => {
        // Primero obtener el usuario actual para obtener su ID
        this.http.get<any>('http://localhost:8085/usuario/current', {
          headers: { 'Authorization': `Bearer ${token}` }
        }).subscribe({
          next: (usuario) => {
            const usuarioId = usuario.id;
            
            // Ahora obtener las reservas del usuario específico
            this.http.get<any[]>(`http://localhost:8085/reserva/usuario/${usuarioId}`, {
              headers: { 'Authorization': `Bearer ${token}` }
            }).subscribe({
              next: (reservas) => {
                if (!reservas || reservas.length === 0) {
                  resolve({ existe: false });
                  return;
                }
                
                // Buscar reserva para el evento actual
                
                const reservaExistente = reservas.find(reserva => {
                  const eventoId = reserva.evento?.id;
                  const eventoSeleccionadoId = this.selectedEvento?.id;
                  const esMismoEvento = eventoId === eventoSeleccionadoId;
                  const esActiva = this.esReservaActiva(reserva);
                  

                  
                  return esMismoEvento && esActiva;
                });
                
                if (reservaExistente) {
                  resolve({ existe: true, reserva: reservaExistente });
                } else {
                  resolve({ existe: false });
                }
              },
              error: (error) => {
                resolve({ existe: false });
              }
            });
          },
          error: (error) => {
            resolve({ existe: false });
          }
        });
      });
    } catch (error) {
      console.error('❌ Error inesperado al verificar reserva existente:', error);
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
      
      // Forzar la detección de cambios para asegurar que el modal se muestre
      setTimeout(() => {
        this.cdr.detectChanges();
      }, 100);
      
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

  // Propiedades para el sistema de notificaciones
  mostrarNotificacion = false;
  mensajeNotificacion = '';
  tipoNotificacion: 'success' | 'error' | 'warning' = 'success';

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

    // Log en consola

    
    // Mostrar notificación automática
    this.mensajeNotificacion = mensajeCompleto;
    this.tipoNotificacion = tipo;
    this.mostrarNotificacion = true;
    
    // Ocultar automáticamente después de 3 segundos
    setTimeout(() => {
      this.mostrarNotificacion = false;
    }, 3000);
    
    // Forzar detección de cambios
    this.cdr.detectChanges();
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
    return new Promise((resolve) => {
      this.reservaService.cancelarReserva(reservaId).subscribe({
        next: () => {
          resolve(true);
        },
        error: (error) => {
          console.error('Error al cancelar reserva:', error);
          resolve(false);
        }
      });
    });
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
        this.mostrarMensaje('Reserva anterior cancelada exitosamente', 'success');
        
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
            this.mostrarMensaje('Reserva creada exitosamente', 'success');
            
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

    }
  }

  /**
   * Maneja la inicialización del mapa
   */
  onMapInitialized(map: google.maps.Map) {
    // Verificar que el mapa esté funcionando
    if (map) {
      // Forzar un refresh del mapa después de un breve delay
      setTimeout(() => {
        google.maps.event.trigger(map, 'resize');
        
        // Centrar el mapa en las coordenadas por defecto
        map.setCenter({ lat: -33.4489, lng: -70.6693 });
        map.setZoom(12);
        
        // Forzar detección de cambios
        this.cdr.detectChanges();
      }, 500);
    }
  }
}
