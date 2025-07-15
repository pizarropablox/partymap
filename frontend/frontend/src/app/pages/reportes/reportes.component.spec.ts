import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReportesComponent } from './reportes.component';
import { NavigationService } from '../../services/navigation.service';

describe('ReportesComponent', () => {
  let component: ReportesComponent;
  let fixture: ComponentFixture<ReportesComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockNavigationService: jasmine.SpyObj<NavigationService>;

  beforeAll(() => {
    try { spyOn(window.location, 'assign').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'replace').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'reload').and.callFake(() => {}); } catch (e) {}
  });
  beforeEach(() => {
    spyOn(console, 'error').and.callFake(() => {});
  });

  beforeEach(async () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const navigationSpy = jasmine.createSpyObj('NavigationService', ['goTo']);
    
    await TestBed.configureTestingModule({
      imports: [ReportesComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: NavigationService, useValue: navigationSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportesComponent);
    component = fixture.componentInstance;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockNavigationService = TestBed.inject(NavigationService) as jasmine.SpyObj<NavigationService>;
  });

  beforeEach(() => {
    // Mock window.location methods to prevent page reloads during tests
    try {
      spyOn(window.location, 'assign').and.callFake(() => {});
      spyOn(window.location, 'replace').and.callFake(() => {});
      spyOn(window.location, 'reload').and.callFake(() => {});
    } catch (error) {
      // Ignore errors if methods are already mocked
    }
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have initial state', () => {
    expect(component).toBeTruthy();
    // Verificar que el componente se inicializa correctamente
  });

  it('should render reportes container', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.reportes-container')).toBeTruthy();
  });

  it('should call ngOnInit and call obtenerUserRole, cargarEstadisticas, cargarEstadisticasEventos, cargarEstadisticasUsuarios', async () => {
    spyOn(component, 'obtenerUserRole');
    spyOn(component, 'cargarEstadisticas');
    spyOn(component, 'cargarEstadisticasEventos');
    spyOn(component, 'cargarEstadisticasUsuarios');
    await component.ngOnInit();
    expect(component.obtenerUserRole).toHaveBeenCalled();
    expect(component.cargarEstadisticas).toHaveBeenCalled();
    expect(component.cargarEstadisticasEventos).toHaveBeenCalled();
    expect(component.cargarEstadisticasUsuarios).toHaveBeenCalled();
  });

  it('should call verificarAutenticacion and return false if no token', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);
    expect(component.verificarAutenticacion()).toBeFalse();
  });

  it('should call limpiarSesion and remove tokens', () => {
    spyOn(localStorage, 'removeItem');
    spyOn(sessionStorage, 'removeItem');
    spyOn(sessionStorage, 'clear');
    component.limpiarSesion();
    expect(localStorage.removeItem).toHaveBeenCalled();
    expect(sessionStorage.removeItem).toHaveBeenCalled();
    expect(sessionStorage.clear).toHaveBeenCalled();
  });

  it('should call irAlLogin and limpiarSesion', () => {
    spyOn(component, 'limpiarSesion');
    component.irAlLogin();
    expect(component.limpiarSesion).toHaveBeenCalled();
    expect(mockNavigationService.goTo).toHaveBeenCalledWith(component['AZURE_B2C_LOGIN_URL']);
  });

  it('should format moneda, porcentaje, numero', () => {
    expect(component.formatearMoneda(1000)).toContain('$');
    expect(component.formatearPorcentaje(50)).toContain('%');
    expect(component.formatearNumero(1000)).toContain('1.000');
  });
}); 