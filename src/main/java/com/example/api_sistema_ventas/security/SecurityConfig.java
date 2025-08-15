package com.example.api_sistema_ventas.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("#{'${app.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter authenticationFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter authenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas generales
                        .requestMatchers("/ws/**", "/topic/**", "/app/**", "/user/**", "/queue/**", "/socket/**", "/sockjs-node/**", "/sockjs/**").permitAll()

                        // Rutas para Login
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleado/login/{id}").permitAll()

                        // Rutas para Admin
                        .requestMatchers(HttpMethod.GET, "/api/admin/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/admin/{id}").permitAll()

                        // Rutas para Categorías
                        .requestMatchers(HttpMethod.GET, "/api/categoria", "/api/categoria/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categoria").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/categoria/{id}").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "api/categoria/{id}/estado").permitAll()

                        // Rutas para Proveedores
                        .requestMatchers(HttpMethod.GET, "/api/proveedores", "/api/proveedores/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/proveedores").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/proveedores/*/estado", "/api/proveedores/{id}").permitAll()

                        // Rutas para Pedidos
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()

                        // Rutas para Empleados
                        .requestMatchers(HttpMethod.GET, "/api/empleados", "/api/empleados/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleados").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/empleados/{id}","/api/empleados/{id}/estado").permitAll()

                        // Rutas para Clientes
                        .requestMatchers(HttpMethod.GET, "/api/clientes", "/api/clientes/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/{id}", "/api/clientes/{id}/estado").permitAll()

                        // Rutas para Productos
                        .requestMatchers(HttpMethod.GET, "/api/producto", "/api/producto/{id}", "/api/producto/categoria/{idCategoria}", "/api/producto/activos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/producto").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/producto/{id}", "/api/producto/{id}/estado").permitAll()

                        // Rutas para Ventas
                        .requestMatchers(HttpMethod.GET, "/api/ventas", "/api/ventas/{id}", "/api/ventas/{id}/nota-pdf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ventas").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/{id}/estado").permitAll()

                        // Rutas para Facturas
                        .requestMatchers(HttpMethod.GET, "/api/facturacion", "/api/facturacion/{id}", "/api/facturacion/{id}/pdf", "/api/facturacion/venta/{idVenta}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/facturacion/crear").permitAll()


                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) corsConfigurationSource();
        return new CorsFilter(source);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}