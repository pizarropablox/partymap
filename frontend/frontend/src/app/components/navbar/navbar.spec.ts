import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MsalService, MSAL_INSTANCE, MsalBroadcastService } from '@azure/msal-angular';
import { IPublicClientApplication, PublicClientApplication, InteractionStatus } from '@azure/msal-browser';
import { of } from 'rxjs';

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
});
