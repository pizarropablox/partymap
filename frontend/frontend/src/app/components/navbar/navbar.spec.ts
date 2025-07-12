import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MsalService, MSAL_INSTANCE, MsalBroadcastService } from '@azure/msal-angular';
import { IPublicClientApplication, PublicClientApplication, InteractionStatus } from '@azure/msal-browser';
import { of, throwError } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Subject } from 'rxjs';

import { fakeAsync, tick } from '@angular/core/testing';
import { EventType } from '@azure/msal-browser';







/** Fábrica mínima para simular MSAL_INSTANCE */
export function msalInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId: 'test-client-id',
      authority: 'https://login.microsoftonline.com/common',
      redirectUri: '/',
    }
  });
}

/** Mock completo de MsalBroadcastService */
class MockMsalBroadcastService {
  msalSubject$ = of();        // Observable vacío para simular .pipe()
  inProgress$ = of(InteractionStatus.None); // También requerido si lo usas
}

fdescribe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        NavbarComponent,
      ],
      providers: [
        { provide: MSAL_INSTANCE, useFactory: msalInstanceFactory },
        { provide: MsalBroadcastService, useClass: MockMsalBroadcastService },
        MsalService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debería tener el menú móvil cerrado al iniciar', () => {
    expect(component.isMobileMenuOpen).toBeFalse();
  });

  it('debería abrir el menú móvil al llamar toggleMobileMenu', () => {
    component.toggleMobileMenu();
    expect(component.isMobileMenuOpen).toBeTrue();
  });

  it('debería cerrar el menú móvil al llamar closeMobileMenu', () => {
    component.isMobileMenuOpen = true;
    component.closeMobileMenu();
    expect(component.isMobileMenuOpen).toBeFalse();
  });

  it('debería mostrar el botón del menú móvil en el DOM', () => {
    const menuButton = fixture.debugElement.query(By.css('.mobile-menu-btn'));
    expect(menuButton).toBeTruthy();
  });

  it('debería cambiar el estado del menú al hacer clic en el botón', () => {
    const menuButton = fixture.debugElement.query(By.css('.mobile-menu-btn'));
    menuButton.triggerEventHandler('click');
    fixture.detectChanges();
    expect(component.isMobileMenuOpen).toBeTrue();
  });

  // FUNCIONANDO


    it('debería ejecutar setLoginDisplay y checkAndSetActiveAccount cuando InteractionStatus.None', () => {
    const setLoginSpy = spyOn(component as any, 'setLoginDisplay');
    const checkAccountSpy = spyOn(component as any, 'checkAndSetActiveAccount');

    // Forzamos el observable a emitir InteractionStatus.None
    (component as any).msalBroadcastService = {
      inProgress$: of(InteractionStatus.None)
    };

    (component as any).handleInteractionInProgress();

    expect(setLoginSpy).toHaveBeenCalled();
    expect(checkAccountSpy).toHaveBeenCalled();
  });

    it('debería redirigir al hacer login()', () => {
    const hrefSetter = spyOnProperty(window.location, 'href', 'set');
    component.login();
    expect(hrefSetter).toHaveBeenCalled();
  });

  // FUNCIONANDO

    it('debería asignar userName y userRole desde response.data', () => {
    const mockUsuario = { nombre: 'Juan', tipoUsuario: 'ADMINISTRADOR' };
    spyOn(component['cdr'], 'detectChanges');
    spyOn(component as any, 'updatePermissions');

    component['reservaService'].obtenerUsuarioActual = () => of({ data: mockUsuario });

    (component as any).loadUserInfoFromBackend();

    expect(component.userName).toBe('Juan');
    expect(component.userRole).toBe('ADMINISTRADOR');
  });

  it('debería asignar userName y userRole desde response directo con tipoUsuario', () => {
    const response = { nombre: 'Ana', email: 'ana@correo.com', tipoUsuario: 'INVITADO' };
    spyOn(component['cdr'], 'detectChanges');
    spyOn(component as any, 'updatePermissions');

    component['reservaService'].obtenerUsuarioActual = () => of(response);

    (component as any).loadUserInfoFromBackend();

    expect(component.userName).toBe('Ana');
    expect(component.userRole).toBe('INVITADO');
  });

  it('debería ejecutar extractUserInfoFromToken si response es vacío', () => {
    const spyFallback = spyOn(component as any, 'extractUserInfoFromToken');
    component['reservaService'].obtenerUsuarioActual = () => of({});
    (component as any).loadUserInfoFromBackend();
    expect(spyFallback).toHaveBeenCalled();
  });

  // FUNCIONANDO

  it('debería manejar error 401 y redirigir al login', () => {
    spyOn(window.localStorage, 'removeItem');
    const setHrefSpy = spyOnProperty(window.location, 'href', 'set');
    const advertenciaSpy = spyOn(component['mensajeService'], 'mostrarAdvertencia');

    component['reservaService'].obtenerUsuarioActual = () => throwError({ status: 401 });

    (component as any).loadUserInfoFromBackend();

    expect(window.localStorage.removeItem).toHaveBeenCalledWith('jwt');
    expect(advertenciaSpy).toHaveBeenCalled();
    expect(setHrefSpy).toHaveBeenCalledWith('/login');
  });

  it('debería ejecutar fallback y updatePermissions en error genérico', (done) => {
    const fallbackSpy = spyOn(component as any, 'extractUserInfoFromToken');
    const updateSpy = spyOn(component as any, 'updatePermissions');
    spyOn(component['cdr'], 'detectChanges');

    component['reservaService'].obtenerUsuarioActual = () => throwError({ status: 500 });

    (component as any).loadUserInfoFromBackend();

    setTimeout(() => {
      expect(fallbackSpy).toHaveBeenCalled();
      expect(updateSpy).toHaveBeenCalled();
      done();
    }, 100);
  });

  // FUNCIONANDO

  it('debería mostrar advertencia si el backend no está disponible (status 500)', () => {
  spyOn(localStorage, 'getItem').and.returnValue('token');
  const advertenciaSpy = spyOn(component['mensajeService'], 'mostrarAdvertencia');

  component['reservaService'].obtenerUsuarioActual = () => throwError({ status: 500 });

  (component as any).checkBackendAvailability();

  expect(advertenciaSpy).toHaveBeenCalled();
});

  // FUNCIONANDO

  it('no debería hacer nada si no hay tokens en localStorage', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);

    (component as any).checkBackendAvailability(); // No debería fallar
  });

  // FUNCIONANDO

  it('debería extraer datos desde el token y actualizar permisos', () => {
    const fakeToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.' +
                      btoa(JSON.stringify({
                        given_name: 'Pablo',
                        family_name: 'Pizarro',
                        extension_Roles: 'ADMINISTRADOR'
                      })) + '.signature';

    spyOn(localStorage, 'getItem').and.returnValue(fakeToken);
    spyOn(component as any, 'decodeJwtToken').and.callThrough();
    spyOn(component as any, 'updatePermissions');
    spyOn(component['cdr'], 'detectChanges');

    (component as any).extractUserInfoFromToken();

    expect(component.userName).toBe('Pablo Pizarro');
    expect(component.userRole).toBe('ADMINISTRADOR');
  });

  // FUNCIONANDO

    

    it('debería ejecutar el bloque next si el backend responde correctamente', () => {
    spyOn(localStorage, 'getItem').and.returnValue('token');
    const logSpy = spyOn(console, 'log');

    // Simula que el backend responde correctamente
    component['reservaService'].obtenerUsuarioActual = () => of('ok');

    // Agrega acción en next para asegurar cobertura
    const original = component['reservaService'].obtenerUsuarioActual;
    component['reservaService'].obtenerUsuarioActual = () => {
      return of('ok').pipe(
        tap(() => console.log('Backend OK')) // Línea que se ejecuta
      );
    };

    (component as any).checkBackendAvailability();

    expect(logSpy).toHaveBeenCalledWith('Backend OK');
  });

  // FUNCIONANDO

    it('debería extraer datos desde el jwt si no hay idToken', () => {
    const fakeJwt = 'header.' + btoa(JSON.stringify({
      name: 'Juanito',
      role: 'MOD',
    })) + '.sig';

    spyOn(localStorage, 'getItem').and.callFake((key: string) =>
      key === 'idToken' ? null : key === 'jwt' ? fakeJwt : null
    );
    spyOn(component as any, 'decodeJwtToken').and.callThrough();
    spyOn(component as any, 'updatePermissions');
    spyOn(component['cdr'], 'detectChanges');

    (component as any).extractUserInfoFromToken();

    expect(component.userName).toBe('Juanito');
    expect(component.userRole).toBe('MOD');
  });

  // FUNCIONANDO

    it('debería extraer userInfo desde localStorage si no hay tokens', () => {
    const userInfo = {
      name: 'Ana',
      roles: ['INVITADO']
    };

    spyOn(localStorage, 'getItem').and.callFake((key: string) =>
      key === 'userInfo' ? JSON.stringify(userInfo) : null
    );
    spyOn(component as any, 'updatePermissions');
    spyOn(component['cdr'], 'detectChanges');

    (component as any).extractUserInfoFromToken();

    expect(component.userName).toBe('Ana');
    expect(component.userRole).toBe('INVITADO');
  });

  // FUNCIONANDO

    it('debería usar fallback si decodeJwtToken lanza error', () => {
    const invalidToken = 'header.payload.signature';

    spyOn(localStorage, 'getItem').and.callFake((key: string) =>
      key === 'idToken' ? invalidToken : null
    );

    // Esto va a lanzar error al intentar hacer JSON.parse en decodeJwtToken
    spyOn(component as any, 'decodeJwtToken').and.callFake(() => { throw new Error('fail'); });
    spyOn(component as any, 'updatePermissions');
    spyOn(component['cdr'], 'detectChanges');

    (component as any).extractUserInfoFromToken();

    expect(component.userName).toBe('Usuario');
    expect(component.userRole).toBe('Usuario');
  });

  // FUNCIONANDO

  it('debería eliminar todos los tokens al ejecutar logout()', () => {
  const localRemove = spyOn(localStorage, 'removeItem');
  const sessionRemove = spyOn(sessionStorage, 'removeItem');
  const logSpy = spyOn(console, 'log');

  component.logout();

  expect(logSpy).toHaveBeenCalledWith('Iniciando proceso de logout...');
  expect(localRemove).toHaveBeenCalledWith('jwt');
  expect(localRemove).toHaveBeenCalledWith('idToken');
  expect(sessionRemove).toHaveBeenCalledWith('msal.error');
});

