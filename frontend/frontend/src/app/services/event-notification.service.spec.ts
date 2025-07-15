import { TestBed } from '@angular/core/testing';
import { EventNotificationService } from './event-notification.service';

describe('EventNotificationService', () => {
  let service: EventNotificationService;

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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EventNotificationService]
    });
    service = TestBed.inject(EventNotificationService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería notificar evento creado', () => {
    let notificationReceived = false;
    service.eventCreated$.subscribe(() => {
      notificationReceived = true;
    });

    service.notifyEventCreated();
    expect(notificationReceived).toBeTrue();
  });

  it('debería emitir múltiples notificaciones', () => {
    let eventCount = 0;

    service.eventCreated$.subscribe(() => eventCount++);

    service.notifyEventCreated();
    service.notifyEventCreated();
    service.notifyEventCreated();

    expect(eventCount).toBe(3);
  });

  it('debería manejar múltiples suscriptores', () => {
    let subscriber1Count = 0;
    let subscriber2Count = 0;

    service.eventCreated$.subscribe(() => subscriber1Count++);
    service.eventCreated$.subscribe(() => subscriber2Count++);

    service.notifyEventCreated();

    expect(subscriber1Count).toBe(1);
    expect(subscriber2Count).toBe(1);
  });

  it('debería limpiar suscripciones al destruir', () => {
    let notificationReceived = false;
    const subscription = service.eventCreated$.subscribe(() => {
      notificationReceived = true;
    });

    subscription.unsubscribe();
    service.notifyEventCreated();

    expect(notificationReceived).toBeFalse();
  });

  it('debería tener observable eventCreated$ definido', () => {
    expect(service.eventCreated$).toBeDefined();
  });

  it('debería tener método notifyEventCreated definido', () => {
    expect(typeof service.notifyEventCreated).toBe('function');
  });

  it('debería emitir notificación inmediatamente', () => {
    let received = false;
    service.eventCreated$.subscribe(() => received = true);
    
    service.notifyEventCreated();
    
    expect(received).toBeTrue();
  });

  it('debería manejar suscripción después de notificación', () => {
    service.notifyEventCreated();
    
    let received = false;
    service.eventCreated$.subscribe(() => received = true);
    
    // La suscripción no debería recibir notificaciones anteriores
    expect(received).toBeFalse();
  });
});
