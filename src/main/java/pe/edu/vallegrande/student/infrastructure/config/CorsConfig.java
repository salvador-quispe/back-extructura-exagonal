package pe.edu.vallegrande.student.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para que cualquier frontend (Angular en :4200, Docker en
 * :8095, u otro origen) pueda llamar a esta API desde el navegador.
 *
 * Sin esto, el navegador bloquea las peticiones con el error:
 * "No 'Access-Control-Allow-Origin' header is present..."
 *
 * CAMBIA "allowedOrigins" (o la variable de entorno CORS_ALLOWED_ORIGINS)
 * si en produccion quieres restringir a un dominio especifico en vez de "*".
 * Ver docs/PIPELINES.md.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${CORS_ALLOWED_ORIGINS:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
