package com.leo.test.luxoft.configuration;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Senchenko Victor
 */
@Configuration
@EnableCaching
public class CachingConfig {
    @Value("${spring.my.cache.size}")
    private int cacheSize;

    @Bean
    public CacheManager cacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(getCacheConfiguration("movie"));
        config.addCache(getCacheConfiguration("comment"));
        net.sf.ehcache.CacheManager cacheManager = new net.sf.ehcache.CacheManager(config);
        return new EhCacheCacheManager(cacheManager);
    }

    private CacheConfiguration getCacheConfiguration(String name) {
        CacheConfiguration cacheConfiguration = new CacheConfiguration(name, cacheSize);
        cacheConfiguration.setMemoryStoreEvictionPolicy("LFU");
        return cacheConfiguration;
    }
}