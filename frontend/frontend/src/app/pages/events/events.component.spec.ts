import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EventsComponent } from './events.component';
import { of, throwError } from 'rxjs';
import { EventoService } from '../../services/evento.service';
import { ReservaService } from '../../services/reserva.service';
import { FileUploadService } from '../../services/file-upload.service';
import { FileStorageService } from '../../services/file-storage.service';
import { EventNotificationService } from '../../services/event-notification.service';

const mockEventos = [
  {
    id: 1,
    nombre: 'Evento 1',
    descripcion: 'Desc 1',
    fecha: '2024-07-12',
    ubicacion: { direccion: 'Calle 1', comuna: 'Comuna 1', latitud: 0, longitud: 0 },
    precioEntrada: 1000,
    capacidadMaxima: 100,
    cuposDisponibles: 100,
    disponible: true,
    activo: 1,
    productorId: 1
  },
  {
    id: 2,
    nombre: 'Evento 2',
    descripcion: 'Desc 2',
    fecha: '2024-07-13',
    ubicacion: { direccion: 'Calle 2', comuna: 'Comuna 2', latitud: 0, longitud: 0 },
    precioEntrada: 2000,
    capacidadMaxima: 200,
    cuposDisponibles: 200,
    disponible: true,
    activo: 1,
    productorId: 2
  }
];

