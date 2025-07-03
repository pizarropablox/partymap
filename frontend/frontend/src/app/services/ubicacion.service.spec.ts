import { TestBed } from '@angular/core/testing';
import { UbicacionService } from './ubicacion.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';

describe('UbicacionService', () => {
  let service: UbicacionService;
  let httpMock: HttpTestingController;

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
    const mockResponse = [
      { id: 1, nombre: 'Ubicación A', direccion: 'Calle 1', comuna: 'Santiago', latitud: -33.4, longitud: -70.6 }
    ];

    service.getAllUbicaciones().subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].nombre).toBe('Ubicación A');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/ubicacion/buscar`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
