package com.int2.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCloudConfig {
    @Bean
    Storage googleCloudStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
