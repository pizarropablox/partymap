import { TestBed } from '@angular/core/testing';
import { EndpointsService, EndpointRequest, EndpointResponse } from './endpoints.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('EndpointsService', () => {
  let service: EndpointsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EndpointsService]
    });
    
    service = TestBed.inject(EndpointsService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Mock localStorage
    spyOn(localStorage, 'getItem').and.returnValue('mock-token');
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  describe('callEndpoint', () => {
    it('debería hacer una petición GET exitosa', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test',
        params: { id: 1, name: 'test' }
      };

      const mockResponse = { success: true, data: 'test data', timestamp: '2024-01-01T00:00:00Z' };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => 
        req.method === 'GET' && 
        req.url.includes('/test') &&
        req.headers.has('Authorization')
      );
      
      expect(req.request.params.get('id')).toBe('1');
      expect(req.request.params.get('name')).toBe('test');
      req.flush(mockResponse);
    });

    it('debería hacer una petición POST exitosa', () => {
      const request: EndpointRequest = {
        method: 'POST',
        path: '/test',
        body: { name: 'test', value: 123 }
      };

      const mockResponse = { success: true, data: 'created', timestamp: '2024-01-01T00:00:00Z' };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => 
        req.method === 'POST' && 
        req.url.includes('/test')
      );
      
      expect(req.request.body).toEqual({ name: 'test', value: 123 });
      req.flush(mockResponse);
    });

    it('debería hacer una petición PUT exitosa', () => {
      const request: EndpointRequest = {
        method: 'PUT',
        path: '/test/1',
        body: { name: 'updated', value: 456 }
      };

      const mockResponse = { success: true, data: 'updated', timestamp: '2024-01-01T00:00:00Z' };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => 
        req.method === 'PUT' && 
        req.url.includes('/test/1')
      );
      
      expect(req.request.body).toEqual({ name: 'updated', value: 456 });
      req.flush(mockResponse);
    });

    it('debería hacer una petición DELETE exitosa', () => {
      const request: EndpointRequest = {
        method: 'DELETE',
        path: '/test/1'
      };

      const mockResponse = { success: true, data: 'deleted', timestamp: '2024-01-01T00:00:00Z' };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => 
        req.method === 'DELETE' && 
        req.url.includes('/test/1')
      );
      
      req.flush(mockResponse);
    });

    it('debería lanzar error cuando no hay token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);

      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      expect(() => service.callEndpoint(request)).toThrowError('No se encontró token de autenticación');
    });

    it('debería lanzar error para método HTTP no soportado', () => {
      const request: EndpointRequest = {
        method: 'PATCH' as any,
        path: '/test'
      };

      expect(() => service.callEndpoint(request)).toThrowError('Método HTTP no soportado: PATCH');
    });

    it('debería manejar parámetros nulos en GET', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test',
        params: { id: 1, name: null, value: undefined, valid: 'test' }
      };

      service.callEndpoint(request).subscribe();

      const req = httpMock.expectOne(req => req.method === 'GET');
      expect(req.request.params.get('id')).toBe('1');
      expect(req.request.params.get('name')).toBeNull();
      expect(req.request.params.get('value')).toBeNull();
      expect(req.request.params.get('valid')).toBe('test');
      req.flush({});
    });
  });

  describe('getAuthHeaders', () => {
    it('debería retornar headers con token', () => {
      const headers = service.getAuthHeaders();
      
      expect(headers.get('Authorization')).toBe('Bearer mock-token');
      expect(headers.get('Content-Type')).toBe('application/json');
    });

    it('debería manejar cuando no hay token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);
      
      const headers = service.getAuthHeaders();
      
      expect(headers.get('Authorization')).toBe('Bearer null');
    });
  });

  describe('buildUrl', () => {
    it('debería construir URL correctamente', () => {
      const url = service.buildUrl('/test');
      expect(url).toContain('/test');
    });
  });

  describe('Reservas endpoints', () => {
    it('debería obtener cantidad mínima', () => {
      const mockResponse = { success: true, cantidad: 5, timestamp: '2024-01-01T00:00:00Z' };

      service.getCantidadMinima().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/reserva/cantidad-minima'));
      req.flush(mockResponse);
    });

    it('debería buscar reservas', () => {
      const criterios = { fecha: '2024-01-01', estado: 'activo' };
      const mockResponse = { success: true, reservas: [], timestamp: '2024-01-01T00:00:00Z' };

      service.buscarReservas(criterios).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/reserva/buscar'));
      expect(req.request.params.get('fecha')).toBe('2024-01-01');
      expect(req.request.params.get('estado')).toBe('activo');
      req.flush(mockResponse);
    });

    it('debería obtener estadísticas', () => {
      const mockResponse = { success: true, total: 100, activas: 80, timestamp: '2024-01-01T00:00:00Z' };

      service.getEstadisticas().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/reserva/estadisticas'));
      req.flush(mockResponse);
    });

    it('debería obtener estadísticas básicas', () => {
      const mockResponse = { success: true, total: 100, timestamp: '2024-01-01T00:00:00Z' };

      service.getEstadisticasBasicas().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/reserva/estadisticas-basicas'));
      req.flush(mockResponse);
    });

    it('debería obtener reservas por evento', () => {
      const eventoId = 1;
      const mockResponse = [{ id: 1, eventoId: 1 }];

      service.getReservasPorEvento(eventoId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes(`/reserva/evento/${eventoId}`));
      req.flush(mockResponse);
    });

    it('debería cancelar reserva', () => {
      const reservaId = 1;
      const mockResponse = { success: true };

      service.cancelarReserva(reservaId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes(`/reserva/${reservaId}/cancelar`));
      req.flush(mockResponse);
    });
  });

  describe('Usuarios endpoints', () => {
    it('debería obtener usuario actual', () => {
      const mockResponse = { id: 1, nombre: 'Test User' };

      service.getUsuarioActual().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/usuario/current'));
      req.flush(mockResponse);
    });

    it('debería obtener todos los usuarios', () => {
      const mockResponse = [{ id: 1, nombre: 'User 1' }, { id: 2, nombre: 'User 2' }];

      service.getAllUsuarios().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/usuario/all'));
      req.flush(mockResponse);
    });

    it('debería obtener productor por usuario', () => {
      const usuarioId = '123';
      const mockResponse = { id: 1, usuarioId: '123', nombre: 'Productor' };

      service.getProductorPorUsuario(usuarioId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/usuario/productor/${usuarioId}`);
      req.flush(mockResponse);
    });

    it('debería crear productor', () => {
      const productorData = { nombre: 'Nuevo Productor', email: 'test@test.com' };
      const mockResponse = { id: 1, ...productorData };

      service.crearProductor(productorData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/usuario/crear-productor'));
      expect(req.request.body).toEqual(productorData);
      req.flush(mockResponse);
    });

    it('debería actualizar usuario', () => {
      const usuarioId = 1;
      const userData = { nombre: 'Usuario Actualizado', email: 'updated@test.com' };
      const mockResponse = { id: usuarioId, ...userData };

      service.actualizarUsuario(usuarioId, userData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/usuario/actualizar/${usuarioId}`);
      expect(req.request.body).toEqual(userData);
      req.flush(mockResponse);
    });

    it('debería eliminar usuario', () => {
      const usuarioId = 1;
      const mockResponse = { success: true };

      service.eliminarUsuario(usuarioId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/usuario/eliminar/${usuarioId}`);
      req.flush(mockResponse);
    });
  });

  describe('Eventos endpoints', () => {
    it('debería obtener todos los eventos', () => {
      const mockResponse = [{ id: 1, nombre: 'Evento 1' }, { id: 2, nombre: 'Evento 2' }];

      service.getAllEventos().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/evento/all'));
      req.flush(mockResponse);
    });

    it('debería obtener eventos por usuario', () => {
      const productorId = 1;
      const mockResponse = [{ id: 1, productorId: 1, nombre: 'Evento 1' }];

      service.getEventosPorUsuario(productorId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes(`/evento/usuario/${productorId}`));
      req.flush(mockResponse);
    });

    it('debería crear evento', () => {
      const eventoData = { nombre: 'Nuevo Evento', fecha: '2024-01-01' };
      const mockResponse = { id: 1, ...eventoData };

      service.crearEvento(eventoData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/evento/crear'));
      expect(req.request.body).toEqual(eventoData);
      req.flush(mockResponse);
    });

    it('debería actualizar evento', () => {
      const eventoId = 1;
      const eventoData = { nombre: 'Evento Actualizado', fecha: '2024-01-01' };
      const mockResponse = { id: eventoId, ...eventoData };

      service.actualizarEvento(eventoId, eventoData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/evento/actualizar/${eventoId}`);
      expect(req.request.body).toEqual(eventoData);
      req.flush(mockResponse);
    });

    it('debería eliminar evento', () => {
      const eventoId = 1;
      const mockResponse = { success: true };

      service.eliminarEvento(eventoId).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/evento/eliminar/${eventoId}`);
      req.flush(mockResponse);
    });

    it('debería obtener estadísticas de eventos', () => {
      const mockResponse = { total: 50, activos: 30 };

      service.getEstadisticasEventos().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`http://localhost:8085/evento/mis-estadisticas`);
      req.flush(mockResponse);
    });
  });

  describe('Ubicaciones endpoints', () => {
    it('debería obtener todas las ubicaciones', () => {
      const mockResponse = [{ id: 1, direccion: 'Dirección 1' }, { id: 2, direccion: 'Dirección 2' }];

      service.getAllUbicaciones().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/ubicacion/all'));
      req.flush(mockResponse);
    });
  });

  describe('Estadísticas endpoints', () => {
    it('debería obtener estadísticas de usuarios', () => {
      const mockResponse = { success: true, total: 100, activos: 80 };

      service.getEstadisticasUsuarios().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url.includes('/usuario/estadisticas'));
      req.flush(mockResponse);
    });
  });

  describe('Error handling', () => {
    it('should handle HTTP errors gracefully', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      service.callEndpoint(request).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error).toBeDefined();
        }
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.error(new ErrorEvent('Network error'));
    });

    it('should handle 404 errors', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/not-found'
      };

      service.callEndpoint(request).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle 500 errors', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/server-error'
      };

      service.callEndpoint(request).subscribe({
        next: () => fail('Should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('URL building', () => {
    it('should build URL with base path', () => {
      const url = service.buildUrl('/api/test');
      expect(url).toContain('/api/test');
    });

    it('should build URL with different paths', () => {
      const paths = ['/test', '/api/users', '/events/123'];
      
      paths.forEach(path => {
        const url = service.buildUrl(path);
        expect(url).toContain(path);
      });
    });
  });

  describe('Headers management', () => {
    it('should include all required headers', () => {
      const headers = service.getAuthHeaders();
      
      expect(headers.has('Authorization')).toBeTrue();
      expect(headers.has('Content-Type')).toBeTrue();
    });

    it('should handle empty token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue('');
      
      const headers = service.getAuthHeaders();
      expect(headers.get('Authorization')).toBe('Bearer ');
    });

    it('should handle undefined token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(undefined);
      
      const headers = service.getAuthHeaders();
      expect(headers.get('Authorization')).toBe('Bearer undefined');
    });
  });

  describe('Request validation', () => {
    it('should validate request object structure', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      expect(request.method).toBeDefined();
      expect(request.path).toBeDefined();
    });

    it('should handle requests with empty body', () => {
      const request: EndpointRequest = {
        method: 'POST',
        path: '/test',
        body: {}
      };

      service.callEndpoint(request).subscribe();

      const req = httpMock.expectOne(req => req.method === 'POST');
      expect(req.request.body).toEqual({});
      req.flush({});
    });

    it('should handle requests with null body', () => {
      const request: EndpointRequest = {
        method: 'POST',
        path: '/test',
        body: null as any
      };

      service.callEndpoint(request).subscribe();

      const req = httpMock.expectOne(req => req.method === 'POST');
      expect(req.request.body).toBeNull();
      req.flush({});
    });
  });

  describe('Response handling', () => {
    it('should handle empty response', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual({} as any);
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.flush({});
    });

    it('should handle response with only success field', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual({ success: true, timestamp: '2024-01-01T00:00:00Z' });
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.flush({ success: true, timestamp: '2024-01-01T00:00:00Z' });
    });

    it('should handle response with data field', () => {
      const request: EndpointRequest = {
        method: 'GET',
        path: '/test'
      };

      const mockData = { id: 1, name: 'test' };

      service.callEndpoint(request).subscribe(response => {
        expect(response).toEqual({ success: true, data: mockData, timestamp: '2024-01-01T00:00:00Z' });
      });

      const req = httpMock.expectOne(req => req.method === 'GET');
      req.flush({ success: true, data: mockData, timestamp: '2024-01-01T00:00:00Z' });
    });
  });

  describe('Service lifecycle', () => {
    it('should be injectable', () => {
      expect(service).toBeDefined();
      expect(typeof service.callEndpoint).toBe('function');
      expect(typeof service.getAuthHeaders).toBe('function');
      expect(typeof service.buildUrl).toBe('function');
    });

    it('should maintain service state', () => {
      expect(service).toBe(TestBed.inject(EndpointsService));
    });
  });
});
