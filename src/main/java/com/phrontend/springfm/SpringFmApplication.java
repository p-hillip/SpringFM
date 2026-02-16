package com.phrontend.springfm;

import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, StorageProperties.class})
public class SpringFmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFmApplication.class, args);
    }

}
