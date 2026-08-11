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


/*캐싱은 아무 데나 넣으면 안 돼요. 좋은 캐싱 후보의 조건:
자주 조회되는데, 자주 안 바뀌는 데이터 조회 비용(쿼리 복잡도)이 좀 있는 것*/


@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {


        // 캐시의 기본 설정 틀을 가져옴
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 캐시 유지 시간: 10분
                .entryTtl(Duration.ofMinutes(10))

                // null 값은 캐싱하지 않음, 불필요한 메모리 낭비와 엉뚱한 캐싱을 막아줌
                .disableCachingNullValues()

                //직렬화(Serialization): 자바의 데이터(객체, 문자 등)를 Redis 서버에 저장할 수 있는 형태(바이트)로 변환하는 과정
                // Redis Key는 String으로 저장
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )

                // Redis Value는 JSON으로 저장
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

        //위에서 설정한 규칙(config)과 연결 정보(connectionFactory)를 모두 합쳐서 최종적인 RedisCacheManager를 만들고 반환
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}