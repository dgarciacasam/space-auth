package com.space.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StartupInfo implements CommandLineRunner {

    private final Environment environment;

    public StartupInfo(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        log.info("==========================================");
        log.info("🚀 Space Auth Service iniciado");
        log.info("📦 Entorno activo: {}", profile.toUpperCase());
        log.info("🌐 Puerto: {}", environment.getProperty("server.port", "8080"));
        log.info("==========================================");
    }
}
