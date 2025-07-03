import { TestBed } from '@angular/core/testing';
import { ReservaService } from './reserva.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ReservaService', () => {
  let service: ReservaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReservaService]
    });
    service = TestBed.inject(ReservaService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });
});
