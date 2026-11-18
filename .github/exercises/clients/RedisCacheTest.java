package dev.mikoto2000.springboot.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
    "spring.cache.redis.time-to-live=1s",
    "spring.data.redis.database=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisCacheTest {
  @Autowired ProductService service;
  @Autowired CacheManager cacheManager;
  @Autowired StringRedisTemplate redis;

  @BeforeEach void clear() { cacheManager.getCache("products").clear(); }

  @Test
  void aCachedProductExpiresAndIsCreatedAgain() throws Exception {
    service.getProduct(2);
    assertTrue(redis.hasKey("products::2"));
    assertTrue(redis.getExpire("products::2", TimeUnit.MILLISECONDS) > 0);
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (redis.hasKey("products::2") && System.nanoTime() < deadline) {
      Thread.sleep(25);
    }
    assertFalse(redis.hasKey("products::2"), "The configured TTL must expire the key");
    service.getProduct(2);
    assertTrue(redis.hasKey("products::2"));
  }
}