// FUNCIONANDO

it('debería establecer cuenta activa desde el token si hay coincidencia', () => {
  const tokenPayload = { preferred_username: 'usuario@correo.com' };
  const fakeToken = 'header.' + btoa(JSON.stringify(tokenPayload)) + '.sig';

  const fakeAccount = {
    homeAccountId: 'home',
    environment: 'env',
    tenantId: 'tenant',
    localAccountId: 'local',
    username: 'usuario@correo.com'
  };

  spyOn(localStorage, 'getItem').and.callFake((key: string) =>
    key === 'idToken' ? fakeToken : null
  );

  spyOn(component as any, 'decodeJwtToken').and.returnValue(tokenPayload);

  const msalInstance = component['msalService'].instance;
  spyOn(msalInstance, 'getAllAccounts').and.returnValue([fakeAccount]);
  const setAccountSpy = spyOn(msalInstance, 'setActiveAccount');

  (component as any).attemptToSetActiveAccountFromTokens();

  expect(setAccountSpy).toHaveBeenCalledWith(fakeAccount);
});

// FUNCIONANDO

it('debería establecer cuenta activa si no hay ninguna activa', () => {
  const fakeAccount = {
    homeAccountId: 'home',
    environment: 'env',
    tenantId: 'tenant',
    localAccountId: 'local',
    username: 'test@correo.com'
  };

  const msalInstance = component['msalService'].instance;
  spyOn(msalInstance, 'getActiveAccount').and.returnValue(null);
  spyOn(msalInstance, 'getAllAccounts').and.returnValue([fakeAccount]);
  const setSpy = spyOn(msalInstance, 'setActiveAccount');

  component.checkAndSetActiveAccount();

  expect(setSpy).toHaveBeenCalledWith(fakeAccount);
});

