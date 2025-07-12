import { ComponentFixture, TestBed } from '@angular/core/testing';
import { App} from './app';
import { Router } from '@angular/router';
import { NavigationEnd, Event } from '@angular/router';
import { Subject } from 'rxjs';
import { of } from 'rxjs';
import { EventMessage, EventType, InteractionType } from '@azure/msal-browser';



describe('App', () => {
  let component: App;
  let fixture: ComponentFixture<App>;
  let routerEventsSubject: Subject<Event>;

  beforeEach(() => {
    routerEventsSubject = new Subject<Event>();

    const mockRouter = {
      events: routerEventsSubject.asObservable()
    };

    TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: Router, useValue: mockRouter }]
    });

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debería establecer isMapPage en true si la ruta es /map', () => {
    routerEventsSubject.next(new NavigationEnd(1, '/inicio', '/map'));
    expect(component.isMapPage).toBeTrue();
  });

  it('debería establecer isMapPage en false si la ruta no es /map', () => {
    routerEventsSubject.next(new NavigationEnd(1, '/inicio', '/contacto'));
    expect(component.isMapPage).toBeFalse();
  });

  // FUNCIONANDO

    it('debería manejar evento LOGIN_SUCCESS y llamar acquireAndSaveToken()', () => {
    const acquireSpy = spyOn(component as any, 'acquireAndSaveToken');
    const loginSpy = spyOn(component as any, 'setLoginDisplay');

    const mockEvent: EventMessage = {
      eventType: EventType.LOGIN_SUCCESS,
      interactionType: InteractionType.Redirect,
      payload: {},
      error: null,
      timestamp: Date.now()
    };

    component['msalBroadcastService'] = {
      msalSubject$: of(mockEvent)
    } as any;

    component.ngOnInit();  // 👈 Activar la suscripción

    expect(acquireSpy).toHaveBeenCalled();
    expect(loginSpy).toHaveBeenCalled();
  });




});
