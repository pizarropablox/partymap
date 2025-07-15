import { TestBed } from '@angular/core/testing';
import { GeocodingService } from './geocoding.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { GoogleMapsLoaderService } from './google-maps-loader.service';
import { of } from 'rxjs';

describe('GeocodingService', () => {
  let service: GeocodingService;
  let googleMapsLoaderService: jasmine.SpyObj<GoogleMapsLoaderService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('GoogleMapsLoaderService', ['load']);
    spy.load.and.returnValue(Promise.resolve());

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        GeocodingService,
        { provide: GoogleMapsLoaderService, useValue: spy }
      ]
    });
    
    service = TestBed.inject(GeocodingService);
    googleMapsLoaderService = TestBed.inject(GoogleMapsLoaderService) as jasmine.SpyObj<GoogleMapsLoaderService>;
  });

  afterEach(() => {
    // Limpia los mocks de window.google y geocoder
    if ((window as any).google) {
      delete (window as any).google;
    }
    if ((window as any).geocoder) {
      delete (window as any).geocoder;
    }
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería tener el método geocodeAddress definido', () => {
    expect(typeof service.geocodeAddress).toBe('function');
  });

  it('debería tener el método testGeocoding definido', () => {
    expect(typeof service.testGeocoding).toBe('function');
  });

  it('debería geocodificar una dirección usando fallback', () => {
    const address = 'Santiago Centro';
    
    service.geocodeAddress(address).subscribe(result => {
      expect(result).toBeTruthy();
      expect(result.direccion).toBe(address);
      expect(result.comuna).toBe('Santiago');
      expect(result.lat).toBe(-33.4489);
      expect(result.lng).toBe(-70.6693);
    });
  });

  it('debería geocodificar una dirección de Providencia', () => {
    const address = 'Providencia 1234';
    
    service.geocodeAddress(address).subscribe(result => {
      expect(result).toBeTruthy();
      expect(result.direccion).toBe(address);
      expect(result.comuna).toBe('Providencia');
      expect(result.lat).toBe(-33.4183);
      expect(result.lng).toBe(-70.6062);
    });
  });

  it('debería manejar direcciones no encontradas en fallback', (done) => {
    // Mock de Google Maps API
    (window as any).google = {
      maps: {
        Geocoder: class {
          geocode(request: any, callback: any) {
            // Simular que no se encuentra la dirección
            callback([], 'ZERO_RESULTS');
          }
        },
        GeocoderStatus: {
          ZERO_RESULTS: 'ZERO_RESULTS'
        }
      }
    };

    service.geocodeAddress('dirección inexistente').subscribe(result => {
      expect(result).toBeTruthy();
      expect(result.direccion).toBe('dirección inexistente');
      expect(result.comuna).toBe('Santiago');
      expect(result.lat).toBe(-33.4489);
      expect(result.lng).toBe(-70.6693);
      done();
    });
  });

  it('debería probar geocoding correctamente', () => {
    service.testGeocoding().subscribe(result => {
      expect(result).toBeTruthy();
      expect(result.direccion).toBe('Providencia 1234, Santiago');
      expect(result.comuna).toBe('Santiago');
    });
  });
});
