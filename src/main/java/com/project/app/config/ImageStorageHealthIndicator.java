package com.project.app.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

// reports DOWN when uploaded images can not be stored, shown in /actuator/health
@Component("imageStorage")
public class ImageStorageHealthIndicator implements HealthIndicator {

    private static final long MIN_FREE_MB = 100;

    @Value("${project.image}")
    private String imagePath;

    @Override
    public Health health() {
        File folder = new File(this.imagePath);
        if (!folder.exists() && !folder.mkdirs()) {
            return Health.down().withDetail("reason", "image folder can not be created").build();
        }
        if (!folder.canWrite()) {
            return Health.down().withDetail("reason", "image folder is not writable").build();
        }

        long freeMb = folder.getUsableSpace() / (1024 * 1024);
        Health.Builder builder = freeMb < MIN_FREE_MB ? Health.down() : Health.up();
        return builder
                .withDetail("path", folder.getAbsolutePath())
                .withDetail("freeSpaceMb", freeMb)
                .build();
    }
}
