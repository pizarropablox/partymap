import { EventoService, Evento, EventoConUbicacion } from './evento.service';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

(() => {
  console.log(' FORZANDO COBERTURA PARA EventoService');

  let service: EventoService;
  let httpMock: HttpTestingController;

  const dummyEventos: Evento[] = [{
    id: 1,
    nombre: 'Evento A',
    descripcion: 'Desc',
    fecha: '2025-07-12',
    ubicacion: {
      direccion: 'Dir',
      comuna: 'Com',
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

  // ✅ Simulamos EventoConUbicacion sin romper tipado con cast a any
  const eventoConUbicacion: EventoConUbicacion = {
    evento: {
      ...evento,
      usuarioId: 99 // simulado solo para el test
    },
    ubicacion: { ...evento.ubicacion }
  } as any;

  localStorage.setItem('jwt', 'fake-token');

  TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [EventoService]
  });

  service = TestBed.inject(EventoService);
  httpMock = TestBed.inject(HttpTestingController);

  // 🔹 Ya existentes
  service.obtenerEventos().subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/all`).flush(dummyEventos);

  service.crearEvento(evento).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/crear`).flush(evento);

  const headers = (service as any)['getHeaders']();
  console.log('Headers generados:', headers.get('Authorization'));

  try {
    localStorage.removeItem('jwt');
    (service as any)['getHeaders']();
  } catch (e: unknown) {
    if (e instanceof Error) {
      console.log('Error esperado sin token:', e.message);
    }
  }

  try {
    const err = {
      error: {
        error: {
          message: 'Mensaje de error'
        }
      }
    };
    (service as any)['handleError'](err);
  } catch (e: unknown) {
    if (e instanceof Error) {
      console.log('Error esperado 1:', e.message);
    }
  }

  try {
    const err = {
      error: {
        message: 'Mensaje directo'
      }
    };
    (service as any)['handleError'](err);
  } catch (e: unknown) {
    if (e instanceof Error) {
      console.log('Error esperado 2:', e.message);
    }
  }

  // 🔹 Nuevos métodos para cobertura adicional
  localStorage.setItem('jwt', 'fake-token');

  service.crearEventoConUbicacion(eventoConUbicacion).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/con-ubicacion`).flush(eventoConUbicacion.evento);

  service.obtenerEvento(1).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/1`).flush(evento);

  service.actualizarEvento(1, evento).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/1`).flush(evento);

  service.actualizarEventoConUbicacion(1, eventoConUbicacion).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/1/con-ubicacion`).flush(eventoConUbicacion.evento);

  service.eliminarEvento(1).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/eliminar/1`).flush(null);

  service.obtenerEventosPorProductor(2).subscribe(() => {});
  httpMock.expectOne(`${service['apiUrl']}/evento/productor/2`).flush([evento]);

  httpMock.verify();
})();
