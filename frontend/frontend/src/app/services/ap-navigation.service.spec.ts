import { TestBed } from '@angular/core/testing';
import { MapNavigationService } from './map-navigation.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { GeocodingService } from './geocoding.service';
import { of } from 'rxjs';

describe('MapNavigationService', () => {
  let service: MapNavigationService;
  let mockGeocodingService: jasmine.SpyObj<GeocodingService>;

  beforeEach(() => {
    const geocodingSpy = jasmine.createSpyObj('GeocodingService', ['geocodeAddress']);
    geocodingSpy.geocodeAddress.and.returnValue(of({ 
      lat: 0, 
      lng: 0, 
      direccion: 'Test Address',
      comuna: 'Test Comuna',
      formattedAddress: 'Test Address, Test Comuna'
    }));

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        MapNavigationService,
        { provide: GeocodingService, useValue: geocodingSpy }
      ]
    });
    service = TestBed.inject(MapNavigationService);
    mockGeocodingService = TestBed.inject(GeocodingService) as jasmine.SpyObj<GeocodingService>;
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería tener el método navigateToLocation definido', () => {
    expect(typeof service.navigateToLocation).toBe('function');
  });

  it('debería tener el método navigateToAddress definido', () => {
    expect(typeof service.navigateToAddress).toBe('function');
  });

  it('debería tener el método clearSelectedLocation definido', () => {
    expect(typeof service.clearSelectedLocation).toBe('function');
  });

  it('debería tener el método getSelectedLocation definido', () => {
    expect(typeof service.getSelectedLocation).toBe('function');
  });

  // Nuevos tests para aumentar cobertura
  it('should initialize with no selected location', () => {
    const location = service.getSelectedLocation();
    expect(location).toBeNull();
  });

  it('should set selected location when navigateToLocation is called', () => {
    const testLocation = { 
      nombre: 'Test Location',
      direccion: '123 Test Street',
      comuna: 'Test Comuna',
      latitud: 40.7128,
      longitud: -74.0060
    };
    service.navigateToLocation(testLocation);
    const selectedLocation = service.getSelectedLocation();
    expect(selectedLocation).toEqual(testLocation);
  });

  it('should clear selected location when clearSelectedLocation is called', () => {
    const testLocation = { 
      nombre: 'Test Location',
      direccion: '123 Test Street',
      comuna: 'Test Comuna',
      latitud: 40.7128,
      longitud: -74.0060
    };
    service.navigateToLocation(testLocation);
    service.clearSelectedLocation();
    const selectedLocation = service.getSelectedLocation();
    expect(selectedLocation).toBeNull();
  });

  it('should call geocoding service when navigateToAddress is called', () => {
    const testAddress = '123 Test Street';
    service.navigateToAddress(testAddress).subscribe();
    expect(mockGeocodingService.geocodeAddress).toHaveBeenCalledWith(testAddress);
  });

  it('should handle geocoding service response', () => {
    const testAddress = '456 Test Avenue';
    const mockResponse = { 
      lat: 34.0522, 
      lng: -118.2437,
      direccion: '456 Test Avenue',
      comuna: 'Test Comuna',
      formattedAddress: '456 Test Avenue, Test Comuna'
    };
    mockGeocodingService.geocodeAddress.and.returnValue(of(mockResponse));
    
    service.navigateToAddress(testAddress).subscribe();
    expect(mockGeocodingService.geocodeAddress).toHaveBeenCalledWith(testAddress);
  });

  it('should handle geocoding service error gracefully', () => {
    const testAddress = 'Invalid Address';
    mockGeocodingService.geocodeAddress.and.returnValue(of(null as any));
    
    expect(() => {
      service.navigateToAddress(testAddress);
    }).not.toThrow();
  });

  it('should update selected location multiple times', () => {
    const location1 = { 
      nombre: 'Location 1',
      direccion: '123 Test Street',
      comuna: 'Test Comuna',
      latitud: 40.7128,
      longitud: -74.0060
    };
    const location2 = { 
      nombre: 'Location 2',
      direccion: '456 Test Avenue',
      comuna: 'Test Comuna',
      latitud: 34.0522,
      longitud: -118.2437
    };
    
    service.navigateToLocation(location1);
    expect(service.getSelectedLocation()).toEqual(location1);
    
    service.navigateToLocation(location2);
    expect(service.getSelectedLocation()).toEqual(location2);
  });

  it('should handle null location input', () => {
    expect(() => {
      service.navigateToLocation(null as any);
    }).not.toThrow();
  });

  it('should handle undefined location input', () => {
    expect(() => {
      service.navigateToLocation(undefined as any);
    }).not.toThrow();
  });

  it('should handle empty address input', () => {
    expect(() => {
      service.navigateToAddress('');
    }).not.toThrow();
  });

  it('should handle null address input', () => {
    expect(() => {
      service.navigateToAddress(null as any);
    }).not.toThrow();
  });

  it('should maintain service state across multiple operations', () => {
    const testLocation = { 
      nombre: 'Test Location',
      direccion: '123 Test Street',
      comuna: 'Test Comuna',
      latitud: 40.7128,
      longitud: -74.0060
    };
    
    // Set location
    service.navigateToLocation(testLocation);
    expect(service.getSelectedLocation()).toEqual(testLocation);
    
    // Clear location
    service.clearSelectedLocation();
    expect(service.getSelectedLocation()).toBeNull();
    
    // Set location again
    service.navigateToLocation(testLocation);
    expect(service.getSelectedLocation()).toEqual(testLocation);
  });

  it('should handle geocoding with different address formats', () => {
    const addresses = [
      '123 Main St',
      '456 Oak Avenue, City, State',
      '789 Pine Road'
    ];
    
    addresses.forEach(address => {
      service.navigateToAddress(address).subscribe();
      expect(mockGeocodingService.geocodeAddress).toHaveBeenCalledWith(address);
    });
  });

  it('should be injectable in other components', () => {
    expect(service).toBeDefined();
    expect(typeof service.navigateToLocation).toBe('function');
    expect(typeof service.navigateToAddress).toBe('function');
    expect(typeof service.clearSelectedLocation).toBe('function');
    expect(typeof service.getSelectedLocation).toBe('function');
  });
});
