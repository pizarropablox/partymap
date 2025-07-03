import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FileStorageService } from './file-storage.service';

describe('FileStorageService', () => {
  let service: FileStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FileStorageService]
    });
    service = TestBed.inject(FileStorageService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });
});
