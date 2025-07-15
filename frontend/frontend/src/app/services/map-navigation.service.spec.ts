import { TestBed } from '@angular/core/testing';
import { MapNavigationService, MapLocation, NavigationResult } from './map-navigation.service';
import { GeocodingService, GeocodingResult } from './geocoding.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('MapNavigationService', () => {
  let service: MapNavigationService;
  let geocodingService: jasmine.SpyObj<GeocodingService>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const geocodingSpy = jasmine.createSpyObj('GeocodingService', ['geocodeAddress']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        MapNavigationService,
        { provide: GeocodingService, useValue: geocodingSpy }
      ]
    });
    
    service = TestBed.inject(MapNavigationService);
    geocodingService = TestBed.inject(GeocodingService) as jasmine.SpyObj<GeocodingService>;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería navegar a una ubicación específica', () => {
    const testLocation: MapLocation = {
      id: 1,
      nombre: 'Test Location',
      direccion: 'Test Address',
      comuna: 'Test Comuna',
      latitud: -33.4489,
      longitud: -70.6693
    };

    service.navigateToLocation(testLocation);

    service.selectedLocation$.subscribe(location => {
      expect(location).toEqual(testLocation);
    });
  });

  it('debería navegar a una dirección usando geocoding', (done) => {
    const testAddress = 'Providencia 1234';
    const geocodingResult: GeocodingResult = {
      lat: -33.4183,
      lng: -70.6062,
      direccion: testAddress,
      comuna: 'Providencia',
      formattedAddress: 'Providencia 1234, Providencia, Chile'
    };

    geocodingService.geocodeAddress.and.returnValue(of(geocodingResult));

    service.navigateToAddress(testAddress).subscribe(success => {
      expect(success).toBeTrue();
      
      service.selectedLocation$.subscribe(location => {
        expect(location).toBeTruthy();
        expect(location?.nombre).toBe(testAddress);
        expect(location?.direccion).toBe(geocodingResult.formattedAddress);
        expect(location?.comuna).toBe('Providencia');
        expect(location?.latitud).toBe(geocodingResult.lat);
        expect(location?.longitud).toBe(geocodingResult.lng);
        expect(location?.isGeocoded).toBeTrue();
        done();
      });
    });
  });

  it('debería manejar error en geocoding', (done) => {
    geocodingService.geocodeAddress.and.returnValue(throwError(() => new Error('Geocoding failed')));

    service.navigateToAddress('Invalid Address').subscribe(success => {
      expect(success).toBeFalse();
      done();
    });
  });

  it('debería extraer comuna correctamente de dirección formateada', () => {
    const testCases = [
      { address: 'Providencia 1234, Providencia, Chile', expected: 'Providencia' },
      { address: 'Las Condes 567, Las Condes, Chile', expected: 'Las Condes' },
      { address: 'Ñuñoa 890, Ñuñoa, Chile', expected: 'Ñuñoa' },
      { address: 'Calle Test, Santiago, Chile', expected: 'Santiago' },
      { address: 'Dirección sin comuna, Chile', expected: 'Dirección sin comuna' }
    ];

    testCases.forEach(testCase => {
      const result = service['extractComuna'](testCase.address);
      expect(result).toBe(testCase.expected);
    });
  });

  it('debería limpiar ubicación seleccionada', () => {
    const testLocation: MapLocation = {
      nombre: 'Test Location',
      direccion: 'Test Address',
      comuna: 'Test Comuna',
      latitud: -33.4489,
      longitud: -70.6693
    };

    service.navigateToLocation(testLocation);
    service.clearSelectedLocation();

    service.selectedLocation$.subscribe(location => {
      expect(location).toBeNull();
    });
  });

  it('debería obtener ubicación seleccionada', () => {
    const testLocation: MapLocation = {
      nombre: 'Test Location',
      direccion: 'Test Address',
      comuna: 'Test Comuna',
      latitud: -33.4489,
      longitud: -70.6693
    };

    service.navigateToLocation(testLocation);
    const selectedLocation = service.getSelectedLocation();
    
    expect(selectedLocation).toEqual(testLocation);
  });

  it('debería obtener direcciones exitosamente', () => {
    const origin = 'Santiago, Chile';
    const destination = 'Providencia, Chile';
    const mockResponse = {
      status: 'OK',
      routes: [{
        legs: [{
          distance: { text: '5.2 km' },
          duration: { text: '15 mins' }
        }],
        overview_polyline: { points: 'mock_polyline' }
      }]
    };

    service.getDirections(origin, destination).subscribe(result => {
      expect(result.success).toBeTrue();
      expect(result.distance).toBe('5.2 km');
      expect(result.duration).toBe('15 mins');
      expect(result.route).toBeTruthy();
    });

    const req = httpMock.expectOne(`https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}&key=AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('debería manejar error al obtener direcciones', () => {
    const origin = 'Santiago, Chile';
    const destination = 'Invalid Destination';

    service.getDirections(origin, destination).subscribe(result => {
      expect(result.success).toBeFalse();
      expect(result.error).toBe('No se encontró una ruta válida');
    });

    const req = httpMock.expectOne(`https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}&key=AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg`);
    req.flush({ status: 'NOT_FOUND', routes: [] });
  });

  it('debería manejar error HTTP al obtener direcciones', () => {
    const origin = 'Santiago, Chile';
    const destination = 'Providencia, Chile';

    service.getDirections(origin, destination).subscribe(result => {
      expect(result.success).toBeFalse();
      expect(result.error).toBe('Error al obtener direcciones');
    });

    const req = httpMock.expectOne(`https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}&key=AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg`);
    req.error(new ErrorEvent('Network error'));
  });

  it('debería calcular distancia correctamente', () => {
    const lat1 = -33.4489;
    const lng1 = -70.6693;
    const lat2 = -33.4183;
    const lng2 = -70.6062;

    const distance = service.calculateDistance(lat1, lng1, lat2, lng2);
    
    expect(distance).toBeGreaterThan(0);
    expect(typeof distance).toBe('number');
  });

  it('debería obtener tiempo estimado para distancias cortas', () => {
    const distance = 5; // 5 km
    const time = service.getEstimatedTime(distance, 30);
    
    expect(time).toBe('10 minutos');
  });

  it('debería obtener tiempo estimado para distancias largas', () => {
    const distance = 120; // 120 km
    const time = service.getEstimatedTime(distance, 60);
    
    expect(time).toBe('2h 0min');
  });

  it('debería obtener tiempo estimado con horas y minutos', () => {
    const distance = 90; // 90 km
    const time = service.getEstimatedTime(distance, 60);
    
    expect(time).toBe('1h 30min');
  });

  it('debería convertir grados a radianes correctamente', () => {
    const degrees = 180;
    const radians = service['deg2rad'](degrees);
    
    expect(radians).toBe(Math.PI);
  });
}); 