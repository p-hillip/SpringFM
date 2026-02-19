package com.phrontend.springfm;

import com.phrontend.springfm.auth.AuthService;
import com.phrontend.springfm.auth.JwtService;
import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.config.SecurityConfig;
import com.phrontend.springfm.files.FileSearchService;
import com.phrontend.springfm.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringFmApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verify Spring context loads successfully
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void jwtPropertiesAreConfigured() {
        // Verify JWT properties bean is loaded
        JwtProperties jwtProperties = applicationContext.getBean(JwtProperties.class);
        assertThat(jwtProperties).isNotNull();
        assertThat(jwtProperties.issuer()).isNotNull();
        assertThat(jwtProperties.secret()).isNotNull();
    }

    @Test
    void securityConfigIsLoaded() {
        // Verify security configuration is loaded
        SecurityConfig securityConfig = applicationContext.getBean(SecurityConfig.class);
        assertThat(securityConfig).isNotNull();
    }

    @Test
    void authServiceIsConfigured() {
        // Verify auth service is available
        AuthService authService = applicationContext.getBean(AuthService.class);
        assertThat(authService).isNotNull();
    }

    @Test
    void jwtServiceIsConfigured() {
        // Verify JWT service is available
        JwtService jwtService = applicationContext.getBean(JwtService.class);
        assertThat(jwtService).isNotNull();
    }

    @Test
    void userServiceIsConfigured() {
        // Verify user service is available
        UserService userService = applicationContext.getBean(UserService.class);
        assertThat(userService).isNotNull();
    }

    @Test
    void fileSearchServiceIsConfigured() {
        // Verify file search service is available
        FileSearchService fileSearchService = applicationContext.getBean(FileSearchService.class);
        assertThat(fileSearchService).isNotNull();
    }

    @Test
    void allControllersAreRegistered() {
        // Verify all controllers are registered
        String[] controllerBeans = applicationContext.getBeanNamesForAnnotation(
                org.springframework.web.bind.annotation.RestController.class
        );
        assertThat(controllerBeans).isNotEmpty();
        assertThat(controllerBeans).hasSizeGreaterThanOrEqualTo(2); // AuthController + SearchController at minimum
    }
}
