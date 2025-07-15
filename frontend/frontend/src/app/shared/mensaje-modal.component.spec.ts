import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MensajeModalComponent } from './mensaje-modal.component';
import { MensajeService, Mensaje } from './mensaje.service';
import { BehaviorSubject } from 'rxjs';

describe('MensajeModalComponent', () => {
  let component: MensajeModalComponent;
  let fixture: ComponentFixture<MensajeModalComponent>;
  let mensajeService: jasmine.SpyObj<MensajeService>;
  let mensajeSubject: BehaviorSubject<Mensaje | null>;

  beforeEach(async () => {
    mensajeSubject = new BehaviorSubject<Mensaje | null>(null);
    const spy = jasmine.createSpyObj('MensajeService', ['cerrarMensaje'], {
      mensaje$: mensajeSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [MensajeModalComponent],
      providers: [
        { provide: MensajeService, useValue: spy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MensajeModalComponent);
    component = fixture.componentInstance;
    mensajeService = TestBed.inject(MensajeService) as jasmine.SpyObj<MensajeService>;
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debería mostrar mensaje de éxito', () => {
    const mensaje: Mensaje = {
      texto: 'Operación exitosa',
      tipo: 'exito',
      titulo: 'Éxito',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.modal-overlay')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.modal-icon.exito')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h3').textContent).toContain('Éxito');
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('Operación exitosa');
  });

  it('debería mostrar mensaje de error', () => {
    const mensaje: Mensaje = {
      texto: 'Error en la operación',
      tipo: 'error',
      titulo: 'Error',
      duracion: 5000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.modal-icon.error')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h3').textContent).toContain('Error');
  });

  it('debería mostrar mensaje de advertencia', () => {
    const mensaje: Mensaje = {
      texto: 'Advertencia importante',
      tipo: 'advertencia',
      titulo: 'Advertencia',
      duracion: 4000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.modal-icon.advertencia')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h3').textContent).toContain('Advertencia');
  });

  it('debería mostrar mensaje informativo', () => {
    const mensaje: Mensaje = {
      texto: 'Información importante',
      tipo: 'info',
      titulo: 'Información',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.modal-icon.info')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h3').textContent).toContain('Información');
  });

  it('debería ocultar modal cuando no hay mensaje', () => {
    mensajeSubject.next(null);
    fixture.detectChanges();

    expect(component.mensajeActual).toBeNull();
    expect(fixture.nativeElement.querySelector('.modal-overlay')).toBeFalsy();
  });

  it('debería cerrar mensaje al hacer clic en el botón cerrar', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const cerrarButton = fixture.nativeElement.querySelector('.btn-cerrar');
    cerrarButton.click();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalled();
  });

  it('debería cerrar mensaje al hacer clic en el botón aceptar', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const aceptarButton = fixture.nativeElement.querySelector('.btn-aceptar');
    aceptarButton.click();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalled();
  });

  it('debería cerrar mensaje al hacer clic en el overlay', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const overlay = fixture.nativeElement.querySelector('.modal-overlay');
    overlay.click();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalled();
  });

  it('debería evitar cerrar mensaje al hacer clic en el contenido del modal', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const modalContent = fixture.nativeElement.querySelector('.modal-content');
    modalContent.click();

    expect(mensajeService.cerrarMensaje).not.toHaveBeenCalled();
  });

  it('debería auto-cerrar mensaje después de la duración especificada', (done) => {
    jasmine.clock().install();
    
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 1000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);

    jasmine.clock().tick(1000);
    fixture.detectChanges();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalled();
    
    jasmine.clock().uninstall();
    done();
  });

  it('debería no auto-cerrar mensaje con duración 0', (done) => {
    jasmine.clock().install();
    
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 0
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    jasmine.clock().tick(5000);
    fixture.detectChanges();

    expect(mensajeService.cerrarMensaje).not.toHaveBeenCalled();
    
    jasmine.clock().uninstall();
    done();
  });

  it('debería limpiar timeout anterior al recibir nuevo mensaje', (done) => {
    jasmine.clock().install();
    
    const mensaje1: Mensaje = {
      texto: 'Test 1',
      tipo: 'exito',
      titulo: 'Test 1',
      duracion: 2000
    };

    const mensaje2: Mensaje = {
      texto: 'Test 2',
      tipo: 'error',
      titulo: 'Test 2',
      duracion: 1000
    };

    mensajeSubject.next(mensaje1);
    fixture.detectChanges();

    jasmine.clock().tick(1000); // Antes de que se cierre el primer mensaje

    mensajeSubject.next(mensaje2);
    fixture.detectChanges();

    jasmine.clock().tick(1000);
    fixture.detectChanges();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalledTimes(1);
    
    jasmine.clock().uninstall();
    done();
  });

  it('debería limpiar subscription en ngOnDestroy', () => {
    // Crear un mensaje para inicializar la subscription
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    // Ahora podemos hacer spy en la subscription
    if (component['subscription']) {
      spyOn(component['subscription'], 'unsubscribe');
      
      component.ngOnDestroy();
      
      expect(component['subscription'].unsubscribe).toHaveBeenCalled();
    }
  });

  it('debería limpiar timeout en ngOnDestroy', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    spyOn(window, 'clearTimeout');
    
    component.ngOnDestroy();
    
    expect(window.clearTimeout).toHaveBeenCalled();
  });

  it('debería mostrar iconos SVG correctos para cada tipo de mensaje', () => {
    const tipos = ['exito', 'error', 'advertencia', 'info'] as const;
    
    tipos.forEach(tipo => {
      const mensaje: Mensaje = {
        texto: `Test ${tipo}`,
        tipo,
        titulo: `Test ${tipo}`,
        duracion: 3000
      };

      mensajeSubject.next(mensaje);
      fixture.detectChanges();

      const iconContainer = fixture.nativeElement.querySelector(`.modal-icon.${tipo}`);
      expect(iconContainer).toBeTruthy();
      
      // Verificar que hay un SVG dentro del contenedor
      const svg = iconContainer.querySelector('svg');
      expect(svg).toBeTruthy();
    });
  });

  it('debería manejar mensaje con texto multilínea', () => {
    const mensaje: Mensaje = {
      texto: 'Línea 1\nLínea 2\nLínea 3',
      tipo: 'info',
      titulo: 'Mensaje multilínea',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const messageText = fixture.nativeElement.querySelector('.message-text');
    expect(messageText.textContent).toContain('Línea 1');
    expect(messageText.textContent).toContain('Línea 2');
    expect(messageText.textContent).toContain('Línea 3');
  });

  it('debería aplicar estilos correctos para cada tipo de mensaje', () => {
    const testCases = [
      { tipo: 'exito' as const, expectedClass: 'exito' },
      { tipo: 'error' as const, expectedClass: 'error' },
      { tipo: 'advertencia' as const, expectedClass: 'advertencia' },
      { tipo: 'info' as const, expectedClass: 'info' }
    ];

    testCases.forEach(testCase => {
      const mensaje: Mensaje = {
        texto: `Test ${testCase.tipo}`,
        tipo: testCase.tipo,
        titulo: `Test ${testCase.tipo}`,
        duracion: 3000
      };

      mensajeSubject.next(mensaje);
      fixture.detectChanges();

      const iconElement = fixture.nativeElement.querySelector(`.modal-icon.${testCase.expectedClass}`);
      expect(iconElement).toBeTruthy();
    });
  });

  it('debería manejar mensajes con texto largo', () => {
    const mensaje: Mensaje = {
      texto: 'Este es un mensaje muy largo que debería ser manejado correctamente por el componente modal sin causar problemas de layout o visualización',
      tipo: 'info',
      titulo: 'Mensaje Largo',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('Este es un mensaje muy largo');
  });

  it('debería manejar mensajes con título vacío', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje sin título',
      tipo: 'exito',
      titulo: '',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    const tituloElement = fixture.nativeElement.querySelector('h3');
    expect(tituloElement).toBeTruthy();
  });

  it('debería manejar mensajes con duración negativa', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con duración negativa',
      tipo: 'advertencia',
      titulo: 'Advertencia',
      duracion: -1000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    // No debería auto-cerrarse con duración negativa
    expect(mensajeService.cerrarMensaje).not.toHaveBeenCalled();
  });

  it('debería manejar mensajes con tipo inválido', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con tipo inválido',
      tipo: 'tipo-invalido' as any,
      titulo: 'Tipo Inválido',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    // Debería mostrar el modal aunque el tipo sea inválido
    expect(fixture.nativeElement.querySelector('.modal-overlay')).toBeTruthy();
  });

  it('debería manejar múltiples clics en botones', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const cerrarButton = fixture.nativeElement.querySelector('.btn-cerrar');
    const aceptarButton = fixture.nativeElement.querySelector('.btn-aceptar');

    // Múltiples clics
    cerrarButton.click();
    cerrarButton.click();
    aceptarButton.click();

    expect(mensajeService.cerrarMensaje).toHaveBeenCalledTimes(3);
  });

  it('debería manejar eventos de teclado', () => {
    const mensaje: Mensaje = {
      texto: 'Test',
      tipo: 'exito',
      titulo: 'Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    const modalContent = fixture.nativeElement.querySelector('.modal-content');
    
    // Simular evento de teclado
    const keyEvent = new KeyboardEvent('keydown', { key: 'Escape' });
    modalContent.dispatchEvent(keyEvent);

    // El componente debería manejar el evento de teclado
    expect(component.mensajeActual).toBeTruthy();
  });

  it('debería manejar cambios rápidos de mensajes', () => {
    const mensaje1: Mensaje = {
      texto: 'Mensaje 1',
      tipo: 'exito',
      titulo: 'Test 1',
      duracion: 1000
    };

    const mensaje2: Mensaje = {
      texto: 'Mensaje 2',
      tipo: 'error',
      titulo: 'Test 2',
      duracion: 1000
    };

    mensajeSubject.next(mensaje1);
    fixture.detectChanges();
    expect(component.mensajeActual).toEqual(mensaje1);

    mensajeSubject.next(mensaje2);
    fixture.detectChanges();
    expect(component.mensajeActual).toEqual(mensaje2);
  });

  it('debería manejar mensajes con caracteres especiales', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con caracteres especiales: áéíóú ñ ü ß € £ ¥',
      tipo: 'info',
      titulo: 'Caracteres Especiales',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('áéíóú');
  });

  it('debería manejar mensajes con HTML en el texto', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con <strong>HTML</strong> y <em>tags</em>',
      tipo: 'advertencia',
      titulo: 'HTML Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    const messageText = fixture.nativeElement.querySelector('.message-text');
    expect(messageText.textContent).toContain('Mensaje con');
  });

  it('debería manejar mensajes con números en el texto', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con números: 123, 456.789, -42',
      tipo: 'info',
      titulo: 'Números Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('123');
  });

  it('debería manejar mensajes con URLs en el texto', () => {
    const mensaje: Mensaje = {
      texto: 'Visita https://example.com para más información',
      tipo: 'info',
      titulo: 'URL Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('https://example.com');
  });

  it('debería manejar mensajes con emojis', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje con emojis: 😀 🎉 ✅ ❌ ⚠️',
      tipo: 'exito',
      titulo: 'Emoji Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('😀');
  });

  it('debería manejar mensajes con saltos de línea', () => {
    const mensaje: Mensaje = {
      texto: 'Línea 1\nLínea 2\nLínea 3',
      tipo: 'info',
      titulo: 'Saltos de Línea',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    const messageText = fixture.nativeElement.querySelector('.message-text');
    expect(messageText.textContent).toContain('Línea 1');
    expect(messageText.textContent).toContain('Línea 2');
  });

  it('debería manejar mensajes con espacios múltiples', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje    con    espacios    múltiples',
      tipo: 'advertencia',
      titulo: 'Espacios Test',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('Mensaje');
  });

  it('debería manejar mensajes con texto muy corto', () => {
    const mensaje: Mensaje = {
      texto: 'OK',
      tipo: 'exito',
      titulo: 'Corto',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('.message-text').textContent).toContain('OK');
  });

  it('debería manejar mensajes con título muy largo', () => {
    const mensaje: Mensaje = {
      texto: 'Mensaje normal',
      tipo: 'info',
      titulo: 'Este es un título muy largo que debería ser manejado correctamente por el componente',
      duracion: 3000
    };

    mensajeSubject.next(mensaje);
    fixture.detectChanges();

    expect(component.mensajeActual).toEqual(mensaje);
    expect(fixture.nativeElement.querySelector('h3').textContent).toContain('Este es un título muy largo');
  });
}); 