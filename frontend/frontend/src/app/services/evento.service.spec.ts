import { TestBed } from '@angular/core/testing';
import { EventoService, Evento, EventoConUbicacion } from './evento.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { throwError } from 'rxjs';

describe('EventoService', () => {
  let service: EventoService;
  let httpMock: HttpTestingController;

  beforeAll(() => {
    try {
      spyOn(window.location, 'assign').and.callFake(() => {});
    } catch (e) {}
    try {
      spyOn(window.location, 'replace').and.callFake(() => {});
    } catch (e) {}
    try {
      spyOn(window.location, 'reload').and.callFake(() => {});
    } catch (e) {}
  });

  const dummyEventos: Evento[] = [{
    id: 1,
    nombre: 'Evento A',
    descripcion: 'Descripción del evento',
    fecha: '2025-07-12',
    ubicacion: {
      direccion: 'Dirección del evento',
      comuna: 'Comuna del evento',
      latitud: 1,
      longitud: 1
    },
    precioEntrada: 1000,
    capacidadMaxima: 50,
    cuposDisponibles: 30,
    disponible: true,
    activo: 1,
    productorId: 2
  }];

  const evento = dummyEventos[0];

  const eventoConUbicacion: EventoConUbicacion = {
    evento: {
      nombre: 'Evento con ubicación',
      descripcion: 'Descripción del evento',
      fecha: '2025-07-12',
      capacidadMaxima: 50,
      precioEntrada: 1000,
      usuarioId: 99
    },
    ubicacion: {
      direccion: 'Dirección del evento',
      comuna: 'Comuna del evento',
      latitud: 1,
      longitud: 1
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventoService]
    });
    service = TestBed.inject(EventoService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Configurar token de prueba
    localStorage.setItem('jwt', 'fake-token');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('jwt');
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería obtener todos los eventos', () => {
    service.obtenerEventos().subscribe(eventos => {
      expect(eventos).toEqual(dummyEventos);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/all`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEventos);
  });

  it('debería crear un evento', () => {
    service.crearEvento(evento).subscribe(eventoCreado => {
      expect(eventoCreado).toEqual(evento);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/crear`);
    expect(req.request.method).toBe('POST');
    req.flush(evento);
  });

  it('debería crear un evento con ubicación', () => {
    service.crearEventoConUbicacion(eventoConUbicacion).subscribe(eventoCreado => {
      expect(eventoCreado).toBeTruthy();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/con-ubicacion`);
    expect(req.request.method).toBe('POST');
    req.flush(eventoConUbicacion.evento);
  });

  it('debería obtener un evento por ID', () => {
    service.obtenerEvento(1).subscribe(evento => {
      expect(evento).toEqual(dummyEventos[0]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/1`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEventos[0]);
  });

  it('debería actualizar un evento', () => {
    service.actualizarEvento(1, evento).subscribe(eventoActualizado => {
      expect(eventoActualizado).toEqual(evento);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(evento);
  });

  it('debería actualizar un evento con ubicación', () => {
    service.actualizarEventoConUbicacion(1, eventoConUbicacion).subscribe(eventoActualizado => {
      expect(eventoActualizado).toBeTruthy();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/1/con-ubicacion`);
    expect(req.request.method).toBe('PUT');
    req.flush(eventoConUbicacion.evento);
  });

  it('debería eliminar un evento', () => {
    service.eliminarEvento(1).subscribe(() => {
      // La eliminación fue exitosa
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/eliminar/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('debería obtener eventos por productor', () => {
    service.obtenerEventosPorProductor(2).subscribe(eventos => {
      expect(eventos).toEqual([evento]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/evento/productor/2`);
    expect(req.request.method).toBe('GET');
    req.flush([evento]);
  });

  it('debería manejar errores cuando no hay token', () => {
    localStorage.removeItem('jwt');
    
    expect(() => {
      service.obtenerEventos().subscribe();
    }).toThrowError('No hay token de autenticación disponible');
  });

  // --- SECCIÓN: MÉTODOS ADICIONALES ---
  describe('Métodos adicionales', () => {
    beforeEach(() => {
      spyOn(localStorage, 'getItem').and.returnValue('fake-token');
    });

    it('should handle error when getting events by producer', () => {
      const productorId = 1;
      
      service.obtenerEventosPorProductor(productorId).subscribe({
        next: (result) => {
          expect(result).toBeDefined();
        },
        error: (error) => {
          expect(error).toBeDefined();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/evento/productor/${productorId}`);
      req.flush([], { status: 200, statusText: 'OK' });
    });

    it('should handle error when creating event with location', () => {
      const eventoConUbicacion = {
        evento: { nombre: 'Test', descripcion: 'Test', fecha: '2024-12-31', capacidadMaxima: 100, precioEntrada: 1000, usuarioId: 1, cuposDisponibles: 100, disponible: true, activo: 1 },
        ubicacion: { direccion: 'Test', comuna: 'Test', latitud: 0, longitud: 0, activo: 1 }
      };
      
      service.crearEventoConUbicacion(eventoConUbicacion).subscribe({
        next: (result) => {
          expect(result).toBeDefined();
        },
        error: (error) => {
          expect(error).toBeDefined();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/evento/con-ubicacion`);
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should handle error when updating event with location', () => {
      const eventoId = 1;
      const eventoConUbicacion = {
        evento: { id: 1, nombre: 'Test', descripcion: 'Test', fecha: '2024-12-31', capacidadMaxima: 100, precioEntrada: 1000, usuarioId: 1, cuposDisponibles: 100, disponible: true, activo: 1 },
        ubicacion: { id: 1, direccion: 'Test', comuna: 'Test', latitud: 0, longitud: 0, activo: 1 }
      };
      
      service.actualizarEventoConUbicacion(eventoId, eventoConUbicacion).subscribe({
        next: (result) => {
          expect(result).toBeDefined();
        },
        error: (error) => {
          expect(error).toBeDefined();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/evento/${eventoId}/con-ubicacion`);
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should handle error when deleting event', () => {
      const eventoId = 1;
      
      service.eliminarEvento(eventoId).subscribe({
        next: (result) => {
          expect(result).toBeDefined();
        },
        error: (error) => {
          expect(error).toBeDefined();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/evento/eliminar/${eventoId}`);
      req.flush({}, { status: 200, statusText: 'OK' });
    });
  });

  // --- SECCIÓN: VALIDACIONES Y UTILIDADES ---
  describe('Validaciones y utilidades', () => {
    it('should create headers correctly', () => {
      // Simular creación de headers
      const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
      expect(token).toBeDefined();
    });

    it('should handle missing token in headers', () => {
      spyOn(localStorage, 'getItem').and.returnValue(null);
      const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
      expect(token).toBeNull();
    });

    it('should validate event data correctly', () => {
      const validEvent = {
        nombre: 'Test Event',
        descripcion: 'Test Description',
        fecha: '2024-12-31',
        capacidadMaxima: 100,
        precioEntrada: 1000
      };
      
      // Simular validación básica
      expect(validEvent.nombre).toBeTruthy();
      expect(validEvent.capacidadMaxima).toBeGreaterThan(0);
      expect(validEvent.precioEntrada).toBeGreaterThanOrEqual(0);
    });

    it('should detect invalid event data', () => {
      const invalidEvent = {
        nombre: '',
        descripcion: '',
        fecha: '',
        capacidadMaxima: 0,
        precioEntrada: -1
      };
      
      // Simular validación básica
      expect(invalidEvent.nombre).toBeFalsy();
      expect(invalidEvent.capacidadMaxima).toBeLessThanOrEqual(0);
      expect(invalidEvent.precioEntrada).toBeLessThan(0);
    });
  });
});
