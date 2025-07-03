import { TestBed } from '@angular/core/testing';
import { GeocodingService } from './geocoding.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { GoogleMapsLoaderService } from './google-maps-loader.service';

describe('GeocodingService', () => {
  let service: GeocodingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        GeocodingService,
        {
          provide: GoogleMapsLoaderService,
          useValue: { load: () => Promise.resolve() }  // Simulación de carga
        }
      ]
    });
    service = TestBed.inject(GeocodingService);
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
});
