package com.nova.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 基于 Redis 的布隆过滤器，用于防缓存穿透。
 * <p>
 * 利用 Redis BIT 操作（SETBIT/GETBIT）实现分布式、实例安全的过滤。
 * 误判率由位图大小（{@code DEFAULT_SIZE}）和哈希函数数量（{@code HASH_NUM}）控制。
 * </p>
 * <pre>
 *   size=100_000, hashNum=3, elements=1000  =>  误判率 ~0.1%
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisBloomFilter {

    private static final String KEY_PREFIX = "bloom:";

    /** 位图大小（比特数）。100K bits = 12.5 KB 每个过滤器。 */
    private static final long DEFAULT_SIZE = 100_000L;

    /** 每个元素的哈希函数数量。 */
    private static final int HASH_NUM = 3;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 向布隆过滤器添加一个值。
     */
    public void add(String filterName, Object value) {
        String key = KEY_PREFIX + filterName;
        String str = String.valueOf(value);
        for (int i = 0; i < HASH_NUM; i++) {
            long index = hash(str, i) % DEFAULT_SIZE;
            redisTemplate.opsForValue().setBit(key, index, true);
        }
    }

    /**
     * 检查一个值是否可能存在于布隆过滤器中。
     *
     * @return {@code false} 表示该值一定不存在；
     *         {@code true} 表示可能存在（存在误判可能）
     */
    public boolean mightContain(String filterName, Object value) {
        String key = KEY_PREFIX + filterName;
        String str = String.valueOf(value);
        for (int i = 0; i < HASH_NUM; i++) {
            long index = hash(str, i) % DEFAULT_SIZE;
            if (!Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, index))) {
                return false;
            }
        }
        return true;
    }

    // --- 哈希函数 ---

    /**
     * 计算值的第 i 个哈希。
     * 采用双重哈希: h_i = h1 + i * h2
     */
    private long hash(String str, int i) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int h1 = murmur32(bytes, 0);
        int h2 = murmur32(bytes, h1);
        return (h1 + (long) i * h2) & 0x7FFFFFFFL;
    }

    /**
     * 简单的 32 位 Murmur 哈希。
     */
    private static int murmur32(byte[] data, int seed) {
        int c1 = 0xCC9E2D51;
        int c2 = 0x1B873593;
        int h = seed;
        int len = data.length;
        int i = 0;

        while (i + 4 <= len) {
            int k = (data[i] & 0xFF)
                  | ((data[i + 1] & 0xFF) << 8)
                  | ((data[i + 2] & 0xFF) << 16)
                  | ((data[i + 3] & 0xFF) << 24);
            k *= c1;
            k = Integer.rotateLeft(k, 15);
            k *= c2;
            h ^= k;
            h = Integer.rotateLeft(h, 13);
            h = h * 5 + 0xE6546B64;
            i += 4;
        }

        int k = 0;
        int remaining = len - i;
        if (remaining >= 3) k ^= (data[i + 2] & 0xFF) << 16;
        if (remaining >= 2) k ^= (data[i + 1] & 0xFF) << 8;
        if (remaining >= 1) {
            k ^= (data[i] & 0xFF);
            k *= c1;
            k = Integer.rotateLeft(k, 15);
            k *= c2;
            h ^= k;
        }

        h ^= len;
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;
        return h;
    }
}
