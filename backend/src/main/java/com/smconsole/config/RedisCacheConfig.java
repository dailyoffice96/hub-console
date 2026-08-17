package com.smconsole.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

// 회원/관리자/장애/문의 통계처럼 자주 조회되는데 자주 안 바뀌는 데이터를 레디스에 캐싱한다.
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                // null까지 캐싱하면 메모리만 낭비하고 캐시 무효화도 애매해져서 꺼둔다.
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                // 캐시에 저장할 때 값의 실제 클래스 정보도 같이 저장해야, 꺼낼 때 원래 타입(예:
                // IncidentStatsResponse)으로 복원된다. allowIfBaseType(Object.class)는 신뢰할 수
                // 없는 클래스가 역직렬화되는 걸 막는 최소한의 안전장치다.
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                GenericJacksonJsonRedisSerializer.builder()
                                        .enableDefaultTyping(
                                                BasicPolymorphicTypeValidator.builder()
                                                        .allowIfBaseType(Object.class)
                                                        .build()
                                        )
                                        .build()
                        )
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