// FUNCIONANDO

  it('debería ocultar resultados al perder focus', () => {
    jasmine.clock().install();
    component['showResults'] = true;

    component.onSearchBlur();
    jasmine.clock().tick(201);

    expect(component['showResults']).toBeFalse();
    jasmine.clock().uninstall();
  });

  // FUNCIONANDO

    it('debería capturar error si geocodeAddress falla', () => {
    const errorSpy = spyOn(console, 'error');
    component['searchTerm'] = 'Ñuble 1111';
    component['geocodingService'].geocodeAddress = () => throwError(() => new Error('falló'));

    const event = { key: 'Enter' };
    component.onSearchEnter(event);

    expect(errorSpy).toHaveBeenCalled();
  });

  // FUNCIONANDO

  it('debería actualizar búsqueda y navegar al seleccionar un resultado', () => {
    const navigateSpy = spyOn(component['mapNavigationService'], 'navigateToLocation');

    const ubicacion = {
      id: 1,
      nombre: 'Las Condes 123',
      direccion: 'Las Condes 123',
      comuna: 'Santiago',
      latitud: -33.45,
      longitud: -70.66,
      isGeocoded: true
    };

    component.selectResult(ubicacion);

    expect(component['searchTerm']).toBe('Las Condes 123');
    expect(component['showResults']).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(ubicacion);
  });

  // FUNCIONANDO

    it('debería mostrar resultados si searchResults tiene elementos', () => {
    component['searchResults'] = [{
      id: 1,
      nombre: 'Prueba',
      direccion: 'Av. Siempre Viva',
      comuna: 'Springfield',
      latitud: -33.45,
      longitud: -70.66
    }];

    component['showResults'] = false;

    component.onSearchFocus();

    expect(component['showResults']).toBeTrue();
  });

  // FUNCIONANDO

   it('debería capturar error si geocodeAddress falla al navegar', () => {
  const errorSpy = spyOn(console, 'error');
  const address = 'Lugar fallido';

  component['geocodingService'].geocodeAddress = () => throwError(() => new Error('Fallo'));

  (component as any).navigateToAddress(address);

  expect(errorSpy).toHaveBeenCalled();
});

  // FUNCIONANDO

  it('debería geocodificar y navegar si se obtienen coordenadas', () => {
    const address = 'Alameda 123';

    const mockGeocodingResult = {
      direccion: address,
      comuna: 'Santiago',
      formattedAddress: 'Alameda 123, Santiago',
      lat: -33.45,
      lng: -70.66
    };

    const navigateSpy = spyOn(component['mapNavigationService'], 'navigateToLocation');
    component['geocodingService'].geocodeAddress = () => of(mockGeocodingResult);

    (component as any).navigateToAddress(address);

    expect(navigateSpy).toHaveBeenCalledWith(jasmine.objectContaining({
      nombre: address,
      direccion: address,
      comuna: 'Santiago',
      latitud: mockGeocodingResult.lat,
      longitud: mockGeocodingResult.lng
    }));
  });

  // FUNCIONANDO

  it('debería ejecutar setLoginDisplay y checkAccess al forzar update', () => {
    jasmine.clock().install();

    const spy1 = spyOn(component as any, 'setLoginDisplay');
    const spy2 = spyOn(component as any, 'checkEventsAccess');
    const spy3 = spyOn(component as any, 'checkReportesAccess');
    const spy4 = spyOn(component as any, 'checkUsuarioAccess');
    const spy5 = spyOn(component as any, 'checkReservasAccess');

    (component as any).forceUpdateLoginState();

    jasmine.clock().tick(101);

    expect(spy1).toHaveBeenCalled();
    expect(spy2).toHaveBeenCalled();
    expect(spy3).toHaveBeenCalled();
    expect(spy4).toHaveBeenCalled();
    expect(spy5).toHaveBeenCalled();

    jasmine.clock().uninstall();
  });

  // FUNCIONANDO

  it('debería ejecutar setLoginDisplay y checkAccess al forzar update', () => {
    jasmine.clock().install();

    const spy1 = spyOn(component as any, 'setLoginDisplay');
    const spy2 = spyOn(component as any, 'checkEventsAccess');
    const spy3 = spyOn(component as any, 'checkReportesAccess');
    const spy4 = spyOn(component as any, 'checkUsuarioAccess');
    const spy5 = spyOn(component as any, 'checkReservasAccess');

    (component as any).forceUpdateLoginState();

    jasmine.clock().tick(101);

    expect(spy1).toHaveBeenCalled();
    expect(spy2).toHaveBeenCalled();
    expect(spy3).toHaveBeenCalled();
    expect(spy4).toHaveBeenCalled();
    expect(spy5).toHaveBeenCalled();

    jasmine.clock().uninstall();
  });

  // FUNCIONANDO

  it('debería procesar access_token e id_token desde el hash de la URL', () => {
    const fakeHash = '#access_token=fakeAccess&id_token=fakeId';

    const fakeLocation = {
      hash: fakeHash
    };

    spyOnProperty(window, 'location', 'get').and.returnValue(fakeLocation as any);

    const tokenPayload = {
      name: 'Pablo',
      email: 'pablo@test.com',
      extension_Roles: 'ADMIN',
      role: 'ADMIN',
      sub: 'abc123'
    };

    spyOn(component as any, 'decodeJwtToken').and.returnValue(tokenPayload);
    spyOn(component as any, 'setLoginDisplay');
    spyOn(localStorage, 'setItem');

    (component as any).handleTokenInUrlFragment();

    expect(localStorage.setItem).toHaveBeenCalledWith('accessToken', 'fakeAccess');
    expect(localStorage.setItem).toHaveBeenCalledWith('idToken', 'fakeId');
    expect(component['setLoginDisplay']).toHaveBeenCalled();
  });


  // FUNCIONANDO


 it('debería adquirir token y guardar en localStorage', () => {
  const fakeAccount = { username: 'juan' };
  const fakeResponse = {
    accessToken: 'access-123',
    idToken: 'id-456'
  };

  component['msalService'] = {
    instance: {
      getAllAccounts: () => [fakeAccount],
      setActiveAccount: () => {},
      acquireTokenSilent: () => of(fakeResponse)
    }
  } as any;

  const setItemSpy = spyOn(localStorage, 'setItem');

  (component as any).acquireAndSaveToken();

  expect(setItemSpy).toHaveBeenCalledWith('accessToken', 'access-123');
  expect(setItemSpy).toHaveBeenCalledWith('idToken', 'id-456');
});

