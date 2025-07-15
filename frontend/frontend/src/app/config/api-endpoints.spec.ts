import { ApiEndpoints } from './api-endpoints';

describe('ApiEndpoints', () => {
  const mockBaseUrl = 'http://18.235.227.189:8085';

  describe('RESERVA endpoints', () => {
    it('should have correct reserva endpoints', () => {
      expect(ApiEndpoints.RESERVA.ESTADISTICAS).toBe(`${mockBaseUrl}/reserva/estadisticas`);
      expect(ApiEndpoints.RESERVA.ESTADISTICAS_BASICAS).toBe(`${mockBaseUrl}/reserva/estadisticas-basicas`);
      expect(ApiEndpoints.RESERVA.CANTIDAD_MINIMA).toBe(`${mockBaseUrl}/reserva/cantidad-minima`);
      expect(ApiEndpoints.RESERVA.BUSCAR).toBe(`${mockBaseUrl}/reserva/buscar`);
      expect(ApiEndpoints.RESERVA.USUARIO).toBe(`${mockBaseUrl}/reserva/usuario`);
      expect(ApiEndpoints.RESERVA.ALL).toBe(`${mockBaseUrl}/reserva/all`);
      expect(ApiEndpoints.RESERVA.CREAR).toBe(`${mockBaseUrl}/reserva/crear`);
    });

    it('should generate correct POR_EVENTO endpoint with eventoId', () => {
      const eventoId = 123;
      const expectedUrl = `${mockBaseUrl}/reserva/evento/${eventoId}/usuario`;
      expect(ApiEndpoints.RESERVA.POR_EVENTO(eventoId)).toBe(expectedUrl);
    });

    it('should generate correct CANCELAR endpoint with reservaId', () => {
      const reservaId = 456;
      const expectedUrl = `${mockBaseUrl}/reserva/${reservaId}/cancelar`;
      expect(ApiEndpoints.RESERVA.CANCELAR(reservaId)).toBe(expectedUrl);
    });
  });

  describe('USUARIO endpoints', () => {
    it('should have correct usuario endpoints', () => {
      expect(ApiEndpoints.USUARIO.CURRENT).toBe(`${mockBaseUrl}/usuario/current`);
      expect(ApiEndpoints.USUARIO.ALL).toBe(`${mockBaseUrl}/usuario/all`);
      expect(ApiEndpoints.USUARIO.ESTADISTICAS).toBe(`${mockBaseUrl}/usuario/estadisticas`);
      expect(ApiEndpoints.USUARIO.CREAR_PRODUCTOR).toBe(`${mockBaseUrl}/usuario/crear-productor`);
    });

    it('should generate correct PRODUCTOR endpoint with usuarioId', () => {
      const usuarioId = 'user123';
      const expectedUrl = `${mockBaseUrl}/usuario/productor/${usuarioId}`;
      expect(ApiEndpoints.USUARIO.PRODUCTOR(usuarioId)).toBe(expectedUrl);
    });

    it('should generate correct ACTUALIZAR endpoint with usuarioId', () => {
      const usuarioId = 789;
      const expectedUrl = `${mockBaseUrl}/usuario/actualizar/${usuarioId}`;
      expect(ApiEndpoints.USUARIO.ACTUALIZAR(usuarioId)).toBe(expectedUrl);
    });

    it('should generate correct ELIMINAR endpoint with usuarioId', () => {
      const usuarioId = 101;
      const expectedUrl = `${mockBaseUrl}/usuario/eliminar/${usuarioId}`;
      expect(ApiEndpoints.USUARIO.ELIMINAR(usuarioId)).toBe(expectedUrl);
    });
  });

  describe('EVENTO endpoints', () => {
    it('should have correct evento endpoints', () => {
      expect(ApiEndpoints.EVENTO.ALL).toBe(`${mockBaseUrl}/evento/all`);
      expect(ApiEndpoints.EVENTO.CREAR).toBe(`${mockBaseUrl}/evento/crear`);
      expect(ApiEndpoints.EVENTO.MIS_ESTADISTICAS).toBe(`${mockBaseUrl}/evento/mis-estadisticas`);
    });

    it('should generate correct ACTUALIZAR endpoint with eventoId', () => {
      const eventoId = 202;
      const expectedUrl = `${mockBaseUrl}/evento/actualizar/${eventoId}`;
      expect(ApiEndpoints.EVENTO.ACTUALIZAR(eventoId)).toBe(expectedUrl);
    });

    it('should generate correct ELIMINAR endpoint with eventoId', () => {
      const eventoId = 303;
      const expectedUrl = `${mockBaseUrl}/evento/eliminar/${eventoId}`;
      expect(ApiEndpoints.EVENTO.ELIMINAR(eventoId)).toBe(expectedUrl);
    });

    it('should generate correct POR_USUARIO endpoint with productorId', () => {
      const productorId = 404;
      const expectedUrl = `${mockBaseUrl}/evento/usuario/${productorId}`;
      expect(ApiEndpoints.EVENTO.POR_USUARIO(productorId)).toBe(expectedUrl);
    });
  });

  describe('UBICACION endpoints', () => {
    it('should have correct ubicacion endpoints', () => {
      expect(ApiEndpoints.UBICACION.ALL).toBe(`${mockBaseUrl}/ubicacion/all`);
      expect(ApiEndpoints.UBICACION.BASE).toBe(`${mockBaseUrl}/ubicacion`);
    });
  });

  describe('Utility methods', () => {
    it('should build URL correctly with buildUrl method', () => {
      const path = '/test/path';
      const expectedUrl = `${mockBaseUrl}${path}`;
      expect(ApiEndpoints.buildUrl(path)).toBe(expectedUrl);
    });

    it('should return base URL with getBaseUrl method', () => {
      expect(ApiEndpoints.getBaseUrl()).toBe(mockBaseUrl);
    });

    it('should handle empty path in buildUrl', () => {
      const path = '';
      const expectedUrl = `${mockBaseUrl}${path}`;
      expect(ApiEndpoints.buildUrl(path)).toBe(expectedUrl);
    });

    it('should handle path with query parameters in buildUrl', () => {
      const path = '/test?param=value&other=123';
      const expectedUrl = `${mockBaseUrl}${path}`;
      expect(ApiEndpoints.buildUrl(path)).toBe(expectedUrl);
    });
  });

  describe('Endpoint structure', () => {
    it('should have all required endpoint categories', () => {
      expect(ApiEndpoints.RESERVA).toBeDefined();
      expect(ApiEndpoints.USUARIO).toBeDefined();
      expect(ApiEndpoints.EVENTO).toBeDefined();
      expect(ApiEndpoints.UBICACION).toBeDefined();
    });

    it('should have static readonly properties', () => {
      expect(typeof ApiEndpoints.RESERVA).toBe('object');
      expect(typeof ApiEndpoints.USUARIO).toBe('object');
      expect(typeof ApiEndpoints.EVENTO).toBe('object');
      expect(typeof ApiEndpoints.UBICACION).toBe('object');
    });
  });
}); 