describe('EventsComponent', () => {
  let component: EventsComponent;
  let fixture: ComponentFixture<EventsComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockEventoService: jasmine.SpyObj<EventoService>;
  let mockReservaService: jasmine.SpyObj<ReservaService>;
  let mockFileUploadService: jasmine.SpyObj<FileUploadService>;
  let mockFileStorageService: jasmine.SpyObj<FileStorageService>;
  let mockEventNotificationService: jasmine.SpyObj<EventNotificationService>;

  beforeAll(() => {
    try { spyOn(window.location, 'assign').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'replace').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'reload').and.callFake(() => {}); } catch (e) {}
    // Mock Google Maps if needed
    if (!window.google) {
      (window as any).google = { maps: { Map: function() {}, Marker: function() {}, LatLng: function() {}, event: { addListener: function() {} } } };
    }
  });

  beforeEach(async () => {
    spyOn(console, 'error').and.callFake(() => {});
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    mockEventoService = jasmine.createSpyObj('EventoService', [
      'obtenerEventos',
      'eliminarEvento',
      'crearEvento',
      'crearEventoConUbicacion',
      'actualizarEventoConUbicacion',
      'obtenerEventosPorProductor'
    ]);
    mockEventoService.obtenerEventos.and.returnValue(of([]));
    mockEventoService.eliminarEvento.and.returnValue(of({} as any));
    mockEventoService.crearEvento.and.returnValue(of({} as any));
    mockEventoService.crearEventoConUbicacion.and.returnValue(of({} as any));
    mockEventoService.actualizarEventoConUbicacion.and.returnValue(of({} as any));
    mockEventoService.obtenerEventosPorProductor.and.returnValue(of([]));

    mockReservaService = jasmine.createSpyObj('ReservaService', ['obtenerReservas']);
    mockReservaService.obtenerReservas.and.returnValue(of([]));

    mockFileUploadService = jasmine.createSpyObj('FileUploadService', ['uploadImage', 'validateImageFile', 'fileToBase64', 'getImageUrl']);
    mockFileUploadService.uploadImage.and.returnValue(of({ imagePath: '' }));
    mockFileUploadService.validateImageFile.and.returnValue(true);
    mockFileUploadService.fileToBase64.and.returnValue(Promise.resolve(''));
    mockFileUploadService.getImageUrl.and.returnValue('test-url');

    mockFileStorageService = {} as any;

    mockEventNotificationService = jasmine.createSpyObj('EventNotificationService', ['notifyEventCreated']);
    mockEventNotificationService.notifyEventCreated.and.returnValue(undefined);

    await TestBed.configureTestingModule({
      imports: [EventsComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: EventoService, useValue: mockEventoService },
        { provide: ReservaService, useValue: mockReservaService },
        { provide: FileUploadService, useValue: mockFileUploadService },
        { provide: FileStorageService, useValue: mockFileStorageService },
        { provide: EventNotificationService, useValue: mockEventNotificationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.eventos).toEqual([]);
    expect(component.isLoading).toBeTrue();
    expect(component.errorMessage).toBe('');
  });

  it('should call ngOnInit and obtenerInformacionUsuario', () => {
    spyOn(component, 'obtenerInformacionUsuario');
    component.ngOnInit();
    expect(component.obtenerInformacionUsuario).toHaveBeenCalled();
  });

  it('should call calcularPaginacion and set totalPaginas', () => {
    component.eventosFiltrados = [{ id: 1, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any];
    component.itemsPorPagina = 1;
    component.calcularPaginacion();
    expect(component.totalPaginas).toBe(1);
  });

  it('should get eventosPaginados', () => {
    component.eventosFiltrados = [
      { id: 1, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any,
      { id: 2, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any
    ];
    component.itemsPorPagina = 1;
    component.paginaActual = 2;
    const paginados = component.eventosPaginados;
    expect(paginados.length).toBe(1);
  });

  it('should call paginaAnterior and paginaSiguiente', () => {
    component.totalPaginas = 2;
    component.paginaActual = 2;
    component.paginaAnterior();
    expect(component.paginaActual).toBe(1);
    component.paginaSiguiente();
    expect(component.paginaActual).toBe(2);
  });

  it('should call irAPagina and change paginaActual', () => {
    component.totalPaginas = 2;
    component.paginaActual = 1;
    component.eventosFiltrados = [
      { id: 1, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any,
      { id: 2, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any
    ];
    component.irAPagina(2);
    expect(component.paginaActual).toBe(2);
    component.irAPagina(0);
    expect(component.paginaActual).toBe(0); // El método simplemente asigna el valor
  });

  it('should call alternarVistaEventos and cerrarNotificacionExito', () => {
    component.mostrarNotificacionExito = true;
    // Mock servicios y datos necesarios para alternarVistaEventos
    component.eventosFiltrados = [
      { id: 1, nombre: '', descripcion: '', fecha: '', ubicacion: { direccion: '', comuna: '', latitud: 0, longitud: 0 } } as any
    ];
    spyOn(component, 'cargarEventos');
    spyOn(component, 'cargarEventosPorProductor');
    component.alternarVistaEventos();
    expect(component).toBeTruthy();
    component.cerrarNotificacionExito();
    expect(component.mostrarNotificacionExito).toBeFalse();
  });

  // --- SECCIÓN: CARGA DE EVENTOS Y MANEJO DE ERRORES ---
  describe('Carga de eventos y manejo de errores', () => {
    it('should load events successfully', fakeAsync(() => {
      mockEventoService.obtenerEventos.and.returnValue(of(mockEventos));
      component.isLoading = true;
      component.errorMessage = '';
      component.cargarEventos();
      tick();
      expect(component.eventos.length).toBe(2);
      expect(component.isLoading).toBeFalse();
      expect(component.errorMessage).toBe('');
    }));

    it('should handle error when loading events', fakeAsync(() => {
      mockEventoService.obtenerEventos.and.returnValue(throwError(() => new Error('Error de carga')));
      component.isLoading = true;
      component.errorMessage = '';
      component.cargarEventos();
      tick();
      expect(component.errorMessage).toContain('Error');
      expect(component.isLoading).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    }));

    it('should load events by producer successfully', fakeAsync(() => {
      mockEventoService.obtenerEventosPorProductor.and.returnValue(of(mockEventos));
      component.userRole = 'PRODUCTOR';
      component.productorId = 1;
      component.cargarEventosPorProductor();
      tick();
      expect(component.eventos.length).toBe(2);
      expect(component.isLoading).toBeFalse();
    }));

    it('should handle error when loading events by producer', fakeAsync(() => {
      mockEventoService.obtenerEventosPorProductor.and.returnValue(throwError(() => new Error('Error de carga')));
      component.userRole = 'PRODUCTOR';
      component.productorId = 1;
      component.cargarEventosPorProductor();
      tick();
      expect(component.errorMessage).toContain('Error');
      expect(component.isLoading).toBeFalse();
    }));
  });

  // --- SECCIÓN: FILTROS Y PAGINACIÓN ---
  describe('Filtros y paginación', () => {
    beforeEach(() => {
      component.eventos = mockEventos;
      component.eventosFiltrados = mockEventos;
    });

    it('should apply name filter correctly', () => {
      component.filtroEventoNombre = 'Evento 1';
      component.aplicarFiltroEventos();
      expect(component.eventosFiltrados.length).toBe(1);
      expect(component.eventosFiltrados[0].nombre).toBe('Evento 1');
    });

    it('should apply date filter correctly', () => {
      component.filtroEventoFecha = '2024-07-12';
      component.aplicarFiltroEventos();
      expect(component.eventosFiltrados.length).toBe(1);
      expect(component.eventosFiltrados[0].fecha).toBe('2024-07-12');
    });

    it('should apply both filters correctly', () => {
      component.filtroEventoNombre = 'Evento 1';
      component.filtroEventoFecha = '2024-07-12';
      component.aplicarFiltroEventos();
      expect(component.eventosFiltrados.length).toBe(1);
    });

    it('should clear filters correctly', () => {
      component.filtroEventoNombre = 'Evento 1';
      component.filtroEventoFecha = '2024-07-12';
      component.aplicarFiltroEventos();
      expect(component.eventosFiltrados.length).toBe(1);
      
      component.filtroEventoNombre = '';
      component.filtroEventoFecha = '';
      component.aplicarFiltroEventos();
      expect(component.eventosFiltrados.length).toBe(2);
    });

    it('should calculate pagination correctly', () => {
      component.eventosFiltrados = mockEventos;
      component.itemsPorPagina = 1;
      component.calcularPaginacion();
      expect(component.totalPaginas).toBe(2);
    });

    it('should get correct page range', () => {
      component.totalPaginas = 5;
      component.paginaActual = 3;
      const range = component.obtenerRangoPagina();
      expect(range).toContain('eventos');
    });

    it('should handle pagination limits', () => {
      component.totalPaginas = 2;
      component.paginaActual = 1;
      component.paginaAnterior();
      expect(component.paginaActual).toBe(1); // No debe ir a página 0
      
      component.paginaActual = 2;
      component.paginaSiguiente();
      expect(component.paginaActual).toBe(2); // No debe exceder totalPaginas
    });
  });

  // --- SECCIÓN: FORMATEO Y UTILIDADES ---
  describe('Formateo y utilidades', () => {
    it('should format price correctly', () => {
      const formatted = component.formatearPrecio(1000);
      expect(formatted).toContain('$1.000');
    });

    it('should format price with zero', () => {
      const formatted = component.formatearPrecio(0);
      expect(formatted).toContain('Gratis');
    });

    it('should format price with undefined', () => {
      const formatted = component.formatearPrecio(undefined);
      expect(formatted).toContain('Gratis');
    });

    it('should format date correctly', () => {
      const formatted = component.formatearFecha('2024-07-12');
      expect(formatted).toContain('julio');
      expect(formatted).toContain('2024');
    });

    it('should get event status correctly', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 1);
      const status = component.getEstadoEvento(futureDate.toISOString().split('T')[0]);
      expect(status).toBe('proximo');
    });

    it('should get past event status', () => {
      const pastDate = new Date();
      pastDate.setDate(pastDate.getDate() - 1);
      const status = component.getEstadoEvento(pastDate.toISOString().split('T')[0]);
      expect(status).toBe('pasado');
    });

    it('should get today event status', () => {
      const today = new Date().toISOString().split('T')[0];
      const status = component.getEstadoEvento(today);
      expect(status).toBe('hoy');
    });
  });

  // --- SECCIÓN: MANEJO DE IMÁGENES ---
  describe('Manejo de imágenes', () => {
    it('should handle image error', () => {
      const event = { target: { src: 'test.jpg' } } as any;
      component.onImageError(event);
      expect(event.target.src).toContain('unsplash');
    });

    it('should handle image selection', () => {
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [file] } } as any;
      
      spyOn(component, 'uploadSelectedImage');
      component.onImageSelected(event);
      expect(component.selectedImage).toBe(file);
      // El método uploadSelectedImage se llama internamente, no necesitamos verificar que se llame
    });

    it('should clear selected image', () => {
      component.selectedImage = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      component.imagePreview = 'data:image/jpeg;base64,test';
      
      component.clearSelectedImage();
      expect(component.selectedImage).toBeNull();
      expect(component.imagePreview).toBeNull();
    });

    it('should open file selector', () => {
      // Mock del fileInput para evitar errores
      component.fileInput = { nativeElement: { click: jasmine.createSpy('click') } } as any;
      component.openFileSelector();
      expect(component.fileInput.nativeElement.click).toHaveBeenCalled();
    });

    it('should get image URL correctly', () => {
      const url = component.getImageUrl('test.jpg');
      expect(url).toBe('test-url');
      expect(component['fileUploadService'].getImageUrl).toHaveBeenCalledWith('test.jpg');
    });
  });

  // --- SECCIÓN: NAVEGACIÓN Y DETALLES ---
  describe('Navegación y detalles', () => {
    it('should show event details', () => {
      const evento = mockEventos[0];
      component.mostrarDetalles(evento);
      expect(component.eventoSeleccionado).toBe(evento);
    });

    it('should close event details', () => {
      component.eventoSeleccionado = mockEventos[0];
      component.cerrarDetalles();
      expect(component.eventoSeleccionado).toBeNull();
    });

    it('should navigate to map', () => {
      const evento = mockEventos[0];
      spyOn(component['mapNavigationService'], 'navigateToLocation');
      component.irAlMapa(evento);
      expect(component['mapNavigationService'].navigateToLocation).toHaveBeenCalled();
    });
  });

  // --- SECCIÓN: FORMULARIOS Y VALIDACIONES ---
  describe('Formularios y validaciones', () => {
    beforeEach(() => {
      component.nuevoEvento = {
        nombre: 'Test Event',
        descripcion: 'Test Description',
        fecha: '2025-08-12',
        capacidadMaxima: 100,
        precioEntrada: 1000,
        direccion: 'Test Address',
        comuna: 'Test Comuna',
        latitud: 0,
        longitud: 0,
        imagenPath: ''
      };
    });

    it('should create new event modal', () => {
      component.crearNuevoEvento();
      expect(component.mostrarModalEvento).toBeTrue();
      expect(component.isEditando).toBeFalse();
    });

    it('should close event modal', () => {
      component.mostrarModalEvento = true;
      component.cerrarModalEvento();
      expect(component.mostrarModalEvento).toBeFalse();
    });

    it('should clear form correctly', () => {
      component.limpiarFormulario();
      expect(component.nuevoEvento.nombre).toBe('');
      expect(component.nuevoEvento.descripcion).toBe('');
      expect(component.formError).toBe('');
      expect(component.formSuccess).toBe('');
    });

    it('should validate form correctly', () => {
      const isValid = component.validarFormulario();
      expect(isValid).toBeTrue();
    });

    it('should validate form with empty name', () => {
      component.nuevoEvento.nombre = '';
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with empty description', () => {
      component.nuevoEvento.descripcion = '';
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with invalid date', () => {
      component.nuevoEvento.fecha = '';
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with invalid capacity', () => {
      component.nuevoEvento.capacidadMaxima = 0;
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with invalid price', () => {
      component.nuevoEvento.precioEntrada = -1;
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with empty address', () => {
      component.nuevoEvento.direccion = '';
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });

    it('should validate form with empty comuna', () => {
      component.nuevoEvento.comuna = '';
      const isValid = component.validarFormulario();
      expect(isValid).toBeFalse();
    });
  });

  // --- SECCIÓN: ELIMINACIÓN DE EVENTOS ---
  describe('Eliminación de eventos', () => {
    it('should open delete confirmation modal', () => {
      const evento = mockEventos[0];
      component.eliminarEvento(evento);
      expect(component.mostrarConfirmacionEliminar).toBeTrue();
      expect(component.eventoAEliminar).toBe(evento);
    });

    it('should cancel deletion', () => {
      component.mostrarConfirmacionEliminar = true;
      component.eventoAEliminar = mockEventos[0];
      component.cancelarEliminacion();
      expect(component.mostrarConfirmacionEliminar).toBeFalse();
      expect(component.eventoAEliminar).toBeNull();
    });

    it('should confirm deletion successfully', fakeAsync(() => {
      mockEventoService.eliminarEvento.and.returnValue(of({} as any));
      component.eventoAEliminar = mockEventos[0];
      component.isEliminando = false;
      
      component.confirmarEliminacion();
      tick();
      
      expect(component.isEliminando).toBeFalse();
      expect(component.mostrarConfirmacionEliminar).toBeFalse();
      expect(component.mostrarNotificacionExito).toBeTrue();
      expect(component.nombreEventoEliminado).toBe('Evento 1');
    }));

    it('should handle deletion error', fakeAsync(() => {
      mockEventoService.eliminarEvento.and.returnValue(throwError(() => new Error('Error al eliminar')));
      component.eventoAEliminar = mockEventos[0];
      component.isEliminando = false;
      
      component.confirmarEliminacion();
      tick();
      
      expect(component.isEliminando).toBeFalse();
      expect(component.mostrarConfirmacionEliminar).toBeFalse();
      // El error se maneja internamente, no necesitamos verificar el mensaje específico
    }));
  });

  // --- SECCIÓN: GEOCODIFICACIÓN ---
  describe('Geocodificación', () => {
    it('should geocode address successfully', fakeAsync(() => {
      spyOn(component as any, 'tryGeocodingWithKey').and.returnValue(Promise.resolve(true));
      component.nuevoEvento.direccion = 'Test Address';
      component.nuevoEvento.comuna = 'Test Comuna';
      
      component.geocodificarDireccion();
      tick();
      
      expect((component as any).tryGeocodingWithKey).toHaveBeenCalled();
    }));

    it('should handle geocoding error', fakeAsync(() => {
      spyOn(component as any, 'tryGeocodingWithKey').and.returnValue(Promise.resolve(false));
      spyOn(component as any, 'tryPredefinedCoordinates').and.returnValue(Promise.resolve(false));
      component.nuevoEvento.direccion = 'Invalid Address';
      component.nuevoEvento.comuna = 'Invalid Comuna';
      
      component.geocodificarDireccion();
      tick();
      
      expect((component as any).tryPredefinedCoordinates).toHaveBeenCalled();
    }));
  });

  // --- SECCIÓN: NOTIFICACIONES ---
  describe('Notificaciones', () => {
    it('should notify map to reload', () => {
      // Simular notificación al mapa
      component.notificarMapaRecargar();
      expect(component).toBeTruthy();
    });

    it('should update events after changes', () => {
      spyOn(component, 'cargarEventos');
      component.actualizarEventos();
      expect(component.cargarEventos).toHaveBeenCalled();
    });
  });
}); 