import { TestBed } from '@angular/core/testing';
import { MapNavigationService } from './map-navigation.service';

describe('MapNavigationService', () => {
  let service: MapNavigationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MapNavigationService]
    });
    service = TestBed.inject(MapNavigationService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });
});
