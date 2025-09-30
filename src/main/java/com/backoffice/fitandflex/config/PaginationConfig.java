package com.backoffice.fitandflex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuración para paginación por defecto
 */
@Configuration
@EnableSpringDataWebSupport
public class PaginationConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        
        // Configurar valores por defecto
        resolver.setFallbackPageable(org.springframework.data.domain.PageRequest.of(0, 10));
        resolver.setMaxPageSize(100); // Máximo 100 elementos por página
        
        argumentResolvers.add(resolver);
    }
}
