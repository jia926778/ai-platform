package com.example.aiplatform.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的滑动窗口限流器
 * <p>
 * 使用Redis有序集合（Sorted Set）实现滑动窗口限流算法。
 * 以时间戳作为分数，自动清理过期的请求记录，
 * 实现精确的时间窗口内请求数量控制。
 * Redis不可用时自动降级为放行所有请求。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Component
public class RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 判断指定Key在时间窗口内是否允许继续请求
     * <p>
     * 使用Redis ZSet实现滑动窗口：
     * 1. 清理窗口外的过期记录
     * 2. 检查当前窗口内请求数是否超限
     * 3. 未超限则记录本次请求
     * </p>
     *
     * @param key             限流标识（通常为用户名+请求路径或IP+请求路径）
     * @param limit           时间窗口内允许的最大请求数
     * @param durationSeconds 时间窗口大小（秒）
     * @return 允许请求返回true，超限返回false
     */
    public boolean isAllowed(String key, int limit, int durationSeconds) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (durationSeconds * 1000L);

        try {
            // 清理滑动窗口外的过期请求记录
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

            // 统计当前窗口内的请求数量
            Long count = redisTemplate.opsForZSet().zCard(redisKey);
            if (count != null && count >= limit) {
                return false;  // 超过限流阈值，拒绝请求
            }

            // 记录本次请求（以当前时间戳作为分数和值）
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
            // 设置Key过期时间，防止无用数据占用内存
            redisTemplate.expire(redisKey, durationSeconds, TimeUnit.SECONDS);

            return true;
        } catch (Exception e) {
            // Redis不可用时降级处理：允许请求通过
            return true;
        }
    }
}