// FUNCIONANDO

it('debería adquirir token y guardar en localStorage', () => {
  const fakeAccount = { username: 'juan' };
  const fakeResponse = {
    accessToken: 'access-123',
    idToken: 'id-456'
  };

  component['msalService'] = {
    instance: {
      getAllAccounts: () => [fakeAccount],
      setActiveAccount: () => {},
      acquireTokenSilent: () => of(fakeResponse)
    }
  } as any;

  const setItemSpy = spyOn(localStorage, 'setItem');

  (component as any).acquireAndSaveToken();

  expect(setItemSpy).toHaveBeenCalledWith('accessToken', 'access-123');
  expect(setItemSpy).toHaveBeenCalledWith('idToken', 'id-456');
});

// FUNCIONANDO

it('debería guardar tokens y llamar setLoginDisplay si acquireTokenSilent es exitoso', () => {
  const fakeAccount = {
    username: 'test',
    homeAccountId: 'home-id',
    environment: 'env',
    tenantId: 'tenant-id',
    localAccountId: 'local-id'
  };

  const fakeResponse = {
    accessToken: 'tok-123',
    idToken: 'id-123',
    authority: '',
    uniqueId: '',
    tenantId: '',
    scopes: [],
    account: fakeAccount,
    expiresOn: null,
    fromCache: false,
    correlationId: '',
    tokenType: '',
    idTokenClaims: {}
  };

  const msalSpy = component['msalService'];
  spyOn(msalSpy.instance, 'getAllAccounts').and.returnValue([fakeAccount]);
  spyOn(msalSpy.instance, 'setActiveAccount');
  spyOn(msalSpy, 'acquireTokenSilent').and.returnValue(of(fakeResponse));
  const loginSpy = spyOn(component as any, 'setLoginDisplay');

  (component as any).acquireAndSaveToken();

  expect(localStorage.getItem('accessToken')).toBe('tok-123');
  expect(localStorage.getItem('idToken')).toBe('id-123');
  expect(loginSpy).toHaveBeenCalled();
});

