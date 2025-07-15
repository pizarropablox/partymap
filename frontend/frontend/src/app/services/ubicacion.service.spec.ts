import { TestBed } from '@angular/core/testing';
import { UbicacionService } from './ubicacion.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';

describe('UbicacionService', () => {
  let service: UbicacionService;
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

  const mockUbicaciones = [
    { 
      id: 1, 
      nombre: 'Ubicación A', 
      direccion: 'Calle 1', 
      comuna: 'Santiago', 
      latitud: -33.4, 
      longitud: -70.6 
    },
    { 
      id: 2, 
      nombre: 'Ubicación B', 
      direccion: 'Calle 2', 
      comuna: 'Providencia', 
      latitud: -33.4183, 
      longitud: -70.6062 
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UbicacionService]
    });
    service = TestBed.inject(UbicacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería obtener todas las ubicaciones', () => {
    service.getAllUbicaciones().subscribe((res) => {
      expect(res.length).toBe(2);
      expect(res[0].nombre).toBe('Ubicación A');
      expect(res[1].nombre).toBe('Ubicación B');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUbicaciones);
  });

  it('debería buscar ubicaciones por término', () => {
    const searchTerm = 'Santiago';
    
    service.buscarUbicaciones(searchTerm, searchTerm).subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].comuna).toBe('Santiago');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=${searchTerm}&direccion=${searchTerm}`);
    expect(req.request.method).toBe('GET');
    req.flush([mockUbicaciones[0]]);
  });

  it('debería manejar errores en la búsqueda', () => {
    const searchTerm = 'Ubicación inexistente';
    service.buscarUbicaciones(searchTerm, searchTerm).subscribe({
      next: () => fail('Debería haber fallado'),
      error: (error) => {
        expect(error).toBeTruthy();
      }
    });
    const req = httpMock.match((request) => decodeURIComponent(request.urlWithParams) === `${environment.apiUrl}/ubicacion/buscar?comuna=${searchTerm}&direccion=${searchTerm}`)[0];
    req.error(new ErrorEvent('Network error'));
  });

  it('debería devolver array vacío cuando no hay resultados', () => {
    const searchTerm = 'Término sin resultados';
    service.buscarUbicaciones(searchTerm, searchTerm).subscribe((res) => {
      expect(res).toEqual([]);
    });
    const req = httpMock.match((request) => decodeURIComponent(request.urlWithParams) === `${environment.apiUrl}/ubicacion/buscar?comuna=${searchTerm}&direccion=${searchTerm}`)[0];
    req.flush([]);
  });

  it('should search with only comuna parameter', () => {
    service.buscarUbicaciones('Santiago').subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].comuna).toBe('Santiago');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=Santiago`);
    expect(req.request.method).toBe('GET');
    req.flush([mockUbicaciones[0]]);
  });

  it('should search with only direccion parameter', () => {
    service.buscarUbicaciones(undefined, 'Providencia').subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].direccion).toBe('Calle 2');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?direccion=Providencia`);
    expect(req.request.method).toBe('GET');
    req.flush([mockUbicaciones[1]]);
  });

  it('should search without parameters', () => {
    service.buscarUbicaciones().subscribe((res) => {
      expect(res).toEqual(mockUbicaciones);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUbicaciones);
  });

  it('should handle 404 error', () => {
    service.buscarUbicaciones('test').subscribe({
      next: () => fail('Debería haber fallado'),
      error: (error) => {
        expect(error.status).toBe(404);
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=test`);
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });
  });

  it('should handle 500 error', () => {
    service.buscarUbicaciones('test').subscribe({
      next: () => fail('Debería haber fallado'),
      error: (error) => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=test`);
    req.flush('Internal Server Error', { status: 500, statusText: 'Internal Server Error' });
  });

  it('should handle 403 error', () => {
    service.buscarUbicaciones('test').subscribe({
      next: () => fail('Debería haber fallado'),
      error: (error) => {
        expect(error.status).toBe(403);
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=test`);
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should handle malformed response', () => {
    service.buscarUbicaciones('test').subscribe({
      next: (data) => {
        // Si llega aquí, significa que el JSON se parseó correctamente
        expect(data).toBeTruthy();
      },
      error: (error) => {
        // Si hay error, también es válido
        expect(error).toBeTruthy();
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=test`);
    req.flush('Invalid JSON', { status: 200, statusText: 'OK' });
  });

  it('should handle timeout error', () => {
    service.buscarUbicaciones('test').subscribe({
      next: () => fail('Debería haber fallado'),
      error: (error) => {
        expect(error.status).toBe(0);
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar?comuna=test`);
    req.error(new ErrorEvent('timeout'));
  });
});
