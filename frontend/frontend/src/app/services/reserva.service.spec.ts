import { TestBed } from '@angular/core/testing';
import { ReservaService } from './reserva.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';

describe('ReservaService', () => {
  let service: ReservaService;
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

  const mockUsuario = {
    id: 1,
    nombre: 'Usuario Test',
    email: 'test@example.com',
    tipoUsuario: 'CLIENTE'
  };

  const mockReserva = {
    id: 1,
    eventoId: 1,
    usuarioId: 1,
    cantidadEntradas: 2,
    fechaReserva: '2025-07-12',
    estado: 'CONFIRMADA'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReservaService]
    });
    service = TestBed.inject(ReservaService);
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

  it('debería obtener el usuario actual', () => {
    service.obtenerUsuarioActual().subscribe(usuario => {
      expect(usuario).toEqual(mockUsuario);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/usuario/current`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUsuario);
  });

  it('debería crear una reserva', () => {
    const nuevaReserva = {
      eventoId: 1,
      cantidad: 2,
      usuarioId: 1,
      comentarios: 'Reserva de prueba'
    };

    service.crearReserva(nuevaReserva).subscribe(reserva => {
      expect(reserva).toEqual(mockReserva);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/reserva/crear`);
    expect(req.request.method).toBe('POST');
    req.flush(mockReserva);
  });

  it('debería obtener reservas del usuario', () => {
    service.obtenerReservasUsuario().subscribe(reservas => {
      expect(reservas).toEqual([mockReserva]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/reserva/usuario`);
    expect(req.request.method).toBe('GET');
    req.flush([mockReserva]);
  });

  it('debería cancelar una reserva', () => {
    const reservaId = 1;
    service.cancelarReserva(reservaId).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/reserva/${reservaId}/cancelar`);
    expect(req.request.method).toBe('PUT');
    req.flush({ success: true });
  });

  it('debería manejar errores cuando no hay token', () => {
    localStorage.removeItem('jwt');
    
    // El servicio debería lanzar un error cuando no hay token
    expect(() => {
      service.obtenerReservasUsuario().subscribe({
        next: () => fail('Debería haber fallado'),
        error: (error) => {
          expect(error.message).toContain('No hay token de autenticación disponible');
        }
      });
    }).toThrow();
  });
});