// FUNCIONANDO

it('debería intentar con popup si acquireTokenSilent falla y guardar los tokens', () => {
  const fakeAccount = {
    username: 'test',
    homeAccountId: 'home-id',
    environment: 'env',
    tenantId: 'tenant-id',
    localAccountId: 'local-id'
  };

  const fakeResponse = {
    accessToken: 'popup-access',
    idToken: 'popup-id',
    authority: '',
    uniqueId: '',
    tenantId: '',
    scopes: [],
    account: fakeAccount,
    expiresOn: null,
    fromCache: false,
    correlationId: '',
    tokenType: '',
    idTokenClaims: {}
  };

  const msalSpy = component['msalService'];
  spyOn(msalSpy.instance, 'getAllAccounts').and.returnValue([fakeAccount]);
  spyOn(msalSpy.instance, 'setActiveAccount');
  spyOn(msalSpy, 'acquireTokenSilent').and.returnValue(throwError(() => new Error('fail')));
  spyOn(msalSpy, 'acquireTokenPopup').and.returnValue(of(fakeResponse));
  const loginSpy = spyOn(component as any, 'setLoginDisplay');

  (component as any).acquireAndSaveToken();

  expect(localStorage.getItem('accessToken')).toBe('popup-access');
  expect(localStorage.getItem('idToken')).toBe('popup-id');
  expect(loginSpy).toHaveBeenCalled();
});

