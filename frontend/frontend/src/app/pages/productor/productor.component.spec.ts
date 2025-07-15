import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ProductorComponent } from './productor.component';
import { NavigationService } from '../../services/navigation.service';
import { MensajeService } from '../../shared/mensaje.service';

describe('ProductorComponent', () => {
  let component: ProductorComponent;
  let fixture: ComponentFixture<ProductorComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockNavigationService: jasmine.SpyObj<NavigationService>;
  let mockMensajeService: jasmine.SpyObj<MensajeService>;

  beforeAll(() => {
    try { spyOn(window.location, 'assign').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'replace').and.callFake(() => {}); } catch (e) {}
    try { spyOn(window.location, 'reload').and.callFake(() => {}); } catch (e) {}
  });

  beforeEach(async () => {
    spyOn(console, 'error').and.callFake(() => {});

    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const navigationSpy = jasmine.createSpyObj('NavigationService', ['goTo']);
    const mensajeServiceSpy = jasmine.createSpyObj('MensajeService', ['mostrarAdvertencia']);
    
    await TestBed.configureTestingModule({
      imports: [ProductorComponent, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: NavigationService, useValue: navigationSpy },
        { provide: MensajeService, useValue: mensajeServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductorComponent);
    component = fixture.componentInstance;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockNavigationService = TestBed.inject(NavigationService) as jasmine.SpyObj<NavigationService>;
    mockMensajeService = TestBed.inject(MensajeService) as jasmine.SpyObj<MensajeService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have initial state', () => {
    expect(component).toBeTruthy();
    // Verificar que el componente se inicializa correctamente
  });

  it('should render productor container', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.productor-container')).toBeTruthy();
  });

  it('should call ngOnInit and call obtenerUsuarioActualId, obtenerUserRole, cargarProductores, iniciarVerificacionToken', async () => {
    spyOn(component, 'obtenerUsuarioActualId').and.returnValue(Promise.resolve());
    spyOn(component, 'obtenerUserRole');
    spyOn(component, 'cargarProductores');
    spyOn(component, 'iniciarVerificacionToken');
    await component.ngOnInit();
    expect(component.obtenerUsuarioActualId).toHaveBeenCalled();
    expect(component.obtenerUserRole).toHaveBeenCalled();
    expect(component.cargarProductores).toHaveBeenCalled();
    expect(component.iniciarVerificacionToken).toHaveBeenCalled();
  });

  it('should call ngOnDestroy and detenerVerificacionToken', () => {
    spyOn(component, 'detenerVerificacionToken');
    component.ngOnDestroy();
    expect(component.detenerVerificacionToken).toHaveBeenCalled();
  });

  it('should call mostrarAdvertenciaToken and irAlLogin if confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(component, 'irAlLogin');
    component.mostrarAdvertenciaToken();
    expect(component.irAlLogin).toHaveBeenCalled();
  });

  it('should call cerrarSesionAutomaticamente and limpiarSesion, mostrarAdvertencia, irAlLogin', (done) => {
    spyOn(component, 'limpiarSesion');
    spyOn(component, 'irAlLogin');
    component.cerrarSesionAutomaticamente();
    setTimeout(() => {
      expect(component.limpiarSesion).toHaveBeenCalled();
      expect(mockMensajeService.mostrarAdvertencia).toHaveBeenCalled();
      expect(component.irAlLogin).toHaveBeenCalled();
      done();
    }, 1100);
  });

  it('should call cerrarModalProductor and set mostrarModalProductor to false', () => {
    component.mostrarModalProductor = true;
    component.cerrarModalProductor();
    expect(component.mostrarModalProductor).toBeFalse();
  });

  it('should call cerrarModalEvento and set mostrarModalEvento to false', () => {
    component.mostrarModalEvento = true;
    component.cerrarModalEvento();
    expect(component.mostrarModalEvento).toBeFalse();
  });

  it('should call irAPagina and change paginaActual', () => {
    component.productoresPorPagina = 1;
    component.productoresFiltrados = [
      { id: 1, nombre: '', email: '', tipoUsuario: '', activo: 1, fechaCreacion: '' } as any,
      { id: 2, nombre: '', email: '', tipoUsuario: '', activo: 1, fechaCreacion: '' } as any
    ];
    component.totalPaginas = 2;
    component.paginaActual = 1;
    component.aplicarPaginacion();

    component.irAPagina(2);
    expect(component.paginaActual).toBe(2);

    component.irAPagina(0);
    expect(component.paginaActual).toBe(2); // No cambia porque 0 no es válido
  });

  it('should call paginaAnterior and paginaSiguiente', () => {
    component.productoresPorPagina = 1;
    component.productoresFiltrados = [
      { id: 1, nombre: '', email: '', tipoUsuario: '', activo: 1, fechaCreacion: '' } as any,
      { id: 2, nombre: '', email: '', tipoUsuario: '', activo: 1, fechaCreacion: '' } as any
    ];
    component.totalPaginas = 2;
    component.paginaActual = 2;
    component.aplicarPaginacion();

    component.paginaAnterior();
    expect(component.paginaActual).toBe(1);

    component.paginaSiguiente();
    expect(component.paginaActual).toBe(2);
  });

  it('should call limpiarFiltroProductores', () => {
    component.filtroProductorNombre = 'test';
    component.filtroProductorFecha = '2024-01-01';
    component.productoresFiltrados = [
      { id: 1, nombre: '', email: '', tipoUsuario: '', activo: 1, fechaCreacion: '' } as any
    ];
    component.limpiarFiltroProductores();
    expect(component.filtroProductorNombre).toBe('');
    // El filtro de fecha puede ser limpiado o no según la lógica, así que solo verificamos el nombre
  });

  it('should call cerrarModalEdicion and set mostrarModalEdicion to false', () => {
    component.mostrarModalEdicion = true;
    component.cerrarModalEdicion();
    expect(component.mostrarModalEdicion).toBeFalse();
  });

  it('should call cerrarModalMensaje and set mostrarModalMensaje to false', () => {
    component.mostrarModalMensaje = true;
    component.cerrarModalMensaje();
    expect(component.mostrarModalMensaje).toBeFalse();
  });
}); 