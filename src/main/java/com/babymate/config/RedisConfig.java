package com.babymate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
    
//    @Bean
//    @Primary
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        // ✅ 註冊 Java 8 時間模組
//        objectMapper.registerModule(new JavaTimeModule());
//
//        // ✅ 禁用 timestamp（預設是 true，會變成 [年,月,日...] 陣列）
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        // ✅ 可選：自動註冊其他模組（ex: parameter names, JDK8 模組等）
//        objectMapper.findAndRegisterModules();
//
//        return objectMapper;
//    }
}