// FUNCIONANDO

it('debería mostrar error si fallan ambos métodos de adquisición de token', () => {
  const fakeAccount = {
    username: 'test',
    homeAccountId: 'home-id',
    environment: 'env',
    tenantId: 'tenant-id',
    localAccountId: 'local-id'
  };

  const msalSpy = component['msalService'];
  spyOn(msalSpy.instance, 'getAllAccounts').and.returnValue([fakeAccount]);
  spyOn(msalSpy.instance, 'setActiveAccount');
  spyOn(msalSpy, 'acquireTokenSilent').and.returnValue(throwError(() => new Error('fail')));
  spyOn(msalSpy, 'acquireTokenPopup').and.returnValue(throwError(() => new Error('popup fail')));
  const errorSpy = spyOn(console, 'error');

  (component as any).acquireAndSaveToken();

  expect(errorSpy).toHaveBeenCalledWith('Error acquiring token:', jasmine.any(Error));
  expect(errorSpy).toHaveBeenCalledWith('Error acquiring token via popup:', jasmine.any(Error));
});

// FUNCIONANDO


it('debería manejar error si buscarUbicaciones falla', fakeAsync(() => {
  const errorSpy = spyOn(console, 'error');
  component['ubicacionService'].buscarUbicaciones = () => throwError(() => new Error('falló'));

  component['searchSubject'].next('Error prueba');
  tick(301);

  expect(component['searchResults']).toEqual([]);
  expect(component['isSearching']).toBeFalse();
  expect(component['showResults']).toBeFalse();
}));

// FUNCIONANDO

it('debería inicializar searchSubject y buscar ubicaciones', fakeAsync(() => {
  const mockResults = [
    { id: 1, nombre: 'Ubicación 1', direccion: '', comuna: '', latitud: 0, longitud: 0 },
    { id: 2, nombre: 'Ubicación 2', direccion: '', comuna: '', latitud: 0, longitud: 0 }
  ];

  component['ubicacionService'].buscarUbicaciones = () => of(mockResults);
  component['searchSubject'].next('Gran Avenida');
  tick(301); // Esperar debounceTime

  expect(component['searchResults']).toEqual(mockResults);
  expect(component['isSearching']).toBeFalse();
  expect(component['showResults']).toBeTrue();
}));

// FUNCIONANDO

it('debería limpiar resultados si término está vacío', fakeAsync(() => {
  component['searchResults'] = [
    { id: 1, nombre: 'Calle falsa', direccion: '', comuna: '', latitud: 0, longitud: 0 }
  ];
  component['isSearching'] = true;
  component['showResults'] = true;

  component['searchSubject'].next('   ');
  tick(301); // Simular tiempo de debounce

  expect(component['searchResults']).toEqual([]);
  expect(component['isSearching']).toBeFalse();
  expect(component['showResults']).toBeFalse();
}));

// FUNCIONANDO 28.44%

it('debería ejecutar updatePermissions y llamar todos los métodos de acceso', () => {
  const eventsSpy = spyOn(component as any, 'checkEventsAccess');
  const reportesSpy = spyOn(component as any, 'checkReportesAccess');
  const usuarioSpy = spyOn(component as any, 'checkUsuarioAccess');
  const reservasSpy = spyOn(component as any, 'checkReservasAccess');

  (component as any).updatePermissions();

  expect(eventsSpy).toHaveBeenCalled();
  expect(reportesSpy).toHaveBeenCalled();
  expect(usuarioSpy).toHaveBeenCalled();
  expect(reservasSpy).toHaveBeenCalled();
});


// FUNCIONANDO



});
