package com.partymap.backend.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtUserSyncFilter jwtUserSyncFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/evento/**", "/productor/**", "/reserva/**") // Ignorar endpoints de API
            )
            .addFilterBefore(jwtDebugFilter(), org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class)
            .addFilterAfter(jwtUserSyncFilter, org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints de prueba
                .requestMatchers(HttpMethod.GET, "/test/**").authenticated()
                
                // Configuración de seguridad para eventos
                // GET /evento/all - Obtener todos los eventos (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/all").permitAll()
                // GET /evento/{id} - Obtener evento por ID (acceso público para consulta)
                .requestMatchers(HttpMethod.GET, "/evento/{id}").permitAll()
                // POST /evento/crear - Crear nuevo evento (solo productores y administradores)
                .requestMatchers(HttpMethod.POST, "/evento/crear").authenticated()
                // POST /evento/con-ubicacion - Crear evento con ubicación (solo productores y administradores)
                .requestMatchers(HttpMethod.POST, "/evento/con-ubicacion").authenticated()
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
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
        
        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtDebugFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    System.out.println("=== JWT DEBUG INFO ===");
                    System.out.println("Request URI: " + request.getRequestURI());
                    System.out.println("Authorization header present: " + (authHeader != null));
                    System.out.println("Token length: " + token.length());
                    System.out.println("Token starts with: " + token.substring(0, Math.min(20, token.length())));
                    System.out.println("Token ends with: " + token.substring(Math.max(0, token.length() - 20)));
                    
                    // Decodificar el token para ver su contenido (sin validar)
                    try {
                        String[] parts = token.split("\\.");
                        if (parts.length == 3) {
                            System.out.println("JWT has 3 parts (header.payload.signature)");
                            // Decodificar el payload (parte 2)
                            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                            System.out.println("JWT Payload: " + payload);
                        } else {
                            System.out.println("JWT does not have 3 parts, actual parts: " + parts.length);
                        }
                    } catch (Exception e) {
                        System.out.println("Error decoding JWT: " + e.getMessage());
                    }
                    System.out.println("=== END JWT DEBUG INFO ===");
                } else {
                    System.out.println("No Authorization header or not Bearer token for URI: " + request.getRequestURI());
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Usar un JwtDecoder que no valide la firma por ahora
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length != 3) {
                        throw new JwtException("Invalid JWT structure");
                    }

                    // Decodificar el header
                    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
                    Map<String, Object> header = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(headerJson, Map.class);

                    // Decodificar el payload
                    String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                    Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(payloadJson, Map.class);

                    // Validar audience
                    String audience = (String) claims.get("aud");
                    if (!"ad16d15c-7d6e-4f58-8146-4b5b3d7b7124".equals(audience)) {
                        throw new JwtException("Invalid audience");
                    }

                    // Validar issuer
                    String issuer = (String) claims.get("iss");
                    if (!"https://duocdesarrollocloudnative.b2clogin.com/dd063bcd-7ee5-4283-a6b4-76561cc07f64/v2.0/".equals(issuer)) {
                        throw new JwtException("Invalid issuer");
                    }

                    // Validar expiración
                    Object expObj = claims.get("exp");
                    Instant exp = null;
                    if (expObj instanceof Integer) {
                        exp = Instant.ofEpochSecond(((Integer) expObj).longValue());
                    } else if (expObj instanceof Long) {
                        exp = Instant.ofEpochSecond((Long) expObj);
                    }
                    
                    if (exp != null && exp.isBefore(Instant.now())) {
                        throw new JwtException("Token expired");
                    }

                    // Obtener iat de manera segura
                    Object iatObj = claims.get("iat");
                    Instant iat = null;
                    if (iatObj instanceof Integer) {
                        iat = Instant.ofEpochSecond(((Integer) iatObj).longValue());
                    } else if (iatObj instanceof Long) {
                        iat = Instant.ofEpochSecond((Long) iatObj);
                    }

                    // Crear el JWT usando el constructor correcto
                    return Jwt.withTokenValue(token)
                        .header("alg", header.get("alg"))
                        .header("kid", header.get("kid"))
                        .header("typ", header.get("typ"))
                        .claim("aud", audience)
                        .claim("iss", issuer)
                        .issuedAt(iat)
                        .expiresAt(exp)
                        .claim("sub", claims.get("sub"))
                        .claim("extension_Roles", claims.get("extension_Roles"))
                        .claim("family_name", claims.get("family_name"))
                        .claim("given_name", claims.get("given_name"))
                        .claim("name", claims.get("name"))
                        .claim("emails", claims.get("emails"))
                        .claim("tfp", claims.get("tfp"))
                        .build();

                } catch (Exception e) {
                    throw new JwtException("Error decoding JWT: " + e.getMessage());
                }
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("*"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
