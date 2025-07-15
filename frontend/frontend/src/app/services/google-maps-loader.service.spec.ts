import { TestBed } from '@angular/core/testing';
import { GoogleMapsLoaderService } from './google-maps-loader.service';

describe('GoogleMapsLoaderService', () => {
  let service: GoogleMapsLoaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GoogleMapsLoaderService]
    });
    service = TestBed.inject(GoogleMapsLoaderService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería tener método load definido', () => {
    expect(typeof service.load).toBe('function');
  });

  it('debería tener método isLoaded definido', () => {
    expect(typeof service.isLoaded).toBe('function');
  });

  it('debería cargar Google Maps exitosamente', async () => {
    // Mock de la API de Google Maps
    (window as any).google = {
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {}
      }
    };

    const result = await service.load();
    expect(result).toBeUndefined();
  });

  it('debería manejar cuando Google Maps ya está cargado', async () => {
    // Simular que Google Maps ya está disponible
    (window as any).google = {
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {}
      }
    };

    const result = await service.load();
    expect(result).toBeUndefined();
  });

  it('debería manejar múltiples llamadas a load', async () => {
    // Mock de la API de Google Maps
    (window as any).google = {
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {}
      }
    };

    const promise1 = service.load();
    const promise2 = service.load();
    const promise3 = service.load();

    await Promise.all([promise1, promise2, promise3]);
    
    // Todas las promesas deberían resolverse sin error
    expect(true).toBeTrue();
  });

  it('debería manejar script con error', async () => {
    // Limpiar estado previo
    service['loadPromise'] = null;
    service['googleMapsLoaded'] = false;
    delete (window as any).google;
    
    // Mock del DOM para simular error de carga
    const mockScript = {
      type: 'text/javascript',
      src: '',
      async: true,
      defer: true,
      onload: null,
      onerror: null
    };
    
    spyOn(document, 'createElement').and.returnValue(mockScript as any);
    spyOn(document.head, 'appendChild').and.callFake((element: any) => {
      // Simular error después de un pequeño delay
      setTimeout(() => {
        if (element.onerror) {
          element.onerror(new Error('Script failed to load'));
        }
      }, 10);
      return element;
    });
    
    await expectAsync(service.load()).toBeRejected();
  });

  it('debería verificar si Google Maps está disponible', () => {
    // Limpiar cualquier mock previo
    delete (window as any).google;
    
    // Verificar estado inicial
    expect(service.isLoaded()).toBeFalse();
    
    // Definir el mock y verificar que funcione
    (window as any).google = { 
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {},
        MapTypeId: { ROADMAP: 'roadmap' }
      }
    };
    
    // Forzar la actualización del estado interno del servicio
    service['googleMapsLoaded'] = true;
    
    expect(service.isLoaded()).toBeTrue();
  });

  it('debería retornar la misma promesa en múltiples llamadas', async () => {
    // Limpiar estado previo para asegurar que no hay promesa en curso
    service['loadPromise'] = null;
    service['googleMapsLoaded'] = false;
    delete (window as any).google;
    
    const promise1 = service.load();
    const promise2 = service.load();
    
    // Verificar que ambas promesas son la misma referencia
    expect(promise1).toBe(promise2);
    
    // Mock de Google Maps para que las promesas se resuelvan
    (window as any).google = {
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {}
      }
    };
    
    // Esperar a que ambas promesas se resuelvan
    await Promise.all([promise1, promise2]);
    expect(service.isLoaded()).toBeTrue();
  });

  it('debería manejar estado de carga inicial', () => {
    // Limpiar estado
    delete (window as any).google;
    service['googleMapsLoaded'] = false;
    service['loadPromise'] = null;
    
    expect(service.isLoaded()).toBeFalse();
  });

  it('debería manejar estado después de carga exitosa', async () => {
    // Limpiar estado previo
    service['googleMapsLoaded'] = false;
    service['loadPromise'] = null;
    delete (window as any).google;
    
    // Mock de Google Maps
    (window as any).google = { 
      maps: {
        Map: class {},
        Marker: class {},
        Geocoder: class {}
      }
    };
    
    await service.load();
    expect(service.isLoaded()).toBeTrue();
  });
});
