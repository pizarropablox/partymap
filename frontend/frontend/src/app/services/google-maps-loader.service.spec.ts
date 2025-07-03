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
});
