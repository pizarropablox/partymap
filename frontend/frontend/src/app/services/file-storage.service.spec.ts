import { TestBed } from '@angular/core/testing';
import { FileStorageService } from './file-storage.service';

describe('FileStorageService', () => {
  let service: FileStorageService;

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
    TestBed.configureTestingModule({
      providers: [FileStorageService]
    });
    service = TestBed.inject(FileStorageService);
    
    // Limpiar localStorage antes de cada prueba
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería guardar imagen en storage', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const imagePath = 'test-image.jpg';
    
    const result = await service.saveImageToStorage(file, imagePath);
    expect(result).toBe(imagePath);
  });

  it('debería obtener imagen del storage', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const imagePath = 'test-image.jpg';
    
    await service.saveImageToStorage(file, imagePath);
    const result = service.getImageFromStorage(imagePath);
    expect(result).toBeTruthy();
    expect(result).toContain('data:image/jpeg;base64');
  });

  it('debería retornar null para imagen inexistente', () => {
    const result = service.getImageFromStorage('inexistente.jpg');
    expect(result).toBeNull();
  });

  it('debería obtener todas las imágenes almacenadas', async () => {
    const file1 = new File(['test1'], 'test1.jpg', { type: 'image/jpeg' });
    const file2 = new File(['test2'], 'test2.jpg', { type: 'image/jpeg' });
    
    await service.saveImageToStorage(file1, 'test1.jpg');
    await service.saveImageToStorage(file2, 'test2.jpg');
    
    const storedImages = service.getStoredImages();
    expect(Object.keys(storedImages)).toContain('test1.jpg');
    expect(Object.keys(storedImages)).toContain('test2.jpg');
  });

  it('debería eliminar imagen del storage', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const imagePath = 'test-image.jpg';
    
    await service.saveImageToStorage(file, imagePath);
    expect(service.imageExists(imagePath)).toBeTrue();
    
    service.removeImageFromStorage(imagePath);
    expect(service.imageExists(imagePath)).toBeFalse();
  });

  it('debería limpiar todas las imágenes', async () => {
    const file1 = new File(['test1'], 'test1.jpg', { type: 'image/jpeg' });
    const file2 = new File(['test2'], 'test2.jpg', { type: 'image/jpeg' });
    
    await service.saveImageToStorage(file1, 'test1.jpg');
    await service.saveImageToStorage(file2, 'test2.jpg');
    
    service.clearAllImages();
    const storedImages = service.getStoredImages();
    expect(Object.keys(storedImages).length).toBe(0);
  });

  it('debería verificar si una imagen existe', async () => {
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const imagePath = 'test-image.jpg';
    
    expect(service.imageExists(imagePath)).toBeFalse();
    
    await service.saveImageToStorage(file, imagePath);
    expect(service.imageExists(imagePath)).toBeTrue();
  });

  it('debería manejar error al guardar imagen', async () => {
    const invalidFile = new File([''], 'test.jpg', { type: 'image/jpeg' });
    const imagePath = 'test-image.jpg';
    
    // Simular error en FileReader
    spyOn(FileReader.prototype, 'readAsDataURL').and.throwError('Test error');
    
    await expectAsync(service.saveImageToStorage(invalidFile, imagePath)).toBeRejected();
  });

  it('debería manejar error al obtener imágenes almacenadas si el JSON es inválido', () => {
    spyOn(console, 'error'); // Silencia el error esperado
    localStorage.setItem('event_images', 'invalid json');
    const images = service.getStoredImages();
    expect(images).toEqual({});
    expect(console.error).toHaveBeenCalled();
  });
});
