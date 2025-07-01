import { TestBed } from '@angular/core/testing';

import { GoogleMapsLoader } from './google-maps-loader';

describe('GoogleMapsLoader', () => {
  let service: GoogleMapsLoader;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GoogleMapsLoader);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
