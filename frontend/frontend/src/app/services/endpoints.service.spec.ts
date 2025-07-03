import { TestBed } from '@angular/core/testing';
import { EndpointsService } from './endpoints.service';

describe('EndpointsService', () => {
  let service: EndpointsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EndpointsService]
    });
    service = TestBed.inject(EndpointsService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería tener una baseUrl definida', () => {
    // Asegura que la propiedad baseUrl exista y sea un string no vacío
    expect(typeof (service as any).baseUrl).toBe('string');
    expect((service as any).baseUrl.length).toBeGreaterThan(0);
  });
});
