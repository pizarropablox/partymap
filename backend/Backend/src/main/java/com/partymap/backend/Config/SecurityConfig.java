package com.partymap.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/evento/**", "/productor/**", "/reserva/**") // Ignorar endpoints de API
            )
            .authorizeHttpRequests(authorize -> authorize
                // Configuración de seguridad para eventos
                // GET /evento - Obtener todos los eventos (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento").permitAll()
                // GET /evento/{id} - Obtener evento por ID (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/{id}").permitAll()
                // POST /evento/crear - Crear nuevo evento (solo productores y administradores)
                .requestMatchers(HttpMethod.POST, "/evento/crear").permitAll()
                // POST /evento/con-ubicacion - Crear evento con ubicación (solo productores y administradores)
                .requestMatchers(HttpMethod.POST, "/evento/con-ubicacion").permitAll()
                // PUT /evento/{id} - Actualizar evento (solo el productor propietario o administradores)
                .requestMatchers(HttpMethod.PUT, "/evento/{id}").authenticated()
                // DELETE /evento/{id} - Eliminar evento (solo el productor propietario o administradores)
                .requestMatchers(HttpMethod.DELETE, "/evento/{id}").authenticated()
                // GET /evento/buscar - Buscar eventos con filtros (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/buscar").permitAll()
                // GET /evento/proximos - Obtener eventos próximos (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/proximos").permitAll()
                // GET /evento/disponibles - Obtener eventos disponibles (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/disponibles").permitAll()
                // GET /evento/{id}/cupos - Obtener cupos disponibles (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/{id}/cupos").permitAll()
                
                // Configuración de seguridad para productores
                // GET /productor - Obtener todos los productores (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor").permitAll()
                // GET /productor/{id} - Obtener productor por ID (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/{id}").permitAll()
                // GET /productor/rut/{rut} - Obtener productor por RUT (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/rut/{rut}").permitAll()
                // POST /productor - Crear nuevo productor (solo administradores)
                .requestMatchers(HttpMethod.POST, "/productor").authenticated()
                // PUT /productor/{id} - Actualizar productor (solo el propio productor o administradores)
                .requestMatchers(HttpMethod.PUT, "/productor/{id}").authenticated()
                // DELETE /productor/{id} - Eliminar productor (solo administradores)
                .requestMatchers(HttpMethod.DELETE, "/productor/{id}").authenticated()
                // GET /productor/buscar - Buscar productores con filtros (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/buscar").permitAll()
                // GET /productor/empresa/{nombreEmpresa} - Obtener productores por empresa (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/empresa/{nombreEmpresa}").permitAll()
                // GET /productor/validar-rut - Validar RUT (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/validar-rut").permitAll()
                // GET /productor/existe-rut - Verificar existencia de RUT (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/productor/existe-rut").permitAll()
                
                // Configuración de seguridad para reservas
                // GET /reserva - Solo usuarios autenticados pueden ver reservas
                .requestMatchers(HttpMethod.GET, "/reserva").authenticated()
                // GET /reserva/{id} - Solo usuarios autenticados pueden ver reservas específicas
                .requestMatchers(HttpMethod.GET, "/reserva/{id}").authenticated()
                // POST /reserva - Solo clientes pueden crear reservas
                .requestMatchers(HttpMethod.POST, "/reserva").authenticated()
                // PUT /reserva/{id} - Solo usuarios autenticados pueden actualizar reservas
                .requestMatchers(HttpMethod.PUT, "/reserva/{id}").authenticated()
                // DELETE /reserva/{id} - Solo administradores pueden eliminar reservas
                .requestMatchers(HttpMethod.DELETE, "/reserva/{id}").authenticated()
                
                // Endpoints específicos de reservas
                // GET /reserva/usuario/{usuarioId} - Solo el propio usuario o admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/usuario/{usuarioId}").authenticated()
                // GET /reserva/evento/{eventoId} - Solo admin/productor pueden ver reservas de eventos
                .requestMatchers(HttpMethod.GET, "/reserva/evento/{eventoId}").authenticated()
                // GET /reserva/activas - Solo admin/productor pueden ver todas las reservas activas
                .requestMatchers(HttpMethod.GET, "/reserva/activas").authenticated()
                // GET /reserva/canceladas - Solo admin/productor pueden ver todas las reservas canceladas
                .requestMatchers(HttpMethod.GET, "/reserva/canceladas").authenticated()
                
                // Endpoints de gestión de reservas
                // PUT /reserva/{id}/cancelar - Solo el propio usuario o admin
                .requestMatchers(HttpMethod.PUT, "/reserva/{id}/cancelar").authenticated()
                // PUT /reserva/{id}/reactivar - Solo admin puede reactivar
                .requestMatchers(HttpMethod.PUT, "/reserva/{id}/reactivar").authenticated()
                
                // Endpoints de consulta y estadísticas
                // GET /reserva/{id}/precio-total - Solo el propio usuario o admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/{id}/precio-total").authenticated()
                // GET /reserva/rango-fechas - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/rango-fechas").authenticated()
                // GET /reserva/usuario/{usuarioId}/rango-fechas - Solo el propio usuario o admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/usuario/{usuarioId}/rango-fechas").authenticated()
                // GET /reserva/evento/{eventoId}/rango-fechas - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/evento/{eventoId}/rango-fechas").authenticated()
                // GET /reserva/precio-minimo - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/precio-minimo").authenticated()
                // GET /reserva/precio-maximo - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/precio-maximo").authenticated()
                // GET /reserva/cantidad/{cantidad} - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/cantidad/{cantidad}").authenticated()
                // GET /reserva/cantidad-minima - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/cantidad-minima").authenticated()
                // GET /reserva/{id}/activa - Solo el propio usuario o admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/{id}/activa").authenticated()
                // GET /reserva/{id}/cancelada - Solo el propio usuario o admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/{id}/cancelada").authenticated()
                // GET /reserva/buscar - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/buscar").authenticated()
                // GET /reserva/estadisticas - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/estadisticas").authenticated()
                // GET /reserva/estadisticas-basicas - Solo admin/productor
                .requestMatchers(HttpMethod.GET, "/reserva/estadisticas-basicas").authenticated()
                
                // Requerir autenticación para todos los demás endpoints
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
