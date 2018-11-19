package com.cert;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.logging.Level;

@SpringBootApplication
@Log
public class SpringBootConsoleApplication implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        // Disabled banner, don't want to see the spring logo
        SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override    
    public void run(String...args) throws Exception {
        log.log(Level.INFO, "Application started with command-line arguments: {} . \n To kill this application, press Ctrl + C.", Arrays.toString(args));
    }
}