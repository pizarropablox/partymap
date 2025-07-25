import { TestBed } from '@angular/core/testing';
import { GoogleMapsLoaderService } from '../services/google-maps-loader.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('GoogleMapsLoaderService', () => {
  let service: GoogleMapsLoaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GoogleMapsLoaderService]
    });

    service = TestBed.inject(GoogleMapsLoaderService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });
});
