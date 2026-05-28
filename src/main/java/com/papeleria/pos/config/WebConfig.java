package com.papeleria.pos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SeguridadInterceptor seguridadInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // El guardia revisará TODO, excepto los archivos de diseño y la pantalla de login
        registry.addInterceptor(seguridadInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/login", "/login-post", "/logout");
    }
}