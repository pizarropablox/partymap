import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { MapNavigationService } from '../../services/map-navigation.service';
import { EventoService, Evento as EventoBackend, EventoConUbicacion } from '../../services/evento.service';
import { ReservaService } from '../../services/reserva.service';
import { FileUploadService } from '../../services/file-upload.service';
import { FileStorageService } from '../../services/file-storage.service';
import { EventNotificationService } from '../../services/event-notification.service';

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
  precio?: number;
  precioEntrada?: number;
  capacidad?: number;
  capacidadMaxima?: number;
  organizador?: string;
  categoria?: string;
  imagen?: string;
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

interface EventoForm extends EventoBackend {
  categoria?: string;
  organizador?: string;
  imagen?: string;
  ubicacionId?: number;
}

interface NuevoEvento {
  // Campos del evento
  nombre: string;
  descripcion: string;
  fecha: string; // datetime-local format
  capacidadMaxima: number;
  precioEntrada: number;
  imagenPath?: string;
  
  // Campos de ubicación
  direccion: string;
  comuna: string;
  latitud: number;
  longitud: number;
}

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class EventsComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef;
  
  http = inject(HttpClient);
  router = inject(Router);

  // 📋 Lista de eventos
  eventos: Evento[] = [];
  eventosFiltrados: Evento[] = [];
  
  // 🔍 Filtros
  filtroEventoNombre = '';
  filtroEventoFecha = '';
  
  // 📅 Categorías disponibles
  categorias = [
    'Música',
    'Deportes',
    'Cultura',
    'Gastronomía',
    'Tecnología',
    'Arte',
    'Educación',
    'Otros'
  ];

  // 🎯 Evento seleccionado para detalles
  eventoSeleccionado: Evento | null = null;
  
  // 📱 Estado de carga
  isLoading = true;
  errorMessage = '';

  // 🔄 Paginación
  paginaActual = 1;
  itemsPorPagina = 10;
  totalPaginas = 1;

  // 📝 Formulario de crear evento
  mostrarModalEvento = false;
  nuevoEvento: NuevoEvento = {
    nombre: '',
    descripcion: '',
    fecha: '',
    capacidadMaxima: 0,
    precioEntrada: 0,
    direccion: '',
    comuna: '',
    latitud: 0,
    longitud: 0,
    imagenPath: ''
  };
  ubicaciones: UbicacionEvento[] = [];
  isSubmitting = false;
  formError = '';
  formSuccess = '';

  // 🆔 Información del usuario
  userRole = '';
  productorId: number | null = null;

  // 🖼️ Carga de imágenes
  selectedImage: File | null = null;
  imagePreview: string | null = null;
  isUploadingImage = false;
  imageUploadError = '';

  // Propiedades para el modal de confirmación de eliminación
  mostrarConfirmacionEliminar = false;
  eventoAEliminar: Evento | null = null;
  isEliminando = false;

  // Propiedades para el modal de notificación de éxito
  mostrarNotificacionExito = false;
  nombreEventoEliminado = '';

  // Propiedades para la edición de eventos
  isEditando = false;
  eventoEditando: Evento | null = null;

  constructor(
    private mapNavigationService: MapNavigationService,
    private eventoService: EventoService,
    private reservaService: ReservaService,
    private fileUploadService: FileUploadService,
    private fileStorageService: FileStorageService,
    private eventNotificationService: EventNotificationService
  ) {}

  ngOnInit() {
    this.obtenerInformacionUsuario();
  }

  /**
   * Obtiene el ID del usuario directamente desde el endpoint
   */
  private obtenerUsuarioIdDirecto(): Promise<number | null> {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });

      this.http.get<any>('http://localhost:8085/usuario/current', { headers }).subscribe({
        next: (response) => {
          console.log('Debug - Respuesta directa de /usuario/current:', response);
          
          if (response && response.id && (response.tipoUsuario === 'PRODUCTOR' || response.tipoUsuario === 'ADMINISTRADOR')) {
            console.log('Debug - Usuario', response.tipoUsuario, 'encontrado con ID:', response.id);
            resolve(response.id);
          } else {
            console.log('Debug - Usuario no es PRODUCTOR/ADMINISTRADOR o no tiene ID válido');
            resolve(null);
          }
        },
        error: (error) => {
          console.error('Debug - Error obteniendo usuario ID directo:', error);
          reject(error);
        }
      });
    });
  }

  /**
   * Obtiene la información del usuario de forma síncrona (para usar en guardarEvento)
   */
  private obtenerInformacionUsuarioSync(): Promise<void> {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });

      this.http.get<any>('http://localhost:8085/usuario/current', { headers }).subscribe({
        next: (response) => {
          console.log('Debug - Usuario actual response (sync):', response);
          
          // Extraer información del usuario de la respuesta
          let userId: number | null = null;
          let userRole: string = '';
          
          // La respuesta viene directamente con id y tipoUsuario
          userId = response.id || null;
          userRole = response.tipoUsuario || '';
          
          console.log('Debug - Usuario extraído:', { userId, userRole });
          
          // Si es PRODUCTOR, usar su ID como productorId
          if (userRole === 'PRODUCTOR' && userId) {
            console.log('Debug - Usuario es PRODUCTOR, estableciendo productorId como userId:', userId);
            this.userRole = userRole;
            this.productorId = userId; // El productor ES el usuario
          } else {
            console.log('Debug - Usuario no es PRODUCTOR o no tiene ID válido');
            this.userRole = userRole;
            this.productorId = null;
          }
          
          resolve();
        },
        error: (error) => {
          console.error('Debug - Error obteniendo usuario (sync):', error);
          reject(error);
        }
      });
    });
  }

  /**
   * Obtiene la información del usuario para determinar qué eventos cargar
   */
  obtenerInformacionUsuario() {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    this.http.get<any>('http://localhost:8085/usuario/current', { headers }).subscribe({
      next: (response) => {
        console.log('Debug - Respuesta de /usuario/current:', response);
        
        // La respuesta viene directamente con id y tipoUsuario
        const userId: number | null = response.id || null;
        const userRole: string = response.tipoUsuario || '';
        
        console.log('Debug - Usuario extraído:', { userId, userRole });
        
        this.userRole = userRole;
        
        // Si es PRODUCTOR, usar su ID como productorId
        if (this.userRole === 'PRODUCTOR' && userId) {
          console.log('Debug - Usuario es PRODUCTOR, estableciendo productorId como userId:', userId);
          this.productorId = userId; // El productor ES el usuario
          this.cargarEventosSegunRol();
        } else {
          console.log('Debug - Usuario no es PRODUCTOR, cargando todos los eventos');
          this.productorId = null;
          this.cargarEventosSegunRol();
        }
      },
      error: (error) => {
        console.error('Debug - Error obteniendo usuario:', error);
        this.cargarEventosSegunRol();
      }
    });
  }

  /**
   * Obtiene el ID del productor para el usuario actual
   * Ahora que el productor se crea en la tabla usuario, usamos el ID del usuario
   */
  obtenerProductorId(usuarioId: number) {
    console.log('Debug - Estableciendo productorId como usuarioId:', usuarioId);
    this.productorId = usuarioId; // El productor ES el usuario
    this.cargarEventosSegunRol();
  }

  /**
   * Carga eventos según el rol del usuario
   */
  cargarEventosSegunRol() {
    if (this.userRole === 'PRODUCTOR' && this.productorId) {
      this.cargarEventosPorProductor();
    } else {
      this.cargarEventos();
    }
  }

  /**
   * Carga las ubicaciones disponibles para el formulario
   */
  cargarUbicaciones() {
    this.http.get<UbicacionEvento[]>('http://localhost:8085/ubicacion/all').subscribe({
      next: (data) => {
        this.ubicaciones = data;
      },
      error: (error) => {
        console.error('Error cargando ubicaciones:', error);
      }
    });
  }

  /**
   * Carga eventos específicos del productor
   */
  cargarEventosPorProductor() {
    if (!this.productorId) {
      this.cargarEventos();
      return;
    }

    this.eventoService.obtenerEventosPorProductor(this.productorId).subscribe({
      next: (data: EventoBackend[]) => {
        this.eventos = data.map((evento: EventoBackend) => ({
          id: evento.id || 0,
          nombre: evento.nombre,
          descripcion: evento.descripcion,
          fecha: evento.fecha,
          ubicacion: evento.ubicacion,
          precioEntrada: evento.precioEntrada,
          capacidadMaxima: evento.capacidadMaxima,
          organizador: 'Organizador del evento',
          categoria: 'Categoría del evento',
          imagen: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=300&fit=crop'
        }));
        
        this.eventosFiltrados = [...this.eventos];
        this.calcularPaginacion();
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error cargando eventos del productor:', err);
        
        // Si es error 500, intentar cargar todos los eventos como fallback
        if (err.status === 500) {
          console.log('Error 500, cargando todos los eventos como fallback');
          this.errorMessage = 'Error al cargar eventos del productor. Cargando todos los eventos...';
          this.cargarEventos();
        } else {
          this.errorMessage = 'Error al cargar los eventos: ' + (err.message || 'Error desconocido');
          this.isLoading = false;
        }
      }
    });
  }

  /**
   * Carga todos los eventos (método alternativo)
   */
  cargarEventos() {
    this.isLoading = true;
    this.errorMessage = '';

    this.eventoService.obtenerEventos().subscribe({
      next: (data: EventoBackend[]) => {
        this.eventos = data.map((evento: EventoBackend) => ({
          id: evento.id || 0,
          nombre: evento.nombre,
          descripcion: evento.descripcion,
          fecha: evento.fecha,
          ubicacion: evento.ubicacion,
          precioEntrada: evento.precioEntrada,
          capacidadMaxima: evento.capacidadMaxima,
          organizador: 'Organizador del evento',
          categoria: 'Categoría del evento',
          imagen: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=300&fit=crop'
        }));
        
        this.eventosFiltrados = [...this.eventos];
        this.calcularPaginacion();
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error cargando todos los eventos:', err);
        this.errorMessage = 'Error al cargar los eventos: ' + (err.message || 'Error desconocido');
        this.isLoading = false;
      }
    });
  }

  /**
   * Aplica filtros a los eventos
   */
  aplicarFiltroEventos() {
    this.eventosFiltrados = this.eventos.filter(evento => {
      const cumpleNombre = !this.filtroEventoNombre || 
        evento.nombre.toLowerCase().includes(this.filtroEventoNombre.toLowerCase());
      
      const cumpleFecha = !this.filtroEventoFecha || 
        evento.fecha.startsWith(this.filtroEventoFecha);
      
      return cumpleNombre && cumpleFecha;
    });
    
    this.paginaActual = 1;
    this.calcularPaginacion();
  }

  /**
   * Calcula la paginación
   */
  calcularPaginacion() {
    this.totalPaginas = Math.ceil(this.eventosFiltrados.length / this.itemsPorPagina);
  }

  /**
   * Obtiene los eventos de la página actual
   */
  get eventosPaginados(): Evento[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    return this.eventosFiltrados.slice(inicio, fin);
  }

  /**
   * Navega a la página anterior
   */
  paginaAnterior() {
    if (this.paginaActual > 1) {
      this.paginaActual--;
    }
  }

  /**
   * Navega a la página siguiente
   */
  paginaSiguiente() {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
    }
  }

  /**
   * Navega a una página específica
   */
  irAPagina(pagina: number) {
    this.paginaActual = pagina;
  }

  /**
   * Obtiene el rango de la página actual
   */
  obtenerRangoPagina(): string {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.itemsPorPagina, this.eventosFiltrados.length);
    return `Mostrando ${inicio}-${fin} de ${this.eventosFiltrados.length} eventos`;
  }

  /**
   * Muestra los detalles de un evento
   */
  mostrarDetalles(evento: Evento) {
    this.eventoSeleccionado = evento;
  }

  /**
   * Cierra los detalles del evento
   */
  cerrarDetalles() {
    this.eventoSeleccionado = null;
  }

  /**
   * Navega al mapa y centra en la ubicación del evento
   */
  irAlMapa(evento: Evento) {
    // Crear objeto de ubicación para el servicio de navegación
    const ubicacion = {
      nombre: evento.nombre,
      latitud: evento.ubicacion.latitud,
      longitud: evento.ubicacion.longitud,
      direccion: evento.ubicacion.direccion,
      comuna: evento.ubicacion.comuna,
      isGeocoded: false // Es una ubicación del backend
    };
    
    // Navegar a la ubicación en el mapa
    this.mapNavigationService.navigateToLocation(ubicacion);
    
    // Navegar a la página del mapa (ruta raíz)
    this.router.navigate(['/']);
    
    // Cerrar el modal si está abierto
    this.cerrarDetalles();
  }

  /**
   * Formatea el precio para mostrar
   */
  formatearPrecio(precio?: number): string {
    if (!precio || precio === 0) {
      return 'Gratis';
    }
    return `$${precio.toLocaleString('es-CL')}`;
  }

  /**
   * Formatea la fecha para mostrar
   */
  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-CL', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Obtiene el estado del evento (próximo, hoy, pasado)
   */
  getEstadoEvento(fecha: string): string {
    const ahora = new Date();
    const fechaEvento = new Date(fecha);
    const diffTime = fechaEvento.getTime() - ahora.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) {
      return 'pasado';
    } else if (diffDays === 0) {
      return 'hoy';
    } else if (diffDays <= 7) {
      return 'proximo';
    } else {
      return 'futuro';
    }
  }

  /**
   * Maneja errores de carga de imágenes
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    if (img) {
      img.src = 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=300&fit=crop';
    }
  }

  /**
   * Abre el modal para crear un nuevo evento
   */
  crearNuevoEvento() {
    // Limpiar estado de edición
    this.isEditando = false;
    this.eventoEditando = null;
    
    // Limpiar formulario y abrir modal
    this.limpiarFormulario();
    this.mostrarModalEvento = true;
  }

  /**
   * Cierra el modal de evento
   */
  cerrarModalEvento() {
    this.mostrarModalEvento = false;
    this.limpiarFormulario();
    
    // Limpiar estado de edición
    this.isEditando = false;
    this.eventoEditando = null;
  }

  /**
   * Limpia el formulario
   */
  limpiarFormulario() {
    this.nuevoEvento = {
      nombre: '',
      descripcion: '',
      fecha: '',
      capacidadMaxima: 0,
      precioEntrada: 0,
      direccion: '',
      comuna: '',
      latitud: 0,
      longitud: 0,
      imagenPath: ''
    };
    this.formError = '';
    this.formSuccess = '';
    this.clearSelectedImage();
  }

  /**
   * Guarda el evento (crear o editar)
   */
  async guardarEvento() {
    console.log('🚀 Iniciando proceso de guardado:', {
      modo: this.isEditando ? 'EDICIÓN' : 'CREACIÓN',
      eventoId: this.eventoEditando?.id
    });

    // Validar formulario
    if (!this.validarFormulario()) {
      console.error('❌ Validación del formulario falló');
      return;
    }

    this.isSubmitting = true;
    this.formError = '';
    this.formSuccess = '';

    try {
      // Obtener el ID del usuario para el evento
      let usuarioId: number | null = null;
      
      // Para PRODUCTOR y ADMINISTRADOR, obtener el ID del usuario
      if (this.userRole === 'PRODUCTOR' || this.userRole === 'ADMINISTRADOR') {
        if (this.userRole === 'PRODUCTOR' && this.productorId) {
          usuarioId = this.productorId;
        } else {
          try {
            usuarioId = await this.obtenerUsuarioIdDirecto();
          } catch (error) {
            console.error('Error obteniendo ID del usuario:', error);
          }
        }
      }

      if (!usuarioId) {
        this.formError = 'Error: No se pudo obtener el ID del usuario. Por favor, recarga la página e intenta nuevamente.';
        return;
      }

      // Preparar datos del evento
      const eventoData: EventoConUbicacion = {
        evento: {
          nombre: this.nuevoEvento.nombre,
          descripcion: this.nuevoEvento.descripcion,
          fecha: this.nuevoEvento.fecha,
          capacidadMaxima: this.nuevoEvento.capacidadMaxima,
          precioEntrada: this.nuevoEvento.precioEntrada,
          imagenPath: this.nuevoEvento.imagenPath || '',
          usuarioId: usuarioId,
          cuposDisponibles: this.nuevoEvento.capacidadMaxima,
          disponible: true,
          activo: 1
        },
        ubicacion: {
          direccion: this.nuevoEvento.direccion,
          comuna: this.nuevoEvento.comuna,
          latitud: this.nuevoEvento.latitud,
          longitud: this.nuevoEvento.longitud,
          activo: 1
        }
      };

      console.log('📋 Datos preparados para envío:', eventoData);

      let response: any;

      if (this.isEditando && this.eventoEditando?.id) {
        // MODO EDICIÓN
        console.log('✏️ Actualizando evento existente con ID:', this.eventoEditando.id);
        
        response = await this.eventoService.actualizarEventoConUbicacion(
          this.eventoEditando.id, 
          eventoData
        ).toPromise();

        console.log('✅ Evento actualizado exitosamente:', response);
        this.formSuccess = `Evento "${this.nuevoEvento.nombre}" actualizado exitosamente`;
        
        // Actualizar el evento en la lista
        const index = this.eventos.findIndex(e => e.id === this.eventoEditando?.id);
        if (index !== -1) {
          this.eventos[index] = {
            ...this.eventos[index],
            nombre: this.nuevoEvento.nombre,
            descripcion: this.nuevoEvento.descripcion,
            fecha: this.nuevoEvento.fecha,
            capacidadMaxima: this.nuevoEvento.capacidadMaxima,
            precioEntrada: this.nuevoEvento.precioEntrada,
            ubicacion: {
              direccion: this.nuevoEvento.direccion,
              comuna: this.nuevoEvento.comuna,
              latitud: this.nuevoEvento.latitud,
              longitud: this.nuevoEvento.longitud
            }
          };
        }
      } else {
        // MODO CREACIÓN
        console.log('🆕 Creando nuevo evento');
        
        response = await this.eventoService.crearEventoConUbicacion(eventoData).toPromise();

        console.log('✅ Evento creado exitosamente:', response);
        this.formSuccess = `Evento "${this.nuevoEvento.nombre}" creado exitosamente`;
        
        // Agregar el nuevo evento a la lista
        const nuevoEvento: Evento = {
          id: response.id || Date.now(),
          nombre: this.nuevoEvento.nombre,
          descripcion: this.nuevoEvento.descripcion,
          fecha: this.nuevoEvento.fecha,
          capacidadMaxima: this.nuevoEvento.capacidadMaxima,
          precioEntrada: this.nuevoEvento.precioEntrada,
          ubicacion: {
            direccion: this.nuevoEvento.direccion,
            comuna: this.nuevoEvento.comuna,
            latitud: this.nuevoEvento.latitud,
            longitud: this.nuevoEvento.longitud
          }
        };
        
        this.eventos.unshift(nuevoEvento);
      }

      // Actualizar eventos filtrados
      this.eventosFiltrados = [...this.eventos];
      
      // Recalcular paginación
      this.calcularPaginacion();

      // Notificar al mapa para recargar
      this.notificarMapaRecargar();

      // Cerrar modal después de un breve delay para mostrar el mensaje de éxito
      setTimeout(() => {
        this.cerrarModalEvento();
        this.actualizarEventos(); // Recargar datos del servidor
      }, 1500);

    } catch (error: any) {
      console.error('❌ Error al guardar el evento:', error);
      
      let mensajeError = 'Error al guardar el evento';
      
      if (error.error && typeof error.error === 'string') {
        mensajeError = error.error;
      } else if (error.error && error.error.message) {
        mensajeError = error.error.message;
      } else if (error.message) {
        mensajeError = error.message;
      }

      this.formError = mensajeError;
    } finally {
      this.isSubmitting = false;
    }
  }

  /**
   * Valida el formulario de creación de evento con todas las reglas especificadas
   */
  validarFormulario(): boolean {
    console.log('🔍 Iniciando validación del formulario de evento...');
    
    // 1. VALIDACIONES DEL EVENTO
    
    // Nombre del Evento
    if (!this.nuevoEvento.nombre || this.nuevoEvento.nombre.trim() === '') {
      this.formError = 'El nombre del evento es obligatorio';
      console.error('❌ Validación fallida - Nombre:', { 
        valor: this.nuevoEvento.nombre, 
        longitud: this.nuevoEvento.nombre?.length || 0 
      });
      return false;
    }
    
    const nombreLength = this.nuevoEvento.nombre.trim().length;
    if (nombreLength < 3) {
      this.formError = `El nombre del evento debe tener al menos 3 caracteres (actual: ${nombreLength})`;
      console.error('❌ Validación fallida - Nombre muy corto:', { 
        valor: this.nuevoEvento.nombre, 
        longitud: nombreLength 
      });
      return false;
    }
    
    if (nombreLength > 100) {
      this.formError = `El nombre del evento no puede exceder 100 caracteres (actual: ${nombreLength})`;
      console.error('❌ Validación fallida - Nombre muy largo:', { 
        valor: this.nuevoEvento.nombre, 
        longitud: nombreLength 
      });
      return false;
    }
    
    console.log('✅ Nombre del evento válido:', { 
      valor: this.nuevoEvento.nombre, 
      longitud: nombreLength 
    });
    
    // Descripción del Evento
    if (!this.nuevoEvento.descripcion || this.nuevoEvento.descripcion.trim() === '') {
      this.formError = 'La descripción del evento es obligatoria';
      console.error('❌ Validación fallida - Descripción vacía:', { 
        valor: this.nuevoEvento.descripcion, 
        longitud: this.nuevoEvento.descripcion?.length || 0 
      });
      return false;
    }
    
    const descripcionLength = this.nuevoEvento.descripcion.trim().length;
    if (descripcionLength < 10) {
      this.formError = `La descripción debe tener al menos 10 caracteres (actual: ${descripcionLength})`;
      console.error('❌ Validación fallida - Descripción muy corta:', { 
        valor: this.nuevoEvento.descripcion, 
        longitud: descripcionLength 
      });
      return false;
    }
    
    if (descripcionLength > 2000) {
      this.formError = `La descripción no puede exceder 2000 caracteres (actual: ${descripcionLength})`;
      console.error('❌ Validación fallida - Descripción muy larga:', { 
        valor: this.nuevoEvento.descripcion, 
        longitud: descripcionLength 
      });
      return false;
    }
    
    console.log('✅ Descripción del evento válida:', { 
      valor: this.nuevoEvento.descripcion.substring(0, 50) + '...', 
      longitud: descripcionLength 
    });
    
    // Fecha del Evento
    if (!this.nuevoEvento.fecha) {
      this.formError = 'La fecha del evento es obligatoria';
      console.error('❌ Validación fallida - Fecha vacía:', { 
        valor: this.nuevoEvento.fecha 
      });
      return false;
    }
    
    const fechaEvento = new Date(this.nuevoEvento.fecha);
    const fechaActual = new Date();
    
    if (fechaEvento <= fechaActual) {
      this.formError = `La fecha del evento debe ser futura (fecha ingresada: ${fechaEvento.toLocaleString()})`;
      console.error('❌ Validación fallida - Fecha en el pasado:', { 
        fechaIngresada: fechaEvento.toISOString(),
        fechaActual: fechaActual.toISOString(),
        diferencia: fechaEvento.getTime() - fechaActual.getTime()
      });
      return false;
    }
    
    console.log('✅ Fecha del evento válida:', { 
      fechaIngresada: fechaEvento.toISOString(),
      fechaActual: fechaActual.toISOString(),
      diferencia: fechaEvento.getTime() - fechaActual.getTime()
    });
    
    // Capacidad Máxima
    if (!this.nuevoEvento.capacidadMaxima || this.nuevoEvento.capacidadMaxima < 1) {
      this.formError = 'La capacidad máxima debe ser mayor a 0';
      console.error('❌ Validación fallida - Capacidad máxima inválida:', { 
        valor: this.nuevoEvento.capacidadMaxima 
      });
      return false;
    }
    
    console.log('✅ Capacidad máxima válida:', { 
      valor: this.nuevoEvento.capacidadMaxima 
    });
    
    // Precio de Entrada
    if (this.nuevoEvento.precioEntrada < 0) {
      this.formError = 'El precio de entrada no puede ser negativo';
      console.error('❌ Validación fallida - Precio negativo:', { 
        valor: this.nuevoEvento.precioEntrada 
      });
      return false;
    }
    
    console.log('✅ Precio de entrada válido:', { 
      valor: this.nuevoEvento.precioEntrada 
    });
    
    // 2. VALIDACIONES DE UBICACIÓN
    
    // Dirección
    if (!this.nuevoEvento.direccion || this.nuevoEvento.direccion.trim() === '') {
      this.formError = 'La dirección es obligatoria';
      console.error('❌ Validación fallida - Dirección vacía:', { 
        valor: this.nuevoEvento.direccion, 
        longitud: this.nuevoEvento.direccion?.length || 0 
      });
      return false;
    }
    
    const direccionLength = this.nuevoEvento.direccion.trim().length;
    if (direccionLength < 5) {
      this.formError = `La dirección debe tener al menos 5 caracteres (actual: ${direccionLength})`;
      console.error('❌ Validación fallida - Dirección muy corta:', { 
        valor: this.nuevoEvento.direccion, 
        longitud: direccionLength 
      });
      return false;
    }
    
    console.log('✅ Dirección válida:', { 
      valor: this.nuevoEvento.direccion, 
      longitud: direccionLength 
    });
    
    // Comuna
    if (!this.nuevoEvento.comuna || this.nuevoEvento.comuna.trim() === '') {
      this.formError = 'La comuna es obligatoria';
      console.error('❌ Validación fallida - Comuna vacía:', { 
        valor: this.nuevoEvento.comuna, 
        longitud: this.nuevoEvento.comuna?.length || 0 
      });
      return false;
    }
    
    const comunaLength = this.nuevoEvento.comuna.trim().length;
    if (comunaLength < 2) {
      this.formError = `La comuna debe tener al menos 2 caracteres (actual: ${comunaLength})`;
      console.error('❌ Validación fallida - Comuna muy corta:', { 
        valor: this.nuevoEvento.comuna, 
        longitud: comunaLength 
      });
      return false;
    }
    
    console.log('✅ Comuna válida:', { 
      valor: this.nuevoEvento.comuna, 
      longitud: comunaLength 
    });
    
    // Coordenadas
    if (this.nuevoEvento.latitud === null || this.nuevoEvento.latitud === undefined) {
      this.formError = 'La latitud es obligatoria';
      console.error('❌ Validación fallida - Latitud null/undefined:', { 
        valor: this.nuevoEvento.latitud 
      });
      return false;
    }
    
    if (this.nuevoEvento.longitud === null || this.nuevoEvento.longitud === undefined) {
      this.formError = 'La longitud es obligatoria';
      console.error('❌ Validación fallida - Longitud null/undefined:', { 
        valor: this.nuevoEvento.longitud 
      });
      return false;
    }
    
    console.log('✅ Coordenadas válidas:', { 
      latitud: this.nuevoEvento.latitud,
      longitud: this.nuevoEvento.longitud
    });
    
    // 3. VALIDACIÓN FINAL
    console.log('🎉 Todas las validaciones pasaron exitosamente');
    this.formError = '';
    return true;
  }

  /**
   * Abre el modal para editar un evento
   */
  editarEvento(evento: Evento) {
    if (!evento.id) {
      console.error('❌ No se puede editar el evento: ID no disponible');
      alert('Error: No se puede editar el evento. ID no disponible.');
      return;
    }

    console.log('✏️ Abriendo modal de edición para evento:', {
      id: evento.id,
      nombre: evento.nombre,
      fecha: evento.fecha
    });

    // Configurar el modal para edición
    this.isEditando = true;
    this.eventoEditando = evento;
    
    // Cargar los datos del evento en el formulario
    this.nuevoEvento = {
      nombre: evento.nombre,
      descripcion: evento.descripcion,
      fecha: this.formatearFechaParaInput(evento.fecha),
      capacidadMaxima: evento.capacidadMaxima || evento.capacidad || 0,
      precioEntrada: evento.precioEntrada || evento.precio || 0,
      imagenPath: evento.imagen || '',
      direccion: evento.ubicacion.direccion,
      comuna: evento.ubicacion.comuna,
      latitud: evento.ubicacion.latitud,
      longitud: evento.ubicacion.longitud
    };

    // Limpiar mensajes previos
    this.formError = '';
    this.formSuccess = '';
    
    // Abrir el modal
    this.mostrarModalEvento = true;
  }

  /**
   * Formatea la fecha para el input datetime-local
   */
  private formatearFechaParaInput(fecha: string): string {
    const fechaObj = new Date(fecha);
    const year = fechaObj.getFullYear();
    const month = String(fechaObj.getMonth() + 1).padStart(2, '0');
    const day = String(fechaObj.getDate()).padStart(2, '0');
    const hours = String(fechaObj.getHours()).padStart(2, '0');
    const minutes = String(fechaObj.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  /**
   * Elimina un evento
   */
  eliminarEvento(evento: Evento) {
    if (!evento.id) {
      console.error('❌ No se puede eliminar el evento: ID no disponible');
      alert('Error: No se puede eliminar el evento. ID no disponible.');
      return;
    }

    console.log('🗑️ Abriendo modal de confirmación para eliminar evento:', {
      id: evento.id,
      nombre: evento.nombre,
      fecha: evento.fecha
    });

    // Configurar el modal de confirmación
    this.eventoAEliminar = evento;
    this.mostrarConfirmacionEliminar = true;
  }

  /**
   * Cancela la eliminación del evento
   */
  cancelarEliminacion() {
    console.log('❌ Eliminación cancelada por el usuario');
    this.mostrarConfirmacionEliminar = false;
    this.eventoAEliminar = null;
    this.isEliminando = false;
  }

  /**
   * Confirma la eliminación del evento
   */
  confirmarEliminacion() {
    if (!this.eventoAEliminar || !this.eventoAEliminar.id) {
      console.error('❌ No se puede eliminar el evento: datos no disponibles');
      this.cancelarEliminacion();
      return;
    }

    console.log('🗑️ Iniciando eliminación del evento:', {
      id: this.eventoAEliminar.id,
      nombre: this.eventoAEliminar.nombre,
      fecha: this.eventoAEliminar.fecha
    });

    // Mostrar indicador de carga
    this.isEliminando = true;

    this.eventoService.eliminarEvento(this.eventoAEliminar.id).subscribe({
      next: () => {
        console.log('✅ Evento eliminado exitosamente:', {
          id: this.eventoAEliminar?.id,
          nombre: this.eventoAEliminar?.nombre
        });

        // Remover el evento de las listas
        this.eventos = this.eventos.filter(e => e.id !== this.eventoAEliminar?.id);
        this.eventosFiltrados = this.eventosFiltrados.filter(e => e.id !== this.eventoAEliminar?.id);

        // Recalcular paginación
        this.calcularPaginacion();

        // Guardar el nombre antes de limpiar el estado
        const nombreEventoEliminado = this.eventoAEliminar?.nombre;

        // Cerrar modal y limpiar estado
        this.mostrarConfirmacionEliminar = false;
        this.eventoAEliminar = null;
        this.isEliminando = false;

        // Mostrar notificación de éxito
        this.nombreEventoEliminado = nombreEventoEliminado || '';
        this.mostrarNotificacionExito = true;

        // Notificar al mapa para recargar
        this.notificarMapaRecargar();

        console.log('📊 Estado actualizado después de eliminación:', {
          totalEventos: this.eventos.length,
          eventosFiltrados: this.eventosFiltrados.length,
          paginaActual: this.paginaActual,
          totalPaginas: this.totalPaginas
        });
      },
      error: (error) => {
        console.error('❌ Error al eliminar el evento:', {
          id: this.eventoAEliminar?.id,
          nombre: this.eventoAEliminar?.nombre,
          error: error
        });

        let mensajeError = 'Error al eliminar el evento';
        
        if (error.error && typeof error.error === 'string') {
          mensajeError = error.error;
        } else if (error.error && error.error.message) {
          mensajeError = error.error.message;
        } else if (error.message) {
          mensajeError = error.message;
        }

        // Cerrar modal y limpiar estado
        this.mostrarConfirmacionEliminar = false;
        this.eventoAEliminar = null;
        this.isEliminando = false;

        alert(`Error al eliminar el evento: ${mensajeError}`);
      },
      complete: () => {
        console.log('🏁 Proceso de eliminación completado');
      }
    });
  }

  /**
   * Actualiza los eventos según el rol del usuario
   */
  actualizarEventos() {
    const userRoleLower = this.userRole.toLowerCase();
    
    if (userRoleLower === 'productor' && this.productorId !== null) {
      this.cargarEventosPorProductor();
    } else {
      // ADMINISTRADOR, CLIENTE, o PRODUCTOR sin ID válido cargan todos los eventos
      this.cargarEventos();
    }
  }

  /**
   * Maneja la selección de un archivo de imagen
   */
  onImageSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    // Validar el archivo
    if (!this.fileUploadService.validateImageFile(file)) {
      this.imageUploadError = 'Por favor selecciona una imagen válida (JPG, PNG, GIF, WebP) de máximo 5MB';
      return;
    }

    this.selectedImage = file;
    this.imageUploadError = '';

    // Crear preview
    this.fileUploadService.fileToBase64(file).then(base64 => {
      this.imagePreview = base64;
    });
  }

  /**
   * Sube la imagen seleccionada al servidor
   */
  uploadSelectedImage(): Promise<string> {
    return new Promise((resolve, reject) => {
      if (!this.selectedImage) {
        resolve(this.nuevoEvento.imagenPath || '');
        return;
      }

      this.isUploadingImage = true;
      this.imageUploadError = '';

      this.fileUploadService.uploadImage(this.selectedImage).subscribe({
        next: (response) => {
          this.isUploadingImage = false;
          this.nuevoEvento.imagenPath = response.imagePath;
          resolve(response.imagePath);
        },
        error: (error) => {
          this.isUploadingImage = false;
          this.imageUploadError = 'Error al subir la imagen: ' + (error.message || 'Error desconocido');
          reject(error);
        }
      });
    });
  }

  /**
   * Limpia la imagen seleccionada
   */
  clearSelectedImage() {
    if (this.nuevoEvento.imagenPath) {
      this.fileStorageService.removeImageFromStorage(this.nuevoEvento.imagenPath);
    }
    this.selectedImage = null;
    this.imagePreview = null;
    this.imageUploadError = '';
    this.nuevoEvento.imagenPath = '';
  }

  /**
   * Abre el selector de archivos
   */
  openFileSelector() {
    this.fileInput.nativeElement.click();
  }

  /**
   * Obtiene la URL de una imagen almacenada
   * @param imagePath Ruta de la imagen
   * @returns URL de la imagen o null si no existe
   */
  getImageUrl(imagePath: string): string | null {
    return this.fileUploadService.getImageUrl(imagePath);
  }

  /**
   * Notifica al mapa que debe recargar los eventos
   */
  notificarMapaRecargar() {
    this.eventNotificationService.notifyEventCreated();
    console.log('Notificación enviada al mapa para recargar eventos');
  }

  /**
   * Geocodifica automáticamente la dirección ingresada
   */
  async geocodificarDireccion() {
    if (!this.nuevoEvento.direccion || !this.nuevoEvento.comuna) {
      this.formError = 'Por favor ingresa tanto la dirección como la comuna';
      return;
    }

    // Limpiar errores anteriores
    this.formError = '';
    this.formSuccess = '';

    const direccionCompleta = `${this.nuevoEvento.direccion}, ${this.nuevoEvento.comuna}, Chile`;
    console.log('🔍 Geocodificando dirección:', direccionCompleta);

    try {
      // Mostrar indicador de carga
      this.formSuccess = 'Buscando ubicación...';
      
      // Intentar primero con coordenadas predefinidas (más confiable)
      let success = await this.tryPredefinedCoordinates();
      
      // Si no encuentra coordenadas predefinidas, intentar con API (opcional)
      if (!success) {
        console.log('🔄 Intentando con API de Google Maps...');
        success = await this.tryGeocodingWithKey(direccionCompleta, 'AIzaSyCRQWonRBlilzJNvnyyFKaXmgn54yCL5EY');
      }
      
      if (!success) {
        this.formError = 'No se pudo encontrar la ubicación. Verifica que la dirección y comuna sean correctas.';
        this.formSuccess = '';
      }
      
    } catch (error) {
      console.error('❌ Error al geocodificar:', error);
      this.formError = 'Error de conexión. Verifica tu conexión a internet e intenta nuevamente.';
      this.formSuccess = '';
    }
  }

  /**
   * Intenta geocodificar con una API key específica
   */
  private async tryGeocodingWithKey(direccionCompleta: string, apiKey: string): Promise<boolean> {
    try {
      const url = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(direccionCompleta)}&key=${apiKey}&language=es&region=cl`;
      console.log('🌐 Intentando con URL:', url);
      
      const response = await fetch(url);
      console.log('📡 Respuesta del servidor:', response.status, response.statusText);
      
      if (!response.ok) {
        console.log('❌ Error HTTP:', response.status);
        return false;
      }
      
      const data = await response.json();
      console.log('📦 Datos de respuesta:', data);

      if (data.status === 'OK' && data.results && data.results.length > 0) {
        const location = data.results[0].geometry.location;
        this.nuevoEvento.latitud = location.lat;
        this.nuevoEvento.longitud = location.lng;
        
        console.log('✅ Dirección geocodificada exitosamente:', {
          direccion: direccionCompleta,
          lat: location.lat,
          lng: location.lng,
          formatted_address: data.results[0].formatted_address
        });
        
        // Mostrar mensaje de éxito
        this.formSuccess = `Ubicación cargada: ${data.results[0].formatted_address}`;
        setTimeout(() => {
          this.formSuccess = '';
        }, 5000);
        
        return true;
      } else {
        console.log('⚠️ No se encontraron coordenadas. Status:', data.status);
        return false;
      }
    } catch (error) {
      console.error('❌ Error con API key:', apiKey, error);
      return false;
    }
  }

  /**
   * Intenta usar coordenadas predefinidas para comunas comunes
   */
  private async tryPredefinedCoordinates(): Promise<boolean> {
    const comunasPredefinidas: { [key: string]: { lat: number; lng: number; nombre: string } } = {
      // Santiago Centro
      'santiago': { lat: -33.4489, lng: -70.6693, nombre: 'Santiago' },
      'providencia': { lat: -33.4186, lng: -70.6062, nombre: 'Providencia' },
      'las condes': { lat: -33.4167, lng: -70.5833, nombre: 'Las Condes' },
      'ñuñoa': { lat: -33.4569, lng: -70.6483, nombre: 'Ñuñoa' },
      'vitacura': { lat: -33.3833, lng: -70.5667, nombre: 'Vitacura' },
      'lo barnechea': { lat: -33.3500, lng: -70.5167, nombre: 'Lo Barnechea' },
      
      // Santiago Sur
      'maipú': { lat: -33.5167, lng: -70.7667, nombre: 'Maipú' },
      'puente alto': { lat: -33.6167, lng: -70.5833, nombre: 'Puente Alto' },
      'la florida': { lat: -33.5333, lng: -70.5833, nombre: 'La Florida' },
      'san miguel': { lat: -33.4833, lng: -70.6500, nombre: 'San Miguel' },
      'la granja': { lat: -33.5333, lng: -70.6333, nombre: 'La Granja' },
      'la cisterna': { lat: -33.5333, lng: -70.6500, nombre: 'La Cisterna' },
      'el bosque': { lat: -33.5667, lng: -70.6667, nombre: 'El Bosque' },
      'pedro aguirre cerda': { lat: -33.4833, lng: -70.6833, nombre: 'Pedro Aguirre Cerda' },
      'lo espejo': { lat: -33.5167, lng: -70.6833, nombre: 'Lo Espejo' },
      'estación central': { lat: -33.4500, lng: -70.6833, nombre: 'Estación Central' },
      
      // Santiago Norte
      'recoleta': { lat: -33.4167, lng: -70.6500, nombre: 'Recoleta' },
      'independencia': { lat: -33.4167, lng: -70.6667, nombre: 'Independencia' },
      'conchalí': { lat: -33.3833, lng: -70.6667, nombre: 'Conchalí' },
      'huechuraba': { lat: -33.3667, lng: -70.6333, nombre: 'Huechuraba' },
      'colina': { lat: -33.2000, lng: -70.6833, nombre: 'Colina' },
      'lampa': { lat: -33.2833, lng: -70.8833, nombre: 'Lampa' },
      'til til': { lat: -33.0833, lng: -70.9333, nombre: 'Til Til' },
      
      // Santiago Poniente
      'cerro navia': { lat: -33.4167, lng: -70.7333, nombre: 'Cerro Navia' },
      'quinta normal': { lat: -33.4333, lng: -70.7000, nombre: 'Quinta Normal' },
      'lo prado': { lat: -33.4333, lng: -70.7167, nombre: 'Lo Prado' },
      'pudahuel': { lat: -33.4500, lng: -70.7500, nombre: 'Pudahuel' },
      'cerrillos': { lat: -33.4833, lng: -70.7167, nombre: 'Cerrillos' },
      'el monte': { lat: -33.6833, lng: -70.9833, nombre: 'El Monte' },
      'isla de maipo': { lat: -33.7500, lng: -70.9000, nombre: 'Isla de Maipo' },
      'talagante': { lat: -33.6667, lng: -70.9333, nombre: 'Talagante' },
      'peñaflor': { lat: -33.6167, lng: -70.8833, nombre: 'Peñaflor' },
      
      // Santiago Oriente
      'la reina': { lat: -33.4500, lng: -70.5500, nombre: 'La Reina' },
      'macul': { lat: -33.4833, lng: -70.6167, nombre: 'Macul' },
      'peñalolén': { lat: -33.4833, lng: -70.5500, nombre: 'Peñalolén' },
      'pirque': { lat: -33.6333, lng: -70.5500, nombre: 'Pirque' },
      'san josé de maipo': { lat: -33.6333, lng: -70.3500, nombre: 'San José de Maipo' },
      
      // Otras ciudades importantes
      'valparaíso': { lat: -33.0458, lng: -71.6197, nombre: 'Valparaíso' },
      'viña del mar': { lat: -33.0245, lng: -71.5518, nombre: 'Viña del Mar' },
      'concepción': { lat: -36.8201, lng: -73.0444, nombre: 'Concepción' },
      'la serena': { lat: -29.9027, lng: -71.2519, nombre: 'La Serena' },
      'antofagasta': { lat: -23.6504, lng: -70.4000, nombre: 'Antofagasta' },
      'temuco': { lat: -38.7397, lng: -72.5984, nombre: 'Temuco' },
      'arica': { lat: -18.4783, lng: -70.3126, nombre: 'Arica' },
      'punta arenas': { lat: -53.1638, lng: -70.9171, nombre: 'Punta Arenas' }
    };

    const comunaLower = this.nuevoEvento.comuna.toLowerCase().trim();
    
    for (const [key, coords] of Object.entries(comunasPredefinidas)) {
      if (comunaLower.includes(key) || key.includes(comunaLower)) {
        this.nuevoEvento.latitud = coords.lat;
        this.nuevoEvento.longitud = coords.lng;
        
        console.log('✅ Usando coordenadas predefinidas:', {
          comuna: this.nuevoEvento.comuna,
          lat: coords.lat,
          lng: coords.lng,
          nombre: coords.nombre
        });
        
        this.formSuccess = `Ubicación cargada: ${coords.nombre}`;
        setTimeout(() => {
          this.formSuccess = '';
        }, 5000);
        
        return true;
      }
    }
    
    return false;
  }

  /**
   * Verifica si un evento se creó realmente en la base de datos
   */
  private verificarEventoCreado(nombreEvento: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.eventoService.obtenerEventos().subscribe({
        next: (eventos) => {
          const eventoEncontrado = eventos.find(e => e.nombre === nombreEvento);
          console.log('Debug - Verificando evento creado:', nombreEvento, 'Encontrado:', !!eventoEncontrado);
          resolve(!!eventoEncontrado);
        },
        error: (error) => {
          console.error('Debug - Error verificando evento:', error);
          resolve(false);
        }
      });
    });
  }

  /**
   * Método temporal para alternar entre todos los eventos y eventos del productor
   */
  alternarVistaEventos() {
    if (this.userRole === 'PRODUCTOR' && this.productorId) {
      this.cargarEventos();
    } else {
      this.cargarEventosPorProductor();
    }
  }

  /**
   * Cierra la notificación de éxito
   */
  cerrarNotificacionExito() {
    this.mostrarNotificacionExito = false;
    this.nombreEventoEliminado = '';
  }
} 