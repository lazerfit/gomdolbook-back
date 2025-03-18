package com.gomdolbook.api.config;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gomdolbook.api.api.dto.AladinAPI;
import com.gomdolbook.api.service.Auth.SecurityService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@EnableCaching
public class CacheConfig {

    private final SecurityService securityService;

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .initialCapacity(100)
            .maximumSize(10_000);
    }

    @Bean
    public AsyncCache<String, AladinAPI> caffeineAsyncCacheForAladin() {
        return Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.HOURS)
            .initialCapacity(100)
            .maximumSize(10_000)
            .buildAsync();
    }

    @Bean
    public KeyGenerator customKeyGenerator() {
        return new CustomKeyGenerator(securityService);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        var caffeineCacheManager = new CaffeineCacheManager("readingLogCache", "statusCache",
            "bookByIsbnCache", "libraryCache", "collectionListCache", "collectionCache");
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
