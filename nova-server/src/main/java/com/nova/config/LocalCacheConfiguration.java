package com.nova.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 用户私有数据本地缓存（Caffeine，L1）。
 * <p>
 * 与公共热点数据（菜品/套餐/分类，走 Redis L2）区分：
 * <ul>
 *   <li>私有数据按用户维度、基数大、变更频繁，放本地进程内缓存，省去序列化与网络往返；</li>
 *   <li>不写入共享 Redis，做到隐私隔离；</li>
 *   <li>订单 TTL 60s（写频繁，配合写时前缀精确失效），地址 TTL 10min（变更少）。</li>
 * </ul>
 * 该 CacheManager 通过 {@code @Cacheable(cacheManager = "caffeineCacheManager")} 显式引用。
 */
@Configuration
public class LocalCacheConfiguration {

    /** 本地缓存名称：用户历史订单。 */
    public static final String USER_ORDERS = "user:orders";
    /** 本地缓存名称：用户收货地址。 */
    public static final String USER_ADDRESS = "user:address";

    /** 本地缓存总开关（默认开启），可通过 -Dnova.cache.local.enabled=false 关闭，用于对照压测。 */
    @Value("${nova.cache.local.enabled:true}")
    private boolean enabled;

    @Bean
    public CacheManager caffeineCacheManager() {
        // 关闭时最大容量设为 0，等价于不缓存
        long ordersMax = enabled ? 5_000 : 0;
        long addressMax = enabled ? 5_000 : 0;
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // 默认：兜底配置，防止未显式注册的缓存名无界增长
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(enabled ? 10_000 : 0)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        // 历史订单：写频繁，短 TTL + 写时前缀精确失效
        manager.registerCustomCache(USER_ORDERS, Caffeine.newBuilder()
                .maximumSize(ordersMax)
                .expireAfterWrite(Duration.ofSeconds(60))
                .recordStats()
                .build());
        // 收货地址：变更少，长 TTL
        manager.registerCustomCache(USER_ADDRESS, Caffeine.newBuilder()
                .maximumSize(addressMax)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build());
        return manager;
    }
}
