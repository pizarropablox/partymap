import { TestBed } from '@angular/core/testing';
import { MensajeService, Mensaje } from './mensaje.service';

describe('MensajeService', () => {
  let service: MensajeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MensajeService]
    });
    service = TestBed.inject(MensajeService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería tener observable mensaje$ definido', () => {
    expect(service.mensaje$).toBeTruthy();
  });

  describe('mostrarExito', () => {
    it('debería mostrar mensaje de éxito con valores por defecto', (done) => {
      const texto = 'Operación exitosa';
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('exito');
          expect(mensaje.titulo).toBe('Éxito');
          expect(mensaje.duracion).toBe(3000);
          done();
        }
      });

      service.mostrarExito(texto);
    });

    it('debería mostrar mensaje de éxito con valores personalizados', (done) => {
      const texto = 'Operación exitosa';
      const titulo = 'Título personalizado';
      const duracion = 5000;
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('exito');
          expect(mensaje.titulo).toBe(titulo);
          expect(mensaje.duracion).toBe(duracion);
          done();
        }
      });

      service.mostrarExito(texto, titulo, duracion);
    });
  });

  describe('mostrarError', () => {
    it('debería mostrar mensaje de error con valores por defecto', (done) => {
      const texto = 'Error en la operación';
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('error');
          expect(mensaje.titulo).toBe('Error');
          expect(mensaje.duracion).toBe(5000);
          done();
        }
      });

      service.mostrarError(texto);
    });

    it('debería mostrar mensaje de error con valores personalizados', (done) => {
      const texto = 'Error en la operación';
      const titulo = 'Error personalizado';
      const duracion = 10000;
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('error');
          expect(mensaje.titulo).toBe(titulo);
          expect(mensaje.duracion).toBe(duracion);
          done();
        }
      });

      service.mostrarError(texto, titulo, duracion);
    });
  });

  describe('mostrarAdvertencia', () => {
    it('debería mostrar mensaje de advertencia con valores por defecto', (done) => {
      const texto = 'Advertencia importante';
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('advertencia');
          expect(mensaje.titulo).toBe('Advertencia');
          expect(mensaje.duracion).toBe(4000);
          done();
        }
      });

      service.mostrarAdvertencia(texto);
    });

    it('debería mostrar mensaje de advertencia con valores personalizados', (done) => {
      const texto = 'Advertencia importante';
      const titulo = 'Advertencia personalizada';
      const duracion = 6000;
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('advertencia');
          expect(mensaje.titulo).toBe(titulo);
          expect(mensaje.duracion).toBe(duracion);
          done();
        }
      });

      service.mostrarAdvertencia(texto, titulo, duracion);
    });
  });

  describe('mostrarInfo', () => {
    it('debería mostrar mensaje informativo con valores por defecto', (done) => {
      const texto = 'Información importante';
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('info');
          expect(mensaje.titulo).toBe('Información');
          expect(mensaje.duracion).toBe(3000);
          done();
        }
      });

      service.mostrarInfo(texto);
    });

    it('debería mostrar mensaje informativo con valores personalizados', (done) => {
      const texto = 'Información importante';
      const titulo = 'Info personalizada';
      const duracion = 7000;
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.texto).toBe(texto);
          expect(mensaje.tipo).toBe('info');
          expect(mensaje.titulo).toBe(titulo);
          expect(mensaje.duracion).toBe(duracion);
          done();
        }
      });

      service.mostrarInfo(texto, titulo, duracion);
    });
  });

  describe('mostrarMensaje', () => {
    it('debería mostrar mensaje personalizado', (done) => {
      const mensajePersonalizado: Mensaje = {
        texto: 'Mensaje personalizado',
        tipo: 'info',
        titulo: 'Título personalizado',
        duracion: 2000
      };
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje).toEqual(mensajePersonalizado);
          done();
        }
      });

      service.mostrarMensaje(mensajePersonalizado);
    });

    it('debería mostrar mensaje sin duración (no auto-cerrar)', (done) => {
      const mensajeSinDuracion: Mensaje = {
        texto: 'Mensaje sin duración',
        tipo: 'exito',
        duracion: 0
      };
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          expect(mensaje.duracion).toBe(0);
          done();
        }
      });

      service.mostrarMensaje(mensajeSinDuracion);
    });
  });

  describe('cerrarMensaje', () => {
    it('debería cerrar el mensaje actual', (done) => {
      // Primero mostrar un mensaje
      service.mostrarExito('Test');
      
      // Luego cerrarlo
      service.cerrarMensaje();
      
      service.mensaje$.subscribe(mensaje => {
        expect(mensaje).toBeNull();
        done();
      });
    });

    it('debería cerrar mensaje después de mostrar uno', (done) => {
      let mensajeCount = 0;
      
      service.mensaje$.subscribe(mensaje => {
        mensajeCount++;
        if (mensajeCount === 1) {
          // Primer valor: null (valor inicial del BehaviorSubject)
          expect(mensaje).toBeNull();
        } else if (mensajeCount === 2) {
          // Segundo valor: mensaje mostrado
          expect(mensaje).toBeTruthy();
          service.cerrarMensaje();
        } else if (mensajeCount === 3) {
          // Tercer valor: null después de cerrar
          expect(mensaje).toBeNull();
          done();
        }
      });

      service.mostrarExito('Test');
    });
  });

  describe('mostrarConfirmacion', () => {
    it('debería mostrar confirmación y retornar Promise', async () => {
      spyOn(window, 'confirm').and.returnValue(true);
      
      const resultado = await service.mostrarConfirmacion('¿Estás seguro?');
      
      expect(resultado).toBeTrue();
      expect(window.confirm).toHaveBeenCalledWith('¿Estás seguro?');
    });

    it('debería mostrar confirmación con título personalizado', async () => {
      spyOn(window, 'confirm').and.returnValue(false);
      
      const resultado = await service.mostrarConfirmacion('¿Estás seguro?', 'Título personalizado');
      
      expect(resultado).toBeFalse();
      expect(window.confirm).toHaveBeenCalledWith('¿Estás seguro?');
    });

    it('debería manejar confirmación cancelada', async () => {
      spyOn(window, 'confirm').and.returnValue(false);
      
      const resultado = await service.mostrarConfirmacion('¿Continuar?');
      
      expect(resultado).toBeFalse();
    });
  });

  describe('comportamiento del observable', () => {
    it('debería emitir múltiples mensajes correctamente', (done) => {
      const mensajes: Mensaje[] = [];
      
      service.mensaje$.subscribe(mensaje => {
        if (mensaje) {
          mensajes.push(mensaje);
        }
        
        if (mensajes.length === 3) {
          expect(mensajes[0].tipo).toBe('exito');
          expect(mensajes[1].tipo).toBe('error');
          expect(mensajes[2].tipo).toBe('info');
          done();
        }
      });

      service.mostrarExito('Éxito 1');
      service.mostrarError('Error 1');
      service.mostrarInfo('Info 1');
    });

    it('debería manejar secuencia de mensajes y cierres', (done) => {
      const eventos: (Mensaje | null)[] = [];
      let subscription = service.mensaje$.subscribe(mensaje => {
        eventos.push(mensaje);
        
        // Verificar que recibimos al menos 4 eventos: [null, mensaje1, null, mensaje2]
        if (eventos.length >= 4) {
          expect(eventos[0]).toBeNull(); // valor inicial
          expect(eventos[1]).toBeTruthy(); // mensaje 1
          expect(eventos[2]).toBeNull(); // después de cerrar mensaje 1
          expect(eventos[3]).toBeTruthy(); // mensaje 2
          subscription.unsubscribe();
          done();
        }
      });

      // Agregar un timeout de seguridad
      setTimeout(() => {
        if (!subscription.closed) {
          subscription.unsubscribe();
          done();
        }
      }, 3000);

      service.mostrarExito('Mensaje 1');
      service.cerrarMensaje();
      service.mostrarError('Mensaje 2');
    });
  });
}); 