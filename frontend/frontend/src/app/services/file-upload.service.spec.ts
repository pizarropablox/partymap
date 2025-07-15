import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FileUploadService } from './file-upload.service';
import { FileStorageService } from './file-storage.service';

describe('FileUploadService', () => {
  let service: FileUploadService;
  let fileStorageService: jasmine.SpyObj<FileStorageService>;

  beforeAll(() => {
    try {
      spyOn(window.location, 'assign').and.callFake(() => {});
    } catch (e) {}
    try {
      spyOn(window.location, 'replace').and.callFake(() => {});
    } catch (e) {}
    try {
      spyOn(window.location, 'reload').and.callFake(() => {});
    } catch (e) {}
  });

  beforeEach(() => {
    const spy = jasmine.createSpyObj('FileStorageService', [
      'saveImageToStorage', 
      'getImageFromStorage'
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        FileUploadService,
        { provide: FileStorageService, useValue: spy }
      ]
    });
    service = TestBed.inject(FileUploadService);
    fileStorageService = TestBed.inject(FileStorageService) as jasmine.SpyObj<FileStorageService>;
    
    // Configurar token de prueba
    localStorage.setItem('jwt', 'fake-token');
  });

  afterEach(() => {
    localStorage.removeItem('jwt');
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería subir imagen exitosamente', (done) => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const mockImagePath = '/images/eventos/test.jpg';
    
    fileStorageService.saveImageToStorage.and.returnValue(Promise.resolve(mockImagePath));

    service.uploadImage(file).subscribe(response => {
      expect(response.imagePath).toContain('/images/eventos/');
      expect(fileStorageService.saveImageToStorage).toHaveBeenCalledWith(file, jasmine.any(String));
      done();
    });
  });

  it('debería manejar error al guardar imagen', async () => {
    spyOn(console, 'error'); // Silencia el error esperado
    fileStorageService.saveImageToStorage.and.returnValue(Promise.reject(new Error('Storage error')));
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    await expectAsync(service.uploadImage(file).toPromise()).toBeRejectedWithError('Storage error');
    expect(console.error).toHaveBeenCalled();
  });

  it('debería validar archivo de imagen válido', () => {
    const validFile = new File([''], 'test.jpg', { type: 'image/jpeg' });
    const validPngFile = new File([''], 'test.png', { type: 'image/png' });
    const validGifFile = new File([''], 'test.gif', { type: 'image/gif' });
    const validWebpFile = new File([''], 'test.webp', { type: 'image/webp' });

    expect(service.validateImageFile(validFile)).toBeTrue();
    expect(service.validateImageFile(validPngFile)).toBeTrue();
    expect(service.validateImageFile(validGifFile)).toBeTrue();
    expect(service.validateImageFile(validWebpFile)).toBeTrue();
  });

  it('debería rechazar archivo de imagen inválido', () => {
    const invalidFile = new File([''], 'test.txt', { type: 'text/plain' });
    const invalidPdfFile = new File([''], 'test.pdf', { type: 'application/pdf' });

    expect(service.validateImageFile(invalidFile)).toBeFalse();
    expect(service.validateImageFile(invalidPdfFile)).toBeFalse();
  });

  it('debería rechazar archivo demasiado grande', () => {
    const largeFile = new File(['x'.repeat(6 * 1024 * 1024)], 'test.jpg', { type: 'image/jpeg' });
    expect(service.validateImageFile(largeFile)).toBeFalse();
  });

  it('debería aceptar archivo de tamaño válido', () => {
    const validSizeFile = new File(['x'.repeat(1024)], 'test.jpg', { type: 'image/jpeg' });
    expect(service.validateImageFile(validSizeFile)).toBeTrue();
  });

  it('debería convertir archivo a base64', async () => {
    const file = new File(['test content'], 'test.jpg', { type: 'image/jpeg' });
    
    const result = await service.fileToBase64(file);
    expect(result).toContain('data:image/jpeg;base64');
  });

  it('debería manejar error al convertir archivo a base64', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    
    // Simular error en FileReader
    spyOn(FileReader.prototype, 'readAsDataURL').and.throwError('Test error');
    
    await expectAsync(service.fileToBase64(file)).toBeRejected();
  });

  it('debería obtener imagen del storage', () => {
    const imagePath = 'test-image.jpg';
    const mockImageData = 'data:image/jpeg;base64,test';
    
    fileStorageService.getImageFromStorage.and.returnValue(mockImageData);
    
    const result = service.getImageUrl(imagePath);
    expect(result).toBe(mockImageData);
    expect(fileStorageService.getImageFromStorage).toHaveBeenCalledWith(imagePath);
  });

  it('debería manejar imagen path vacío', () => {
    fileStorageService.getImageFromStorage.and.returnValue(null);
    
    const result = service.getImageUrl('');
    expect(result).toBeNull();
  });

  it('debería manejar imagen path null', () => {
    fileStorageService.getImageFromStorage.and.returnValue(null);
    
    const result = service.getImageUrl(null as any);
    expect(result).toBeNull();
  });

  it('debería validar múltiples archivos', () => {
    const files = [
      new File([''], 'test1.jpg', { type: 'image/jpeg' }),
      new File([''], 'test2.png', { type: 'image/png' }),
      new File([''], 'test3.txt', { type: 'text/plain' })
    ];

    const validFiles = files.filter(file => service.validateImageFile(file));
    expect(validFiles.length).toBe(2);
  });

  it('debería manejar archivo sin tipo MIME', () => {
    const fileWithoutType = new File([''], 'test');
    expect(service.validateImageFile(fileWithoutType)).toBeFalse();
  });

  it('debería generar nombres únicos para archivos', (done) => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    
    fileStorageService.saveImageToStorage.and.returnValue(Promise.resolve('/images/eventos/test.jpg'));

    service.uploadImage(file).subscribe(response => {
      expect(response.imagePath).toMatch(/\/images\/eventos\/evento_\d+_[a-z0-9]+\.jpg$/);
      done();
    });
  });
});
